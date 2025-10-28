package com.yz.shoppaldemo.config;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "embedding.provider", havingValue = "djlEmbeddingConfig")
public class DjlEmbeddingConfig implements Embedding {
    private HuggingFaceTokenizer tokenizer;
    private OrtSession session;
    private OrtEnvironment env;
    private static String tokenizer_path = "static/model/bge-small-zh-v1.5/tokenizer.json";
    private static String model_path = "static/model/bge-small-zh-v1.5/model_int8.onnx";

    public DjlEmbeddingConfig() throws IOException, OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.tokenizer = HuggingFaceTokenizer.newInstance(Paths.get(new ClassPathResource(tokenizer_path).getURI()));
        this.session = env.createSession(Paths.get(new ClassPathResource(model_path).getURI()).toString());
    }

    @Override
    public float[] embed(String text) {
        try {
            Encoding encoding = tokenizer.encode(text);
            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] tokenTypeIds = new long[inputIds.length];

            try (OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, new long[][]{inputIds});
                 OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, new long[][]{attentionMask});
                 OnnxTensor tokenTypeIdsTensor = OnnxTensor.createTensor(env, new long[][]{tokenTypeIds})) {

                try (OrtSession.Result result = session.run(Map.of(
                        "input_ids", inputIdsTensor,
                        "attention_mask", attentionMaskTensor,
                        "token_type_ids", tokenTypeIdsTensor
                ))) {
                    OnnxTensor output = (OnnxTensor) result.get(0);
                    Object value = output.getValue();

                    if (!(value instanceof float[][][])) {
                        throw new IllegalStateException("Unexpected output type: " + value.getClass());
                    }

                    float[][][] raw = (float[][][]) value;
                    if (raw.length == 0 || raw[0].length == 0) {
                        throw new IllegalStateException("Empty embedding output");
                    }

                    // 取 [CLS] token 的 embedding（BGE 的标准做法）
                    return raw[0][0]; // [batch=0][token=0]
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Embedding failed for text: " + text, e);
        }
    }

    @Override
    @PreDestroy
    public void destroy() {
        if (session != null) {
            try {
                session.close();
            } catch (OrtException ignored) {
            }
        }
        if (env != null) {
            env.close();
        }
    }
}
