package com.chatforyou.io.models.out;

import com.chatforyou.io.client.Connection;
import com.chatforyou.io.client.Publisher;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionOutVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
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
    private String clientData;
    @SerializedName("token")
    @JsonProperty("token")
    private String token;

//    @SerializedName("publishers")
//    @JsonProperty("publishers")
//    private Map<String, Publisher> publishers = new ConcurrentHashMap<>();
//    @SerializedName("subscribers")
//    @JsonProperty("subscribers")
//    private List<String> subscribers = new ArrayList<>();

    public static ConnectionOutVo of(Connection connection){
        return ConnectionOutVo.builder()
                .connectionId(connection.getConnectionId())
                .status(connection.getStatus())
                .createdAt(connection.createdAt())
                .activeAt(connection.activeAt())
                .location(connection.getLocation())
                .ip(connection.getIp())
                .platform(connection.getPlatform())
                .clientData(connection.getClientData())
                .token(connection.getToken())
//                .publishers(connection.getPublishers())
//                .subscribers(connection.getSubscribers())
                .build();
    }
}
