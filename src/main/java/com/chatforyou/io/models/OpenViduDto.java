package com.chatforyou.io.models;

import com.chatforyou.io.models.out.SessionOutVo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Builder
@ToString
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenViduDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @SerializedName("creator")
    @JsonProperty("creator")
    private String creator;
//    @SerializedName("recordings")
//    @JsonProperty("recordings")
//    private ArrayList<Recording>() recordings;
    @SerializedName("recording_enabled")
    @JsonProperty("recording_enabled")
    private boolean recordingEnabled;
    @SerializedName("is_recording_active")
    @JsonProperty("is_recording_active")
    private boolean isRecordingActive;
    @SerializedName("broadcasting_enabled")
    @JsonProperty("broadcasting_enabled")
    private boolean broadcastingEnabled;
    @SerializedName("is_broadcasting_active")
    @JsonProperty("is_broadcasting_active")
    private boolean isBroadcastingActive;
    @SerializedName("session")
    @JsonProperty("session")
    private SessionOutVo session;
}
