package com.yz.shoppaldemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LuceneVectorStore {

    private static final String VECTOR_FIELD = "embedding";
    private static final String ID_FIELD = "id";
    private static final String QUESTION_FIELD = "question";
    private static final String ANSWER_FIELD = "answer";

    private Directory directory;
    private IndexWriter indexWriter;
    private IndexSearcher indexSearcher;
    private IndexReader indexReader;

    @Value("${lucene.index-path:${java.io.tmpdir}/lucene_vector_index}")
    private String indexPath;

    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get(indexPath);
        Files.createDirectories(path);
        directory = FSDirectory.open(path);
        log.info("Lucene 向量索引路径: {}", path.toAbsolutePath());

        // 创建或打开索引
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(directory, config);

        // 初始化 searcher
        refreshSearcher();

        log.info("Lucene HNSW 向量存储初始化完成");
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (directory != null) {
            directory.close();
        }
    }

    /**
     * 添加一个 FAQ 条目（自动计算 embedding 外部传入）
     */
    public void addFaq(String id, String question, String answer, float[] embedding) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(ID_FIELD, id, Field.Store.YES));
        doc.add(new TextField(QUESTION_FIELD, question, Field.Store.YES));
        doc.add(new StoredField(ANSWER_FIELD, answer));

        // 关键：添加 HNSW 向量字段（Lucene 9+）
        doc.add(new KnnVectorField(VECTOR_FIELD, embedding, VectorSimilarityFunction.COSINE));

        indexWriter.addDocument(doc);
        indexWriter.commit(); // 立即提交（生产环境可批量）
        refreshSearcher();
        log.debug("已添加 FAQ: {}", question);
    }

    /**
     * 搜索最相似的 Top-K 条目
     */
    public List<FaqResult> search(float[] queryVector, int topK) throws IOException {
        if (indexSearcher == null) {
            throw new IllegalStateException("索引尚未初始化");
        }

        // 构建 KNN 向量查询
        Query knnQuery = new KnnVectorQuery(VECTOR_FIELD, queryVector, topK);
        TopDocs topDocs = indexSearcher.search(knnQuery, topK);

        List<FaqResult> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            // Lucene 的 score 是相似度（越大越相似），但 HNSW 返回的是距离，需转换
            // 实际使用中，我们更关注 doc 内容而非 score
            results.add(new FaqResult(
                    doc.get(ID_FIELD),
                    doc.get(QUESTION_FIELD),
                    doc.get(ANSWER_FIELD),
                    scoreDoc.score // 注意：HNSW 中 score = 1 / (1 + distance)
            ));
        }
        return results;
    }

    private void refreshSearcher() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }
        // 检查目录是否包含有效索引
        if (DirectoryReader.indexExists(directory)) {
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } else {
            // 索引不存在，searcher 保持为 null（后续 search 方法需处理）
            indexReader = null;
            indexSearcher = null;
            log.info("索引尚未创建，等待首次添加文档");
        }
    }

    // 结果封装类
    public static record FaqResult(String id, String question, String answer, float score) {
    }
}