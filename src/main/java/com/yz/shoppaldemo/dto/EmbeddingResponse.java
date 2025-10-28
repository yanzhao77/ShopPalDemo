package com.yz.shoppaldemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingResponse {
    private List<EmbeddingData> data;
}
