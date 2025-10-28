package com.yz.shoppaldemo;

import com.yz.shoppaldemo.config.Embedding;
import com.yz.shoppaldemo.service.LuceneVectorStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LuceneVectorStoreTest {
    @Autowired
    private LuceneVectorStore vectorStore;
    @Autowired
    private Embedding embeddingConfig;

    @Test
    void shouldAddAndSearchFaq() throws IOException {
        // Given
        String question = "支持七天无理由退货吗？";
        String answer = "是的，本店支持七天无理由退货。";
        float[] embedding = embeddingConfig.embed(question);
        String id = "faq-1";

        // When
        vectorStore.addFaq(id, question, answer, embedding);
        List<LuceneVectorStore.FaqResult> results = vectorStore.search(embedding, 1);

        // Then
        assertThat(results).hasSize(1);
        LuceneVectorStore.FaqResult result = results.get(0);
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.question()).isEqualTo(question);
        assertThat(result.answer()).isEqualTo(answer);
        assertThat(result.score()).isGreaterThan(0.9f);
    }

    @Test
    void shouldReturnRelevantFaqForSimilarQuery() throws IOException {
        // Add a FAQ
        String faqQuestion = "怎么申请退款？";
        String faqAnswer = "请在订单页面点击“申请退款”按钮。";
        vectorStore.addFaq("faq-2", faqQuestion, faqAnswer, embeddingConfig.embed(faqQuestion));

        // Search with a similar query
        float[] queryVec = embeddingConfig.embed("如何退款？");
        List<LuceneVectorStore.FaqResult> results = vectorStore.search(queryVec, 2);

        assertThat(results).isNotEmpty();
        LuceneVectorStore.FaqResult top = results.get(0);
        assertThat(top.answer()).contains("申请退款");
    }
}