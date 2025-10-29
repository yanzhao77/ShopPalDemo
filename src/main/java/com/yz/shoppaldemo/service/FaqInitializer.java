package com.yz.shoppaldemo.service;

import com.yz.shoppaldemo.config.Embedding;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FaqInitializer {
    @Autowired
    private LuceneVectorStore vectorStore;
    @Autowired
    private Embedding embeddingConfig;

    @Value("${faq:faq/shopping_faq.md}")
    private String indexPath;

    @PostConstruct
    public void initSampleFaqs() {
        try {
            // 读取 shopping_faq.md 文件
            var resource = getClass().getClassLoader().getResourceAsStream(indexPath);
            if (resource == null) {
                log.warn("未找到 faq/shopping_faq.md 文件，跳过 FAQ 初始化");
                return;
            }

            String content = new String(resource.readAllBytes());
            String[] sections = content.split("\n## "); // 按章节分割

            int count = 0;
            for (String section : sections) {
                if (section.trim().isEmpty()) continue;

                // 提取所有问答对
                var lines = section.split("\n");
                String currentQuestion = null;
                StringBuilder currentAnswer = new StringBuilder();

                for (String line : lines) {
                    if (line.startsWith("Q: ")) {
                        // 如果已有待处理的问答对，先保存
                        if (currentQuestion != null && currentAnswer.length() > 0) {
                            float[] embedding = embeddingConfig.embed(currentQuestion);
                            vectorStore.addFaq(
                                    UUID.randomUUID().toString(),
                                    currentQuestion,
                                    currentAnswer.toString().trim(),
                                    embedding
                            );
                            count++;
                            currentAnswer.setLength(0);
                        }
                        // 开始新的问答对
                        currentQuestion = line.substring(3).trim();
                    } else if (line.startsWith("A: ") && currentQuestion != null) {
                        // 添加答案内容
                        currentAnswer.append(line.substring(3).trim());
                    }
                }

                // 处理最后一个问答对
                if (currentQuestion != null && currentAnswer.length() > 0) {
                    float[] embedding = embeddingConfig.embed(currentQuestion);
                    vectorStore.addFaq(
                            UUID.randomUUID().toString(),
                            currentQuestion,
                            currentAnswer.toString().trim(),
                            embedding
                    );
                    count++;
                }
            }
            log.info("已从 " + indexPath + " 加载 {} 个 FAQ 到 Lucene 向量索引", count);

        } catch (Exception e) {
            log.error("初始化 FAQ 失败", e);
        }
    }
}