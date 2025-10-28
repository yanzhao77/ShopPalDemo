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

配置示例 (`application.yml`):
```yaml
server:
  port: 8080  # 服务端口

lucene:
  index-path: ${user.home}/lucene_vector_index  # 向量库存储路径

embedding:
  provider: bgeSmallZhEmbeddingConfig  # 使用本地 ONNX 模型（bgeSmallZhEmbeddingConfig）或智谱云服务（djlEmbeddingConfig）

zhipu:
  model: glm-4-flash  # 智谱 AI 模型，可选：glm-4、glm-4-flash（更快）等
  url: https://open.bigmodel.cn/api/paas/v4
  api-key: ${ZHIPU_API_KEY}  # 从环境变量读取，不要硬编码
```

- 若要使用仓库中的本地 ONNX 模型，将 `embedding.provider` 设为 `bgeSmallZhEmbeddingConfig`，并确保机器上有合适的 ONNX 运行环境与依赖（CPU/GPU）。
- 若使用智谱云 Embedding/LLM，将 `embedding.provider` 设为 `djlEmbeddingConfig`，并在环境变量中设置 `ZHIPU_API_KEY`。

## API 调用示例

服务启动后，可以通过以下方式调用聊天接口：

```bash
# PowerShell 示例
$question = '如何申请退款？'
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/chat' -Body $question -ContentType 'text/plain;charset=utf-8'

# curl 示例（Windows CMD）
curl -X POST "http://localhost:8080/api/chat" ^
  -H "Content-Type: text/plain;charset=utf-8" ^
  -d "如何申请退款？"

# curl 示例（Linux/macOS）
curl -X POST "http://localhost:8080/api/chat" \
  -H "Content-Type: text/plain;charset=utf-8" \
  -d "如何申请退款？"
```

示例响应：
```
根据参考资料，我来为您解答退款相关问题。如果您需要申请退款，可以按以下步骤操作：...
（注：实际响应内容取决于向量库中的参考资料）
```

## 知识库加载与测试

项目提供了示例知识库文件用于快速测试：

- 位置：`src/main/resources/faq/shopping_faq.md`
- 内容：包含常见的电商场景问答，如退货退款流程、订单修改、商品质量问题处理等
- 格式：使用 Markdown 格式，按主题组织，每组包含多个 Q&A 对

测试流程：
1. 确保 `FaqInitializer` 配置的文档目录指向 `src/main/resources/faq`
2. 启动应用，`FaqInitializer` 会自动加载并处理示例知识库
3. 通过上述 API 发送问题，如"如何申请退款？"、"退款多久能到账？"等
4. 系统会基于向量检索返回最相关的参考文档，并让 LLM 生成回答
5. 若检索不到相关文档，系统会返回："请稍等，我帮您转接人工客服。"

您也可以：
- 在示例知识库中添加更多 Q&A
- 创建新的知识库文件（.txt 或 .md）
- 调整 `FaqInitializer` 的分块策略和向量检索参数

## 注意事项与扩展建议

- 请勿在源码或公开仓库中硬编码 API Key。生产环境下使用 Secret Manager 或环境变量注入。
- 若知识库数据量较大，考虑对分块策略、向量维度与相似度搜索参数进行调优，并使用持久化向量数据库或外部服务以提高并发与容量。
- 可以将本地 ONNX 模型替换为更高性能的推理引擎或使用云端模型以获得更好的响应速度与质量。

## 联系与许可

详见仓库根目录的 `LICENSE`。

---

（已追加：运行指引、主要文件说明、模型与知识库使用说明）