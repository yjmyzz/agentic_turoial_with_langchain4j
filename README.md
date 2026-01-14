# Agent设计模式学习项目 - 基于LangChain4j实现

这是一个用于学习LangChain4j Agent设计模式的Spring Boot项目，集成了本地Ollama服务，演示了如何使用LangChain4j的Agentic功能实现各种Agent工作流模式。

**Package**: `com.cnblogs.yjmyzz.langchain4j.study`

## 🚀 项目特性

- **Java 25**: 使用最新的Java版本
- **Spring Boot 4.0.0**: 现代化的Spring Boot框架
- **LangChain4j 1.10.0**: 强大的Java AI框架
- **LangChain4j Agentic 1.10.0-beta18**: Agent设计模式支持
- **Ollama集成**: 支持本地大语言模型和嵌入模型
  - 聊天模型：默认使用 `deepseek-v3.1:671b-cloud`
  - 嵌入模型：默认使用 `nomic-embed-text:latest`
- **5种Agent工作流模式**: 完整演示Agent设计模式的各种应用场景
  - 基础Agent（Basic Agent）
  - 顺序工作流（Sequential Workflow）
  - 循环工作流（Loop Workflow）
  - 并行工作流（Parallel Workflow）
  - 条件工作流（Conditional Workflow）

## 📋 前置要求

1. **Java 25**: 确保已安装JDK 25
2. **Maven 3.6+**: 确保已安装Maven
3. **Ollama**: 确保已安装并启动Ollama服务

## 🛠️ 安装和配置

### 1. 安装Ollama

访问 [Ollama官网](https://ollama.ai/) 下载并安装Ollama。

### 2. 启动Ollama服务

```bash
# 启动Ollama服务
ollama serve
```

### 3. 下载模型

```bash
# 下载聊天模型（默认模型）
ollama pull deepseek-v3.1:671b-cloud

# 下载嵌入模型（用于RAG功能）
ollama pull nomic-embed-text:latest

# 或者下载其他模型
ollama pull qwen3:0.6b
ollama pull llama2
```

### 4. 克隆项目

```bash
git clone <repository-url>
cd agentic_tutorial
```

### 5. 编译项目

```bash
mvn clean compile
```

### 6. 运行项目

```bash
mvn spring-boot:run
```

## 📚 Agent工作流模式详解

### 1. 基础Agent（Basic Agent）

**示例文件**: `_1a_Basic_Agent_Example.java`

最简单的Agent模式，演示如何创建一个独立的Agent。

**功能**: 将用户的个人简介转换成一份完整规范的简历

**关键特性**:
- 使用 `@Agent` 注解定义Agent行为
- 使用 `@UserMessage` 定义用户消息模板
- 使用 `@V` 注解标记变量
- 通过 `outputKey` 指定输出结果在AgenticScope中的键名

**运行示例**:
```java
CvGenerator cvGenerator = AgenticServices
    .agentBuilder(CvGenerator.class)
    .chatModel(model)
    .outputKey("masterCv")
    .build();

String cv = cvGenerator.generateCv(lifeStory);
```

### 2. 顺序工作流（Sequential Workflow）

**示例文件**: `_2a_Sequential_Agent_Example.java`

演示如何将多个Agent按顺序连接，前一个Agent的输出作为后一个Agent的输入。

**功能**: 
- `CvGenerator`: 生成主简历
- `CvTailor`: 根据职位描述定制简历

**关键特性**:
- 使用 `sequenceBuilder()` 创建顺序工作流
- Agent之间通过 `outputKey` 和输入变量名匹配传递数据
- 支持类型化Agent和无类型Agent（UntypedAgent）

**运行示例**:
```java
UntypedAgent tailoredCvGenerator = AgenticServices
    .sequenceBuilder()
    .subAgents(cvGenerator, cvTailor)
    .outputKey("tailoredCv")
    .build();
```

### 3. 循环工作流（Loop Workflow）

**示例文件**: `_3a_Loop_Agent_Example.java`

演示如何实现迭代优化，直到满足退出条件。

**功能**:
- `CvReviewer`: 评审简历并给出分数和反馈
- `ScoredCvTailor`: 根据反馈优化简历
- 循环执行直到分数达到阈值（>0.8）

**关键特性**:
- 使用 `loopBuilder()` 创建循环工作流
- 通过 `exitCondition()` 定义退出条件
- 使用 `maxIterations()` 防止无限循环
- 每次迭代后检查退出条件

**运行示例**:
```java
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

**示例文件**: `_4_Parallel_Workflow_Example.java`

演示如何让多个Agent并行执行，提高效率。

**功能**: 三个评审者同时评审简历
- `HrCvReviewer`: HR角度评审
- `ManagerCvReviewer`: 经理角度评审
- `TeamMemberCvReviewer`: 团队成员角度评审

**关键特性**:
- 使用 `parallelBuilder()` 创建并行工作流
- 支持自定义线程池执行器
- 通过 `output()` 方法聚合多个Agent的结果
- 所有Agent并行执行，提高效率

**运行示例**:
```java
UntypedAgent cvReviewGenerator = AgenticServices
    .parallelBuilder()
    .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer)
    .executor(executor)
    .outputKey("fullCvReview")
    .output(agenticScope -> {
        // 聚合多个评审结果
        CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
        CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
        CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
        // 返回汇总结果
        return aggregateReviews(hrReview, managerReview, teamMemberReview);
    })
    .build();
```

### 5. 条件工作流（Conditional Workflow）

**示例文件**: `_5a_Conditional_Workflow_Example.java`

演示如何根据条件选择执行不同的Agent分支。

**功能**: 根据简历评审分数决定下一步操作
- 分数 >= 0.8: 调用 `InterviewOrganizer` 准备面试
- 分数 < 0.8: 调用 `EmailAssistant` 发送拒绝邮件

**关键特性**:
- 使用 `conditionalBuilder()` 创建条件工作流
- 通过Lambda表达式定义条件
- 支持工具（Tools）和RAG（检索增强生成）
- 条件按顺序检查，第一个满足的条件会被执行

**运行示例**:
```java
UntypedAgent candidateResponder = AgenticServices
    .conditionalBuilder()
    .subAgents(
        agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score >= 0.8, 
        interviewOrganizer
    )
    .subAgents(
        agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score < 0.8, 
        emailAssistant
    )
    .build();
```

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/cnblogs/yjmyzz/langchain4j/study/
│   │   ├── AgentDesignPatternApplication.java    # 主启动类
│   │   ├── config/
│   │   │   └── OllamaConfig.java                # Ollama配置类
│   │   ├── agentic/
│   │   │   ├── _1_basic_agent/                  # 基础Agent示例
│   │   │   │   ├── _1a_Basic_Agent_Example.java
│   │   │   │   ├── _1b_Basic_Agent_Example_Structured.java
│   │   │   │   ├── CvGenerator.java
│   │   │   │   └── CvGeneratorStructuredOutput.java
│   │   │   ├── _2_sequential_workflow/          # 顺序工作流示例
│   │   │   │   ├── _2a_Sequential_Agent_Example.java
│   │   │   │   ├── _2b_Sequential_Agent_Example_Typed.java
│   │   │   │   ├── CvTailor.java
│   │   │   │   └── SequenceCvGenerator.java
│   │   │   ├── _3_loop_workflow/                # 循环工作流示例
│   │   │   │   ├── _3a_Loop_Agent_Example.java
│   │   │   │   ├── _3a_Loop_Agent_Example_States_And_Fail.java
│   │   │   │   ├── CvReviewer.java
│   │   │   │   └── ScoredCvTailor.java
│   │   │   ├── _4_parallel_workflow/           # 并行工作流示例
│   │   │   │   ├── _4_Parallel_Workflow_Example.java
│   │   │   │   ├── HrCvReviewer.java
│   │   │   │   ├── ManagerCvReviewer.java
│   │   │   │   └── TeamMemberCvReviewer.java
│   │   │   └── _5_conditional_workflow/        # 条件工作流示例
│   │   │       ├── _5a_Conditional_Workflow_Example.java
│   │   │       ├── _5b_Conditional_Workflow_Example_Async.java
│   │   │       ├── EmailAssistant.java
│   │   │       ├── InfoRequester.java
│   │   │       ├── InterviewOrganizer.java
│   │   │       ├── OrganizingTools.java
│   │   │       └── RagProvider.java
│   │   ├── domain/
│   │   │   ├── Cv.java                          # 简历领域模型
│   │   │   └── CvReview.java                    # 简历评审领域模型
│   │   └── util/
│   │       ├── AgenticScopePrinter.java         # AgenticScope打印工具
│   │       └── StringLoader.java                # 资源文件加载工具
│   └── resources/
│       ├── application.yml                      # 应用配置
│       └── documents/                           # 示例文档
│           ├── user_life_story.txt              # 用户个人经历
│           ├── master_cv.txt                    # 主简历
│           ├── tailored_cv.txt                  # 定制简历
│           ├── job_description_backend.txt      # 后端职位描述
│           ├── job_description_fullstack.txt    # 全栈职位描述
│           ├── hr_requirements.txt              # HR要求
│           ├── phone_interview_notes.txt        # 电话面试记录
│           ├── candidate_contact.txt            # 候选人联系方式
│           └── house_rules.txt                  # 公司规则（用于RAG）
└── test/
    └── java/
```

## ⚙️ 配置说明

项目配置文件位于 `src/main/resources/application.yml`：

```yaml
# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /

# Spring应用配置
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
  embedding-model: nomic-embed-text:latest  # 嵌入模型名称
  timeout: 60                               # 请求超时时间（秒）

# 应用信息
info:
  app:
    name: langchain4j Study
    version: 1.0.0
    description: langchain4j学习项目 - Agent设计模式示例
```

## 🔧 核心组件说明

### 1. 配置类

#### OllamaConfig.java
- 配置Ollama聊天模型
- 支持自定义模型名称、服务地址和超时时间
- 启用请求和响应日志记录
- 使用 `@Bean` 注解注册为Spring Bean

### 2. Agent接口定义

#### CvGenerator.java
- 基础Agent接口，用于生成简历
- 使用 `@Agent` 注解定义Agent描述
- 使用 `@UserMessage` 定义消息模板
- 使用 `@V` 注解标记变量

#### CvTailor.java
- 定制简历的Agent接口
- 接收主简历和定制指令，生成定制简历

#### CvReviewer.java
- 评审简历的Agent接口
- 返回结构化的 `CvReview` 对象（包含分数和反馈）

### 3. 领域模型

#### Cv.java
- 简历领域模型
- 包含技能、专业经历、教育背景等字段
- 使用 `@Description` 注解描述字段

#### CvReview.java
- 简历评审领域模型
- 包含分数（0-1）和反馈信息

### 4. 工具类

#### StringLoader.java
- 从资源文件加载文本内容
- 支持从classpath加载文件

#### AgenticScopePrinter.java
- 格式化打印AgenticScope内容
- 用于调试和观察Agent执行状态

## 🧪 运行示例

### 运行基础Agent示例

```bash
# 直接运行主类
java -cp target/classes com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent._1a_Basic_Agent_Example
```

### 运行顺序工作流示例

```bash
java -cp target/classes com.cnblogs.yjmyzz.langchain4j.study.agentic._2_sequential_workflow._2a_Sequential_Agent_Example
```

### 运行循环工作流示例

```bash
java -cp target/classes com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow._3a_Loop_Agent_Example
```

### 运行并行工作流示例

```bash
java -cp target/classes com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow._4_Parallel_Workflow_Example
```

### 运行条件工作流示例

```bash
java -cp target/classes com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow._5a_Conditional_Workflow_Example
```

## 🔧 开发指南

### 创建新的Agent

1. 定义Agent接口，使用 `@Agent` 注解描述Agent功能
2. 使用 `@UserMessage` 定义消息模板
3. 使用 `@V` 注解标记变量
4. 使用 `AgenticServices.agentBuilder()` 创建Agent实例

**示例**：
```java
public interface MyAgent {
    @Agent("描述Agent的功能")
    @UserMessage("消息模板，使用{{variableName}}引用变量")
    String doSomething(@V("variableName") String input);
}

MyAgent agent = AgenticServices
    .agentBuilder(MyAgent.class)
    .chatModel(model)
    .outputKey("result")
    .build();
```

### 创建结构化输出Agent

使用 `@StructuredOutput` 注解和领域模型类：

```java
public interface StructuredAgent {
    @Agent("生成结构化数据")
    @StructuredOutput(Cv.class)
    Cv generateCv(@V("lifeStory") String input);
}
```

### 组合多个Agent

#### 顺序工作流
```java
UntypedAgent composite = AgenticServices
    .sequenceBuilder()
    .subAgents(agent1, agent2, agent3)
    .outputKey("finalResult")
    .build();
```

#### 循环工作流
```java
UntypedAgent loop = AgenticServices
    .loopBuilder()
    .subAgents(agent1, agent2)
    .outputKey("result")
    .exitCondition(scope -> {
        // 定义退出条件
        return conditionMet(scope);
    })
    .maxIterations(5)
    .build();
```

#### 并行工作流
```java
UntypedAgent parallel = AgenticServices
    .parallelBuilder()
    .subAgents(agent1, agent2, agent3)
    .outputKey("result")
    .output(scope -> {
        // 聚合结果
        return aggregate(scope);
    })
    .build();
```

#### 条件工作流
```java
UntypedAgent conditional = AgenticServices
    .conditionalBuilder()
    .subAgents(
        scope -> condition1(scope), agent1,
        scope -> condition2(scope), agent2
    )
    .build();
```

### 使用工具（Tools）

Agent可以使用工具扩展功能：

```java
public class MyTools {
    @Tool("工具描述")
    public String toolMethod(String param) {
        // 工具实现
        return result;
    }
}

MyAgent agent = AgenticServices
    .agentBuilder(MyAgent.class)
    .chatModel(model)
    .tools(new MyTools())
    .build();
```

### 使用RAG（检索增强生成）

为Agent添加RAG能力：

```java
MyAgent agent = AgenticServices
    .agentBuilder(MyAgent.class)
    .chatModel(model)
    .contentRetriever(retriever)
    .build();
```

## 🐛 故障排除

### 常见问题

1. **Ollama连接失败**
   - 确保Ollama服务已启动：`ollama serve`
   - 检查端口11434是否被占用
   - 验证模型是否已下载：`ollama list`
   - 确认使用的模型名称正确

2. **Agent执行失败**
   - 检查Agent接口定义是否正确
   - 确认 `outputKey` 和输入变量名匹配
   - 验证AgenticScope中的状态是否正确

3. **循环工作流无限循环**
   - 检查退出条件是否正确
   - 确认 `maxIterations` 设置合理
   - 验证退出条件中的状态读取是否正确

4. **并行工作流结果聚合错误**
   - 检查 `output()` 方法中的状态读取
   - 确认所有Agent的 `outputKey` 设置正确
   - 验证聚合逻辑是否正确

5. **条件工作流条件不匹配**
   - 检查条件Lambda表达式
   - 确认AgenticScope中的状态键名正确
   - 验证条件判断逻辑

6. **模型响应缓慢**
   - 检查硬件资源（CPU、内存）
   - 考虑使用更小的模型
   - 调整超时配置（`ollama.timeout`）
   - 对于本地模型，考虑使用GPU加速

7. **Java 25 兼容性**
   - 项目使用 Java 25，确保已安装 JDK 25
   - Maven编译器插件设置为Java 25
   - Lombok为可选依赖，打包时会被排除

## 📝 AgenticScope说明

`AgenticScope` 是Agent工作流中的核心概念，用于：
- 存储Agent之间的共享状态
- 传递Agent的输入和输出
- 跟踪工作流执行过程

**关键方法**：
- `readState(key)`: 读取状态值
- `state()`: 获取所有状态
- `memoryId()`: 获取记忆ID

## 📦 主要依赖

- **Spring Boot Starter**: Web应用支持
- **LangChain4j Core**: AI框架核心（版本 1.10.0）
- **LangChain4j Agentic**: Agent设计模式支持（版本 1.10.0-beta18）
- **LangChain4j Ollama**: Ollama集成
- **LangChain4j Embeddings BGE**: 嵌入模型支持
- **Lombok**: 代码简化工具（可选依赖）

## 📚 参考资源

### 官方文档
- [LangChain4j 中文文档](https://docs.langchain4j.info/) - 为Java应用赋能大模型能力的官方中文指南
- [LangChain4j 英文文档](https://docs.langchain4j.dev/) - 官方英文文档，提供完整的技术参考
- [LangChain4j Agentic教程](https://docs.langchain4j.dev/tutorials/agents/) - Agent设计模式官方教程
- [LangChain4j示例代码](https://github.com/langchain4j/langchain4j-examples) - 官方示例代码库

### 参考文章
- [Building Effective Agents](https://www.anthropic.com/engineering/building-effective-agents) - Anthropic关于构建有效Agent的文章
- [构建有效的AI Agent（中文翻译）](http://arthurchiao.art/blog/build-effective-ai-agent-zh/) - 中文翻译版本
- [Ollama官网](https://ollama.ai/) - 本地大语言模型运行环境

## 📝 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 📞 联系方式

如有问题，请通过以下方式联系：
- 提交GitHub Issue
- 作者博客: http://yjmyzz.cnblogs.com
- 作者: 菩提树下的杨过

## 🙏 致谢

感谢 [LangChain4j](https://github.com/langchain4j/langchain4j) 开源项目提供的强大支持！

特别感谢以下资源：
- LangChain4j官方团队提供的优秀框架和文档
- Anthropic关于Agent设计模式的理论指导
- Ollama提供的本地大语言模型运行环境

---

**注意**: 
- 请确保在使用前已正确安装和配置Ollama服务，并下载所需的模型
- Agent工作流中的状态管理是核心，需要仔细设计 `outputKey` 和变量名
- 循环工作流需要设置合理的退出条件和最大迭代次数，避免无限循环
- 并行工作流可以提高效率，但需要注意结果聚合的正确性
- 条件工作流中的条件按顺序检查，第一个满足的条件会被执行
