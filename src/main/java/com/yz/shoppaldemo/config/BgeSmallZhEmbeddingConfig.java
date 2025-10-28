package com.yz.shoppaldemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "embedding.provider", havingValue = "bgeSmallZhEmbeddingConfig")
public class BgeSmallZhEmbeddingConfig implements Embedding {
    @Autowired
    EmbeddingConfig embeddingConfig;

    @Override
    public float[] embed(String text) {
        return embeddingConfig.embeddingModel().embed(text).content().vector();
    }

    @Override
    public void destroy() {
        embeddingConfig.embeddingModel().dimension();
    }
}
