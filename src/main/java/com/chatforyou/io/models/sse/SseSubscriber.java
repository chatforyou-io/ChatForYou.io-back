package com.chatforyou.io.models.sse;

import com.chatforyou.io.services.SseSubscriberService;
import io.micrometer.common.lang.Nullable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Getter
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
@Slf4j
public class SseSubscriber {

    private final Long userIdx;
    private final Sinks.Many<ServerSentEvent<?>> sink;
    private final Flux<ServerSentEvent<?>> flux;
    private final SseType sseType;
    private final String sessionId;
    private final SseSubscriberService func;
    private Disposable keepAliveTask;

    public static SseSubscriber of(Long userIdx, SseType sseType, @Nullable String sessionId, SseSubscriberService func) {
        Sinks.Many<ServerSentEvent<?>> sink = Sinks.many().multicast().onBackpressureBuffer();

        return builder()
                .userIdx(userIdx)
                .sink(sink)
                .sseType(sseType)
                .sessionId(sessionId)
                .func(func)
                .build();
    }

    public SseSubscriber buildFlux() {
        Flux<ServerSentEvent<?>> boundFlux = sink.asFlux()
                .doOnCancel(() -> {
                    log.debug("[SSE] SSE canceled for user {} in {}", userIdx, sseType);
                })
                .doFinally(signal -> {
                    log.debug("[SSE] SSE finalized for user {} in {} due to {}", userIdx, sseType, signal);
                    cleanup();
                });

        return this.toBuilder().flux(boundFlux).build();
    }

    public SseSubscriber startKeepAlive() {
        this.keepAliveTask = Flux.interval(Duration.ofSeconds(10))
                .map(i -> ServerSentEvent.<String>builder()
                        .event("keepAlive")
                        .data("ping")
                        .build())
                .subscribe(event -> {
                    Sinks.EmitResult result = sink.tryEmitNext(event);
                    if (result.isFailure()) {
                        if (result == Sinks.EmitResult.FAIL_TERMINATED) {
                            log.info("[SSE] KeepAlive emit skipped: connection already terminated for user {}", userIdx);
                        } else {
                            log.warn("[SSE] KeepAlive emit failed for user {} :: {}", userIdx, result);
                        }
                        this.cleanup();
                    }
                }, error -> {
                    if (error instanceof java.io.IOException) {
                        log.warn("[SSE] Broken Pipe IOException occurred user: {} :: {}", userIdx, error.getMessage());
                    } else {
                        log.error("[SSE] Unknown Server Exception occurred user: {} :: {}", userIdx, error.getMessage());
                    }
                    this.cleanup();
                });

        return this;
    }

    private void cleanup() {
        if (keepAliveTask != null && !keepAliveTask.isDisposed()) {
            keepAliveTask.dispose();
        }
        sink.tryEmitComplete();
        func.removeSubscriber(sseType, userIdx, sessionId);
    }
}