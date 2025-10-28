package com.yz.shoppaldemo.service;

import com.yz.shoppaldemo.config.Embedding;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FaqInitializer {
    @Autowired
    private LuceneVectorStore vectorStore;
    @Autowired
    private Embedding embeddingConfig;


    @PostConstruct
    public void initSampleFaqs() {
        String[][] faqs = {
                {"订单多久能发货？", "我们通常在24小时内发货，节假日顺延。"},
                {"支持7天无理由退货吗？", "支持！商品需保持完好、未使用、包装齐全。"},
                {"优惠券怎么用？", "下单时在支付页面选择“使用优惠券”即可自动抵扣。"},
                {"蓝牙耳机有降噪功能吗？", "Pro 版支持主动降噪，标准版仅支持物理隔音。"},
                {"支付失败怎么办？", "请检查网络或更换支付方式，订单保留30分钟。"}
        };
        try {
            for (String[] faq : faqs) {
                String question = faq[0];
                String answer = faq[1];
                float[] embedding = embeddingConfig.embed(question);
                vectorStore.addFaq(UUID.randomUUID().toString(), question, answer, embedding);
            }
            log.info("示例 FAQ 已加载到 Lucene 向量索引");
        } catch (Exception e) {
            log.error("初始化 FAQ 失败", e);
        }
    }
}