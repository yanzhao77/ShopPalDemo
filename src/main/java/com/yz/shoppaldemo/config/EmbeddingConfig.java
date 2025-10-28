package com.yz.shoppaldemo.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 备份Embedding
 */
@Component
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new BgeSmallZhEmbeddingModel(); // 官方实现，已处理 ONNX + tokenizer
    }
}