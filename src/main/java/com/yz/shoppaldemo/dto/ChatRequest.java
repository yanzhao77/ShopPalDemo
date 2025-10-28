// dto/ChatRequest.java
package com.yz.shoppaldemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String model = "glm-4-flash";
    private List<Message> messages;
}