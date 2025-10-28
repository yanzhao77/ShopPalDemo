package com.yz.shoppaldemo.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "zhipu")
@Data
public class ZhipuDto {
    private String model;
    private String url;
    private String apiKey;
}