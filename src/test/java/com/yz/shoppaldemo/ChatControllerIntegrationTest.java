package com.yz.shoppaldemo;

import com.yz.shoppaldemo.config.Embedding;
import com.yz.shoppaldemo.service.LuceneVectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShopPalDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LuceneVectorStore vectorStore;
    @Autowired
    private Embedding embeddingConfig;

    @BeforeEach
    void setupFaq() throws IOException {
        // 添加一条测试 FAQ
        String q = "退货流程是什么？";
        String a = "请登录账号，进入订单详情，点击“申请退货”并填写原因。";
        vectorStore.addFaq("test-1", q, a, embeddingConfig.embed(q));
    }

    @Test
    void shouldReturnAnswerFromRagPipeline() {
        // Given
        String userQuestion = "怎么退货？";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(userQuestion, headers);

        // When
        String response = restTemplate.postForObject("http://localhost:" + port + "/api/chat", request, String.class);
        System.out.println(response);
        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
        // 注意：不能断言具体内容（因为大模型输出不确定），但可以检查是否包含关键词或非 fallback
        assertThat(response).doesNotContain("转接人工客服");
    }

    @Test
    void shouldFallbackToHumanWhenNoRelevantFaq() {
        String userQuestion = "你们公司CEO是谁？"; // 无关问题
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(userQuestion, headers);

        String response = restTemplate.postForObject("http://localhost:" + port + "/api/chat", request, String.class);

        assertThat(response).contains("转接人工客服");
    }
}