package com.chatforyou.io.models.sse;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 특정 클라이언트(userIdx)와의 SSE(Server-Sent Events) 연결을 관리
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SseSubscriber {

    private final Long userIdx;
    private final SseEmitter sseEmitter; // sse 전송을 담당하는 객체
    private boolean isSseCompleted; // sse 전송이 끝났는지 확인
    private ScheduledFuture<?> scheduledFuture; // 주기적인 작업(keep-alive 메시지 전송)을 관리하는 객체
    private final ScheduledExecutorService scheduler; // 주기적인 작업을 실행하기 위한 스케줄러

    public static SseSubscriber of(Long userIdx, SseEmitter emitter, ScheduledExecutorService scheduler) {
        return builder()
                .userIdx(userIdx)
                .sseEmitter(emitter)
                .scheduler(scheduler)
                .build();
    }

    /**
     * SSE 연결을 유지하기 위해 주기적으로 keep-alive 메시지를 보내는 작업을 스케줄링
     * 연결 초기화 시 "connected" 메시지를 클라이언트에게 전송
     * 연결 확인 시 주기적으로  keep-alive 메시지 전송
     */
    public void scheduleKeepAliveTask() throws IOException {

        notifyConnection();

        // 25초마다 주기적으로 keep-alive 메시지를 전송하는 작업을 스케줄링
        this.scheduledFuture = this.scheduler.scheduleAtFixedRate(() -> {
            try {
                if(this.sseEmitter == null  || this.isSseCompleted) { // sse 객체가 없어졌거나 연결이 종료된 경우 예외처리
                    throw new RuntimeException();
                }
                sendKeepAlive();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 15, 15, TimeUnit.SECONDS);

        this.sseEmitter.onCompletion(this::cleanupSubscriber);
        this.sseEmitter.onTimeout(this::cleanupSubscriber);
        this.sseEmitter.onError((e) -> cleanupSubscriber());
    }

    /**
     * 클라이언트에게 "connected" 상태를 알리는 메시지를 전송
     * @throws IOException 메시지 전송 중 에러가 발생한 경우
     */
    private void notifyConnection() throws IOException {
        Map<String, String> result = Map.of("data", "connected");
        this.sseEmitter.send(SseEmitter.event().name("connectionStatus").data(result));
    }

    /**
     * 클라이언트에게 주기적으로 keep-alive 메시지("ping")를 전송하여 SSE 연결 유지
     * @throws IOException 메시지 전송 중 에러가 발생한 경우
     */
    private void sendKeepAlive() throws IOException {
        Map<String, String> result = Map.of("data", "ping");
        this.sseEmitter.send(SseEmitter.event().name("keepAlive").data(result));
    }

    /**
     * 스케줄링된 작업을 취소하고 SSE 연결을 종료
     * 이때 스케줄링된 작업이 아직 취소되지 않았다면 취소 처리
     */
    public void cleanupSubscriber() {
        if (this.scheduledFuture != null && !this.scheduledFuture.isCancelled()) {
            this.scheduledFuture.cancel(true);
        }
        this.sseEmitter.complete();
        this.isSseCompleted = true;
    }
}