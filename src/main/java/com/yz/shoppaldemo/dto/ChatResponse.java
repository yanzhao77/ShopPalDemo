// dto/ChatResponse.java
package com.yz.shoppaldemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String id;
    private String model;
    private Long created;
    @JsonProperty("request_id")
    private String requestId;
    private List<Choice> choices;
    private Usage usage;
}

