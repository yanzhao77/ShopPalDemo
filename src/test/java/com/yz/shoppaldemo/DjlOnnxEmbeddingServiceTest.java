// src/test/java/com/yz/shoppaldemo/service/DjlOnnxEmbeddingServiceTest.java
package com.yz.shoppaldemo;

import com.yz.shoppaldemo.config.Embedding;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DjlOnnxEmbeddingServiceTest {
    @Autowired
    private Embedding embeddingConfig;

    @Test
    void embed_shouldReturn512DimVector() throws Exception {
        float[] vec = embeddingConfig.embed("支持七天无理由退货吗？");
        Assertions.assertThat(vec).hasSize(512);
//        Assertions.assertThat(vec).allMatch(f -> Float.isFinite(f));
    }

    @Test
    void embed_sameText_shouldBeConsistent() throws Exception {
        float[] v1 = embeddingConfig.embed("你好");
        float[] v2 = embeddingConfig.embed("你好");
        Assertions.assertThat(v1).isEqualTo(v2);
    }

    @Test
    void shouldGenerateEmbeddingWithCorrectDimension() {
        // Given
        String text = "如何退货？";

        // When
        float[] embedding = embeddingConfig.embed(text);

        // Then
        assertThat(embedding).isNotNull();
        assertThat(embedding).hasSize(512); // bge-small-zh-v1.5 输出 512 维
    }

    @Test
    void shouldProduceSimilarEmbeddingsForSimilarTexts() {
        float[] e1 = embeddingConfig.embed("怎么退款");
        float[] e2 = embeddingConfig.embed("如何申请退款");

        // 简单计算余弦相似度（可选）
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < e1.length; i++) {
            dot += e1[i] * e2[i];
            norm1 += e1[i] * e1[i];
            norm2 += e2[i] * e2[i];
        }
        double similarity = dot / (Math.sqrt(norm1) * Math.sqrt(norm2));

        assertThat(similarity).isGreaterThan(0.7); // 经验阈值
    }
}