package com.chatforyou.io.models.out;

import com.chatforyou.io.client.Session;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionOutVo {
    @SerializedName("sessionId")
    @JsonProperty("sessionId")
    private String sessionId;
    @SerializedName("createdAt")
    @JsonProperty("createdAt")
    private long createdAt;
    @SerializedName("connections")
    @JsonProperty("connections")
    private Map<String, ConnectionOutVo> connections = new ConcurrentHashMap<>();
    @SerializedName("recording")
    @JsonProperty("recording")
    private boolean recording = false;
    @SerializedName("broadcasting")
    @JsonProperty("broadcasting")
    private boolean broadcasting = false;

    public static SessionOutVo of(Session session, Map<String, ConnectionOutVo> connections){
        return SessionOutVo.builder()
                .sessionId(session.getSessionId())
                .connections(connections)
                .createdAt(session.createdAt())
                .recording(session.isBeingRecorded())
                .broadcasting(session.isBeingBroadcasted())
                .build();

    }
}
