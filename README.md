# LangChain4j Agent 设计模式学习项目

这是一个用于学习 LangChain4j Agent 设计模式的 Spring Boot 项目，集成了本地 Ollama 服务。项目通过简历生成和优化的实际应用场景，演示了如何使用 LangChain4j 的 Agentic 功能实现不同类型的 Agent 工作流。

**Package**: `com.cnblogs.yjmyzz.langchain4j.study`

## 🚀 项目特性

- **Java 25**: 使用最新的 Java 版本
- **Spring Boot 4.0.0**: 现代化的 Spring Boot 框架
- **LangChain4j 1.10.0**: 强大的 Java AI 框架
- **LangChain4j Agentic 1.10.0-beta18**: Agent 设计模式支持
- **Ollama 集成**: 支持本地大语言模型
  - 聊天模型：默认使用 `deepseek-v3.1:671b-cloud`
- **Agent 设计模式**: 演示多种 Agent 工作流模式
  - **基础 Agent**: 单个 Agent 的使用
  - **顺序工作流**: 多个 Agent 按顺序执行
  - **循环工作流**: Agent 循环执行直到满足退出条件
  - **并行工作流**: 多个 Agent 并行执行（目录预留）

## 📋 前置要求

1. **Java 25**: 确保已安装 JDK 25
2. **Maven 3.6+**: 确保已安装 Maven
3. **Ollama**: 确保已安装并启动 Ollama 服务

## 🛠️ 安装和配置

### 1. 安装 Ollama

访问 [Ollama 官网](https://ollama.ai/) 下载并安装 Ollama。

### 2. 启动 Ollama 服务

```bash
# 启动 Ollama 服务
ollama serve
```

### 3. 下载模型

```bash
# 下载聊天模型（默认模型）
ollama pull deepseek-v3.1:671b-cloud

# 或者下载其他模型
ollama pull qwen3:0.6b
ollama pull llama2
ollama pull llama2:7b
ollama pull llama2:13b
```

### 4. 克隆项目

```bash
git clone https://github.com/yjmyzz/agent-design-pattern-with-langchaing4j.git
cd agent-design-pattern-with-langchaing4j
```

### 5. 编译项目

```bash
mvn clean compile
```

### 6. 运行项目

```bash
mvn spring-boot:run
```

## 📚 示例说明

项目包含四个主要示例，演示不同的 Agent 设计模式：

### 1. 基础 Agent（Basic Agent）

**文件位置**: `src/main/java/com/cnblogs/yjmyzz/langchain4j/study/agentic/_1_basic_agent/`

**示例文件**:
- `_1a_Basic_Agent_Example.java` - 基础 Agent 示例
- `_1b_Basic_Agent_Example_Structured.java` - 结构化输出示例
- `CvGenerator.java` - 简历生成 Agent 接口
- `CvGeneratorStructuredOutput.java` - 结构化输出简历生成 Agent

**功能说明**:
- 演示如何创建和使用单个 Agent
- 将用户的个人简介转换成完整的简历
- 支持普通文本输出和结构化输出两种方式

**运行示例**:
```bash
# 运行基础 Agent 示例
mvn exec:java -Dexec.mainClass="com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent._1a_Basic_Agent_Example"
```

**核心代码**:
```java
CvGenerator cvGenerator = AgenticServices
        .agentBuilder(CvGenerator.class)
        .chatModel(model)
        .outputKey("masterCv")
        .build();

String cv = cvGenerator.generateCv(lifeStory);
```

### 2. 顺序工作流（Sequential Workflow）

**文件位置**: `src/main/java/com/cnblogs/yjmyzz/langchain4j/study/agentic/_2_sequential_workflow/`

**示例文件**:
- `_2a_Sequential_Agent_Example.java` - 顺序工作流示例（无类型）
- `_2b_Sequential_Agent_Example_Typed.java` - 顺序工作流示例（类型化）
- `CvTailor.java` - 简历优化 Agent 接口
- `SequenceCvGenerator.java` - 顺序简历生成器接口

**功能说明**:
- 演示如何将多个 Agent 组合成顺序工作流
- 第一个 Agent（CvGenerator）生成主简历
- 第二个 Agent（CvTailor）根据职位描述优化简历
- 演示如何在 Agent 之间传递参数

**运行示例**:
```bash
# 运行顺序工作流示例
mvn exec:java -Dexec.mainClass="com.cnblogs.yjmyzz.langchain4j.study.agentic._2_sequential_workflow._2a_Sequential_Agent_Example"
```

**核心代码**:
```java
CvGenerator cvGenerator = AgenticServices
        .agentBuilder(CvGenerator.class)
        .chatModel(model)
        .outputKey("masterCv")
        .build();

CvTailor cvTailor = AgenticServices
        .agentBuilder(CvTailor.class)
        .chatModel(model)
        .outputKey("tailoredCv")
        .build();

UntypedAgent tailoredCvGenerator = AgenticServices
        .sequenceBuilder()
        .subAgents(cvGenerator, cvTailor)
        .outputKey("tailoredCv")
        .build();
```

### 3. 循环工作流（Loop Workflow）

**文件位置**: `src/main/java/com/cnblogs/yjmyzz/langchain4j/study/agentic/_3_loop_workflow/`

**示例文件**:
- `_3a_Loop_Agent_Example.java` - 循环工作流示例
- `_3a_Loop_Agent_Example_States_And_Fail.java` - 循环工作流示例（带状态和失败处理）
- `CvReviewer.java` - 简历评审 Agent 接口
- `ScoredCvTailor.java` - 评分简历优化 Agent 接口

**功能说明**:
- 演示如何实现循环工作流
- CvReviewer Agent 评审简历并给出分数和反馈
- ScoredCvTailor Agent 根据反馈优化简历
- 循环执行直到分数达到阈值（默认 0.8）或达到最大迭代次数（默认 3 次）

**运行示例**:
```bash
# 运行循环工作流示例
mvn exec:java -Dexec.mainClass="com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow._3a_Loop_Agent_Example"
```

**核心代码**:
```java
CvReviewer cvReviewer = AgenticServices.agentBuilder(CvReviewer.class)
        .chatModel(model)
        .outputKey("cvReview")
        .build();

ScoredCvTailor scoredCvTailor = AgenticServices.agentBuilder(ScoredCvTailor.class)
        .chatModel(model)
        .outputKey("cv")
        .build();

UntypedAgent reviewedCvGenerator = AgenticServices
        .loopBuilder()
        .subAgents(cvReviewer, scoredCvTailor)
        .outputKey("cv")
        .exitCondition(agenticScope -> {
            CvReview review = (CvReview) agenticScope.readState("cvReview");
            return review.score > 0.8;
        })
        .maxIterations(3)
        .build();
```

### 4. 并行工作流（Parallel Workflow）

**文件位置**: `src/main/java/com/cnblogs/yjmyzz/langchain4j/study/agentic/_4_parallel_workflow/`

**状态**: 目录已创建，示例代码待实现

## ⚙️ 配置说明

项目配置文件位于 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: langchain4j-study
  
  # 日志配置
  logging:
    level:
      com.example.langchain4jstudy: DEBUG
      dev.langchain4j: DEBUG
    pattern:
      console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Ollama配置
ollama:
  base-url: http://localhost:11434          # Ollama服务地址
  model: deepseek-v3.1:671b-cloud           # 聊天模型名称
  timeout: 60                               # 请求超时时间（秒）

# 应用信息
info:
  app:
    name: langchain4j Study
    version: 1.0.0
    description: langchain4j学习项目 - Agent设计模式示例
```

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/cnblogs/yjmyzz/langchain4j/study/
│   │   ├── AgentDesignPatternApplication.java    # 主启动类
│   │   ├── agentic/
│   │   │   ├── _1_basic_agent/                   # 基础 Agent 示例
│   │   │   │   ├── _1a_Basic_Agent_Example.java
│   │   │   │   ├── _1b_Basic_Agent_Example_Structured.java
│   │   │   │   ├── CvGenerator.java
│   │   │   │   └── CvGeneratorStructuredOutput.java
│   │   │   ├── _2_sequential_workflow/           # 顺序工作流示例
│   │   │   │   ├── _2a_Sequential_Agent_Example.java
│   │   │   │   ├── _2b_Sequential_Agent_Example_Typed.java
│   │   │   │   ├── CvTailor.java
│   │   │   │   └── SequenceCvGenerator.java
│   │   │   ├── _3_loop_workflow/                 # 循环工作流示例
│   │   │   │   ├── _3a_Loop_Agent_Example.java
│   │   │   │   ├── _3a_Loop_Agent_Example_States_And_Fail.java
│   │   │   │   ├── CvReviewer.java
│   │   │   │   └── ScoredCvTailor.java
│   │   │   └── _4_parallel_workflow/             # 并行工作流示例（待实现）
│   │   ├── config/
│   │   │   └── OllamaConfig.java                  # Ollama 配置类
│   │   ├── domain/
│   │   │   ├── Cv.java                           # 简历领域模型
│   │   │   └── CvReview.java                     # 简历评审领域模型
│   │   └── util/
│   │       ├── AgenticScopePrinter.java          # AgenticScope 打印工具
│   │       └── StringLoader.java                # 资源文件加载工具
│   └── resources/
│       ├── application.yml                       # 应用配置
│       └── documents/                            # 示例文档
│           ├── user_life_story.txt               # 用户个人经历
│           ├── master_cv.txt                     # 主简历
│           ├── job_description_backend.txt       # 后端职位描述
│           └── job_description_fullstack.txt     # 全栈职位描述
└── test/
    └── java/com/cnblogs/yjmyzz/langchain4j/study/
```

## 📦 Package 结构

项目使用标准的 Maven package 命名规范：
- **GroupId**: `com.yjmyzz`
- **ArtifactId**: `agent-design-pattern-with-langchaing4j`
- **Version**: `1.0.0`
- **Package**: `com.cnblogs.yjmyzz.langchain4j.study`
- **主类**: `AgentDesignPatternApplication`

## 🔧 核心组件说明

### 1. 配置类

#### OllamaConfig.java
- 配置 Ollama 聊天模型
- 支持自定义模型名称、服务地址和超时时间
- 启用请求和响应日志记录
- 使用 `@Bean` 注解注册为 Spring Bean，支持依赖注入
- Bean 名称：`ollamaChatModel`

### 2. Agent 接口

#### CvGenerator.java
- 基础 Agent 接口，用于生成简历
- 使用 `@Agent` 注解定义 Agent 描述
- 使用 `@UserMessage` 注解定义用户消息模板
- 使用 `@V` 注解标记变量，用于在 AgenticScope 中传递

#### CvTailor.java
- 简历优化 Agent 接口
- 接收原始简历和优化指令，生成优化后的简历
- 使用 `@SystemMessage` 定义系统提示词

#### CvReviewer.java
- 简历评审 Agent 接口
- 接收简历和职位描述，返回评审结果（分数和反馈）
- 返回结构化对象 `CvReview`

#### ScoredCvTailor.java
- 评分简历优化 Agent 接口
- 接收简历和评审反馈，生成优化后的简历

### 3. 领域模型

#### Cv.java
- 简历领域模型
- 包含技能、专业经历、教育背景等字段
- 使用 `@Description` 注解描述字段含义

#### CvReview.java
- 简历评审领域模型
- 包含分数（0-1）和反馈信息

### 4. 工具类

#### StringLoader.java
- 从资源文件加载文本内容
- 支持从 classpath 加载文件

#### AgenticScopePrinter.java
- 格式化打印 AgenticScope 内容
- 支持截断长文本，便于调试

### 5. 主要依赖

- **Spring Boot Starter**: Spring Boot 应用支持
- **LangChain4j**: AI 框架核心（版本 1.10.0）
- **LangChain4j Agentic**: Agent 设计模式支持（版本 1.10.0-beta18）
- **LangChain4j Ollama**: Ollama 集成（包含聊天模型支持）
- **Lombok**: 代码简化工具（可选依赖）

## 🧪 运行示例

### 运行基础 Agent 示例

```bash
mvn exec:java -Dexec.mainClass="com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent._1a_Basic_Agent_Example"
```

### 运行顺序工作流示例

```bash
mvn exec:java -Dexec.mainClass="com.cnblogs.yjmyzz.langchain4j.study.agentic._2_sequential_workflow._2a_Sequential_Agent_Example"
```

### 运行循环工作流示例

```bash
mvn exec:java -Dexec.mainClass="com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow._3a_Loop_Agent_Example"
```

## 🔧 开发指南

### 创建新的 Agent

1. 定义 Agent 接口，使用 `@Agent` 注解描述 Agent 功能
2. 使用 `@SystemMessage` 定义系统提示词（可选）
3. 使用 `@UserMessage` 定义用户消息模板
4. 使用 `@V` 注解标记变量，用于在 AgenticScope 中传递
5. 使用 `AgenticServices.agentBuilder()` 创建 Agent 实例

**示例**:
```java
public interface MyAgent {
    @Agent("描述 Agent 的功能")
    @SystemMessage("系统提示词")
    @UserMessage("用户消息模板：{{variable}}")
    String doSomething(@V("variable") String input);
}

MyAgent agent = AgenticServices
        .agentBuilder(MyAgent.class)
        .chatModel(model)
        .outputKey("result")
        .build();
```

### 创建顺序工作流

使用 `AgenticServices.sequenceBuilder()` 创建顺序工作流：

```java
UntypedAgent sequence = AgenticServices
        .sequenceBuilder()
        .subAgents(agent1, agent2, agent3)
        .outputKey("finalResult")
        .build();
```

### 创建循环工作流

使用 `AgenticServices.loopBuilder()` 创建循环工作流：

```java
UntypedAgent loop = AgenticServices
        .loopBuilder()
        .subAgents(reviewer, optimizer)
        .outputKey("result")
        .exitCondition(scope -> {
            // 定义退出条件
            return conditionMet;
        })
        .maxIterations(5)  // 最大迭代次数，防止无限循环
        .build();
```

### AgenticScope 变量传递

- 使用 `@V` 注解标记方法参数，这些参数会自动添加到 AgenticScope
- 使用 `outputKey` 指定 Agent 的输出变量名
- 在顺序工作流中，前一个 Agent 的输出可以作为后一个 Agent 的输入
- 在循环工作流中，每次迭代都会更新 AgenticScope 中的变量

## 🐛 故障排除

### 常见问题

1. **Ollama 连接失败**
   - 确保 Ollama 服务已启动：`ollama serve`
   - 检查端口 11434 是否被占用
   - 验证模型是否已下载：`ollama list`
   - 确认使用的模型名称正确：`deepseek-v3.1:671b-cloud`

2. **Agent 执行失败**
   - 检查 Agent 接口定义是否正确
   - 确保 `@Agent`、`@UserMessage` 等注解使用正确
   - 验证 `@V` 注解的变量名与 `outputKey` 匹配
   - 检查输入参数是否正确传递

3. **循环工作流无限循环**
   - 检查退出条件是否正确实现
   - 确保 `maxIterations` 设置合理
   - 验证退出条件中的变量名是否正确

4. **模型响应缓慢**
   - 检查硬件资源（CPU、内存）
   - 考虑使用更小的模型
   - 调整超时配置（`ollama.timeout`）
   - 对于本地模型，考虑使用 GPU 加速

5. **变量传递失败**
   - 确保 `outputKey` 与下一个 Agent 的输入变量名匹配
   - 检查 `@V` 注解的变量名是否正确
   - 在顺序工作流中，确保变量名在 Agent 之间一致

6. **Java 25 兼容性**
   - 项目使用 Java 25，确保已安装 JDK 25
   - Maven 编译器插件设置为 Java 25
   - Lombok 为可选依赖，打包时会被排除

## 📝 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 📞 联系方式

如有问题，请通过以下方式联系：
- 提交 GitHub Issue: https://github.com/yjmyzz/agent-design-pattern-with-langchaing4j/issues
- 作者博客: http://yjmyzz.cnblogs.com
- 作者: 菩提树下的杨过

## 🙏 致谢

感谢 [LangChain4j](https://github.com/langchain4j/langchain4j) 开源项目提供的强大支持！

特别感谢以下官方文档资源：
- [LangChain4j 中文文档](https://docs.langchain4j.info/) - 为 Java 应用赋能大模型能力的官方中文指南
- [LangChain4j 英文文档](https://docs.langchain4j.dev/) - 官方英文文档，提供完整的技术参考
- [LangChain4j Examples](https://github.com/langchain4j/langchain4j-examples) - 官方示例代码库
- [Building effective agents](https://www.anthropic.com/engineering/building-effective-agents) - Anthropic 构造有效的智能体
- [[译] AI Workflow & AI Agent：架构、模式与工程建议（Anthropic，2024）](http://arthurchiao.art/blog/build-effective-ai-agent-zh/) - (中文翻译)Anthropic 构造有效的智能体
- [Ollama 官网](https://ollama.ai/) - 本地大语言模型运行环境

## ⚠️ 重要说明

### Java 25 兼容性

项目使用 Java 25 和 Spring Boot 4.0.0 进行开发：

- **Java 25**: 确保已安装 JDK 25
- **Maven 配置**: 编译器源码和目标版本都设置为 25
- **Lombok**: 作为可选依赖，打包时会被排除

### Agent 设计模式说明

项目演示了如何使用 LangChain4j 的 Agentic 功能实现不同的 Agent 设计模式：

1. **基础 Agent**: 单个 Agent 的使用
   - 适合简单的单步任务
   - 如果只有一个 Agent，使用 AiService 会是更好的选择
   - Agent 只有与其他 Agent 结合使用时才更有用

2. **顺序工作流**: 多个 Agent 按顺序执行
   - 适合需要多步骤处理的任务
   - 前一个 Agent 的输出作为后一个 Agent 的输入
   - 使用 `sequenceBuilder()` 创建

3. **循环工作流**: Agent 循环执行直到满足退出条件
   - 适合需要迭代优化的任务
   - 可以设置退出条件和最大迭代次数
   - 使用 `loopBuilder()` 创建

4. **并行工作流**: 多个 Agent 并行执行
   - 适合可以并行处理的任务
   - 目录已创建，示例代码待实现

### 技术架构

- **Spring Boot**: 提供应用框架和依赖注入
- **LangChain4j**: 提供 AI 集成能力
  - `AgenticServices`: Agent 服务构建器
  - `@Agent`: Agent 描述注解
  - `@SystemMessage`: 系统消息注解
  - `@UserMessage`: 用户消息注解
  - `@V`: 变量注解，用于标记 AgenticScope 中的变量
  - `AgenticScope`: Agent 执行上下文，用于在 Agent 之间传递数据
  - `OllamaChatModel`: 聊天模型接口
- **Ollama**: 提供本地大语言模型服务

### 应用场景

- 简历生成和优化系统
- 多步骤 AI 任务处理
- 迭代优化任务
- 复杂的 AI 工作流编排

---

**注意**: 
- 请确保在使用前已正确安装和配置 Ollama 服务，并下载所需的模型
- Agent 执行可能需要较长时间，特别是生成长文本时
- 循环工作流设置了最大迭代次数以防止无限循环
- 项目中的示例文档位于 `src/main/resources/documents/` 目录
