package com.chatforyou.io.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum OpenViduEvent {
    @JsonProperty("sessionCreated")
    SESSION_CREATED(01),

    @JsonProperty("sessionDestroyed")
    SESSION_DESTROYED(02),

    @JsonProperty("participantJoined")
    PARTICIPANT_JOINED(03),

    @JsonProperty("participantLeft")
    PARTICIPANT_LEFT(04),

    @JsonProperty("webrtcConnectionCreated")
    WEBRTC_CONNECTION_CREATED(05),

    @JsonProperty("webrtcConnectionDestroyed")
    WEBRTC_CONNECTION_DESTROYED(06);

    private final int code;

    OpenViduEvent(int code) {
        this.code = code;
    }

}
