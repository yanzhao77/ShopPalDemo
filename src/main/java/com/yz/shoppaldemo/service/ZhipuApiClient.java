package com.yz.shoppaldemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yz.shoppaldemo.dto.*;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class ZhipuApiClient {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public ZhipuApiClient(ZhipuDto zhipuDto) {
        this.apiKey = zhipuDto.getApiKey();
    }

    public String chat(String prompt) throws IOException {
        ChatRequest req = new ChatRequest();
        req.setMessages(Collections.singletonList(new Message("user", prompt)));

        String json = mapper.writeValueAsString(req);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("https://open.bigmodel.cn/api/paas/v4/chat/completions")
                .header("Authorization", apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Chat API error: " + response.body().string());
            }
            ChatResponse resp = mapper.readValue(response.body().string(), ChatResponse.class);
            return resp.getChoices().get(0).getMessage().getContent();
        }
    }

    /**
     * 废弃，这个模型要钱
     *
     * @param text
     * @return
     * @throws IOException
     */
    @Deprecated
    public float[] getEmbedding(String text) throws IOException {
        EmbeddingRequest req = new EmbeddingRequest();
        req.setInput(Collections.singletonList(text));

        String json = mapper.writeValueAsString(req);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("https://open.bigmodel.cn/api/paas/v4/embeddings")
                .header("Authorization", apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Embedding API error: " + response.body().string());
            }
            EmbeddingResponse resp = mapper.readValue(response.body().string(), EmbeddingResponse.class);
            return resp.getData().get(0).getEmbedding();
        }
    }


}