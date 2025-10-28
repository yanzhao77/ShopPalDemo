package com.yz.shoppaldemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 防御性：防止未来新增字段崩溃
public class Choice {
    private Integer index;
    private Message message;

    @JsonProperty("finish_reason")
    private String finishReason;

}
