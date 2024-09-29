package com.chatforyou.io.models;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OpenViduWebhookData {
    private OpenViduEvent event;
    private String sessionId;
    private String uniqueSessionId;
    private long timestamp;
    private String participantId;
    private String connectionId;
    private String location;
    private String ip;
    private String platform;
    private String clientData;
    private String serverData;
    private String streamId;
    private String connection;
    private String videoSource;
    private int videoFramerate;
    private boolean audioEnabled;
    private boolean videoEnabled;
    private long startTime;
    private int duration;
    private String reason;
    private String receivingFrom;
    private VideoDimensions videoDimensions;

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    private class VideoDimensions{
        private int width;
        private int height;
    }
}
