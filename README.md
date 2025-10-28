# ShopPalDemo
电商智能客服demo,一个 完整的 Spring Boot + JVector + 智谱 Embedding + RAG 的最小可运行示例代码（含知识库加载、搜索、LLM 调用）

### Description
这是一个基于 Spring Boot 构建的最小可运行 RAG（Retrieval-Augmented Generation）系统示例，完整集成了以下关键技术组件：

JVector：用于高效向量存储与相似性检索的纯 Java 向量数据库；
智谱 AI（Zhipu AI）Embedding API：将文本转换为高质量语义向量；
智谱 AI 大语言模型（如 GLM-4）：用于生成自然语言回答；
本地知识库支持：可从文本文件（如 .txt 或 .md）加载文档，自动分块、嵌入并建立索引。
项目实现了端到端流程：知识加载 → 文本分块 → 向量化 → 向量存储 → 用户查询嵌入 → 相似性检索 → LLM 生成答案，仅需配置智谱 API Key 即可一键运行，适合快速验证 RAG 架构或作为企业智能客服、知识问答系统的起点。

## 项目结构说明

下面列出仓库中主要目录和文件的作用，便于快速定位源码与资源：

- `src/main/java/com/yz/shoppaldemo/ShopPalDemoApplication.java`：Spring Boot 应用入口。
- `src/main/java/com/yz/shoppaldemo/config/EmbeddingConfig.java`：本地/远程 Embedding 服务相关的 Spring 配置。
- `src/main/java/com/yz/shoppaldemo/config/ZhipuConfig.java`：智谱 API（Zhipu）相关配置类。
- `src/main/java/com/yz/shoppaldemo/controller/ChatController.java`：HTTP 接口入口，暴露聊天/问答相关 REST 接口。
- `src/main/java/com/yz/shoppaldemo/dto/`：请求/响应与内部数据结构（如 `ChatRequest`、`ChatResponse`、`EmbeddingRequest/Response` 等）。
- `src/main/java/com/yz/shoppaldemo/service/FaqInitializer.java`：知识库初始化器，用于加载本地文件、分块并写入向量库。
- `src/main/java/com/yz/shoppaldemo/service/LuceneVectorStore.java`：基于 JVector（或 Lucene 向量能力）实现的向量存储与相似度检索逻辑。
- `src/main/java/com/yz/shoppaldemo/service/OnnxEmbeddingService.java`：若使用本地 ONNX 模型（仓库中已包含示例模型），负责本地文本嵌入的实现。
- `src/main/java/com/yz/shoppaldemo/service/ZhipuApiClient.java`：调用智谱云 Embedding 与 LLM 接口的客户端实现。
- `src/main/resources/static/model/bge-small-zh-v1.5/`：示例本地 ONNX 模型及其 tokenizer/config，包含 `model.onnx`、`vocab.txt` 等文件，可用于本地推理（仅当使用 OnnxEmbeddingService 时）。
- `src/main/resources/application.yml`：应用配置入口，包含智谱 API Key、服务端口、向量库参数等（请在生产环境使用安全方式管理密钥）。

## 快速开始（在 Windows 下使用项目自带的 `mvnw`）

1. 准备 Java 环境（建议 JDK 17 或与 `pom.xml` 指定的版本一致）。
2. 在项目根目录打开 PowerShell，设置智谱 API Key：

```powershell
$env:ZHIPU_API_KEY="your_zhipu_api_key_here"
``` 

3. 编译并运行应用：

```powershell
.\mvnw clean package -DskipTests; .\mvnw spring-boot:run
```

4. 启动后，默认 REST 接口（例如聊天/问答）由 `ChatController` 暴露，通常在 `http://localhost:8080` 下可访问。请参考代码中的接口路径（控制器中注解）。

## 使用本地模型 vs. 智谱云 Embedding

- 若要使用仓库中的本地 ONNX 模型，确保 `application.yml` 中将 embedding 配置指向本地实现（OnnxEmbeddingService），并且机器上有合适的 ONNX 运行环境与依赖（CPU/GPU）。
- 若使用智谱云 Embedding/LLM，请在 `application.yml` 中配置 `zhipu` 相关项并设置环境变量 `ZHIPU_API_KEY`。

## 知识库加载与测试

- 本项目提供 `FaqInitializer`，会从指定目录加载 `.txt` / `.md` 文档，自动分块并调用 Embedding 服务把向量写入 `LuceneVectorStore`。启动时可触发初始化流程（请查看 `FaqInitializer` 的注释与配置）。
- 测试流程：准备若干文本文档，启动应用后通过 `ChatController` 提交一个 `ChatRequest`，服务会基于向量检索返回相关文档并调用 LLM 生成回答。

## 注意事项与扩展建议

- 请勿在源码或公开仓库中硬编码 API Key。生产环境下使用 Secret Manager 或环境变量注入。
- 若知识库数据量较大，考虑对分块策略、向量维度与相似度搜索参数进行调优，并使用持久化向量数据库或外部服务以提高并发与容量。
- 可以将本地 ONNX 模型替换为更高性能的推理引擎或使用云端模型以获得更好的响应速度与质量。

## 联系与许可

详见仓库根目录的 `LICENSE`。

---

（已追加：运行指引、主要文件说明、模型与知识库使用说明）