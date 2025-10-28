package com.yz.shoppaldemo.controller;

import com.yz.shoppaldemo.config.Embedding;
import com.yz.shoppaldemo.service.LuceneVectorStore;
import com.yz.shoppaldemo.service.ZhipuApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private LuceneVectorStore vectorStore;
    @Autowired
    private ZhipuApiClient llmClient;
    @Autowired
    private Embedding embeddingConfig;

    @PostMapping("/chat")
    public String chat(@RequestBody String userQuestion) {
        try {
            // ✅ 使用本地 ONNX 模型生成 embedding（免费！）
            float[] queryVec = embeddingConfig.embed(userQuestion);

            var results = vectorStore.search(queryVec, 2);

            if (results.isEmpty()) {
                return "请稍等，我帮您转接人工客服。";
            }

            String context = results.stream()
                    .map(r -> "Q: " + r.question() + "\nA: " + r.answer())
                    .collect(Collectors.joining("\n\n"));

            String prompt = """
                    你是一个专业电商客服，请根据以下参考资料回答用户问题。
                    如果资料不相关，请回答：“请稍等，我帮您转接人工客服。”
                    
                    参考资料：
                    %s
                    
                    用户问题：%s
                    """.formatted(context, userQuestion);

            return llmClient.chat(prompt);

        } catch (IOException e) {
            e.printStackTrace();
            return "系统繁忙，请稍后再试。";
        } catch (Exception e) {
            e.printStackTrace();
            return "嵌入生成失败，请稍后再试。";
        }
    }

    @PostMapping("/faq")
    public String addFaq(@RequestParam String question, @RequestParam String answer) {
        try {
            //  使用本地 embedding
            float[] embedding = embeddingConfig.embed(question);
            vectorStore.addFaq(java.util.UUID.randomUUID().toString(), question, answer, embedding);
            return " FAQ 已添加";
        } catch (Exception e) {
            return " 添加失败: " + e.getMessage();
        }
    }
}