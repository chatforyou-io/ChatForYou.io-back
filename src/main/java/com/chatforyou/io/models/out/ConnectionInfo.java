package com.chatforyou.io.models.out;

import com.chatforyou.io.client.Connection;
import com.chatforyou.io.client.Publisher;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ConnectionInfo {
    @SerializedName("connectionId")
    @JsonProperty("connectionId")
    private String connectionId;
    @SerializedName("status")
    @JsonProperty("status")
    private String status;
    @SerializedName("createdAt")
    @JsonProperty("createdAt")
    private Long createdAt;
    @SerializedName("activeAt")
    @JsonProperty("activeAt")
    private Long activeAt;
    @SerializedName("location")
    @JsonProperty("location")
    private String location;
    @SerializedName("ip")
    @JsonProperty("ip")
    private String ip;
    @SerializedName("platform")
    @JsonProperty("platform")
    private String platform;
    @SerializedName("clientData")
    @JsonProperty("clientData")
    private JsonObject clientData;
    @SerializedName("token")
    @JsonProperty("token")
    private String token;

    @SerializedName("publishers")
    @JsonProperty("publishers")
    private Map<String, Publisher> publishers = new ConcurrentHashMap<>();
    @SerializedName("subscribers")
    @JsonProperty("subscribers")
    private List<String> subscribers = new ArrayList<>();

    public static ConnectionInfo of(Connection connection){
        return ConnectionInfo.builder()
                .connectionId(connection.getConnectionId())
                .status(connection.getStatus())
                .createdAt(connection.createdAt())
                .activeAt(connection.activeAt())
                .location(connection.getLocation())
                .ip(connection.getIp())
                .platform(connection.getPlatform())
                .clientData(connection.getClientData() != null ? new Gson().fromJson(connection.getClientData(), JsonObject.class) : new JsonObject())
                .token(connection.getToken())
                .subscribers(connection.getSubscribers())
                .build();
    }
}
