package com.yz.shoppaldemo.config;

import jakarta.annotation.PreDestroy;

public interface Embedding {
    float[] embed(String text);

    void destroy();
}
