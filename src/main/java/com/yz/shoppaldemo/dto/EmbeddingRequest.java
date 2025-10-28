// dto/EmbeddingRequest.java
package com.yz.shoppaldemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingRequest {
    private String model = "embedding-2";
    private List<String> input;
}