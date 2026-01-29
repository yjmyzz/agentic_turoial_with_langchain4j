package com.cnblogs.yjmyzz.langchain4j.study.agentic._a_react;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/**
 * ReAct（Reasoning and Acting）智能体演示应用程序
 * 
 * 这个应用程序演示了AI智能体中的ReAct（推理与行动）设计模式。
 * ReAct模式结合了推理（Reasoning）和行动（Acting），允许AI智能体：
 * 1. 分析和理解用户请求
 * 2. 制定解决问题的策略
 * 3. 必要时使用外部工具获取信息
 * 4. 基于获取的信息形成最终答案
 * 
 * 核心组件：
 * - ReActAssistant：实现了ReAct模式的智能助手接口，具有系统消息指导其思考过程
 * - SampleTools：提供了一系列实用工具，如数学计算、时间查询、天气查询等
 * - AgenticServices：LangChain4j框架提供的智能体构建服务
 * 
 * 应用场景：
 * 适用于需要结合推理和工具调用来解决的复杂问题，如数学计算、
 * 信息查询、数据分析等需要多步思考和外部数据支持的任务。
 * 
 * 工作流程图：
 * 
 * +-------------------+
 * |   用户输入问题    |
 * +--------+----------+
 *          |
 *          v
 * +--------+----------+
 * |   ReAct智能体     |
 * |  - 理解问题      |
 * |  - 推理解决方案   |
 * |  - 决定是否需工具 |
 * +--------+----------+
 *          |
 *          v
 * +--------+----------+
 * |   调用工具        |
 * |  (如果需要)      |
 * +--------+----------+
 *          |
 *          v
 * +--------+----------+
 * |  获取工具结果     |
 * +--------+----------+
 *          |
 *          v
 * +--------+----------+
 * |  形成最终答案     |
 * |   并返回用户      |
 * +-------------------+
 * 
 * @author junmingyang
 */
@SpringBootApplication
public class ReActAgentApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);
        SampleTools sampleTools = context.getBean("sampleTools", SampleTools.class);

        ReActAssistant agent = AgenticServices
                .agentBuilder(ReActAssistant.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15))
                .tools(sampleTools)
                .build();


        String[] testQueries = {
                "计算 15 加上 27 等于多少？",
                "北京现在的天气怎么样？",
                "计算半径为5的圆的面积",
                "现在是几点？",
                "计算长方体的体积，长10，宽5，高3",
                "帮我算一下 (25 × 4) ÷ 2 等于多少？"
        };

        for (String query : testQueries) {
            System.out.println("问: " + query);
            try {
                String response = agent.chat(query);
                System.out.println("答: " + response);
                System.out.println("-".repeat(50));
                // 避免请求过快
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("查询失败: " + e.getMessage());
            }
        }

    }
}
