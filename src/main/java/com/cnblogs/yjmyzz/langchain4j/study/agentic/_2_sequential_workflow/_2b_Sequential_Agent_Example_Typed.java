package com.cnblogs.yjmyzz.langchain4j.study.agentic._2_sequential_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent.CvGenerator;
import com.cnblogs.yjmyzz.langchain4j.study.util.AgenticScopePrinter;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Map;

/**
 * 【顺序工作流示例】(改编自<a href="https://github.com/langchain4j/langchain4j-examples">langchain4j官网示例</a>)
 * 该示例演示了如何实现两个Agent：
 * - CvGenerator（接收个人简介并生成一份完整的主简历）
 * - CvTailor（接收原始简历并根据特定指令（职位描述、反馈等）进行定制）
 * 然后使用 sequenceBuilder 按顺序工作流依次调用它们，
 * 并演示如何在它们之间传递参数。
 * 当组合多个Agent时，所有输入、中间和输出参数以及调用链都存储在
 * AgenticScope 中，可供高级用例访问。
 */
public class _2b_Sequential_Agent_Example_Typed {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);


        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(model)
                .outputKey("masterCv")
                // 如果要将此变量从agent-1传递到agent-2，
                // 请确保此处的outputKey与第二个agent接口中指定的输入变量名匹配
                // 对应 CvTailor.java 文件中的变量名
                .build();


        CvTailor cvTailor = AgenticServices
                .agentBuilder(CvTailor.class)
                // 注意：可以为不同agent使用不同的模型
                .chatModel(model)
                // 定义输出对象的键
                // 如果在这里使用 "masterCv"，原始的主简历会被第二个agent覆盖
                .outputKey("tailoredCv")
                .build();

        UntypedAgent tailoredCvGenerator = AgenticServices
                .sequenceBuilder()
                // 可以包含任意多个代理，顺序很重要
                .subAgents(cvGenerator, cvTailor)
                // 这是合成Agent的最终输出
                // 注意：可以使用 AgenticScope 中的任何字段作为输出
                // 例如，可以输出 'masterCv' 而不是 tailoredCv（尽管在本例中没有意义）
                .outputKey("tailoredCv")
                .build();

        // 从 resources/documents/ 目录下的文本文件加载参数
        // - user_life_story.txt
        // - job_description_backend.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String instructions = "根据以下职位描述优化简历。" + StringLoader.loadFromResource("/documents/job_description_fullstack.txt");

        SequenceCvGenerator sequenceCvGenerator = AgenticServices
                //这里指定类型化接口
                .sequenceBuilder(SequenceCvGenerator.class)
                .subAgents(cvGenerator, cvTailor)
                .outputKey("bothCvsAndLifeStory")
                .output(agenticScope -> {
                    // 可以使用任何方法，本示例中用Map收集一些内部变量
                    return Map.of(
                            "lifeStory", agenticScope.readState("lifeStory", ""),
                            "masterCv", agenticScope.readState("masterCv", ""),
                            "tailoredCv", agenticScope.readState("tailoredCv", "")
                    );
                })
                .build();

        // 调用类型化的组合 agent
        ResultWithAgenticScope<Map<String,String>> bothCvsAndScope = sequenceCvGenerator.generateTailoredCv(lifeStory, instructions);

        // 提取结果和 agenticScope
        AgenticScope agenticScope = bothCvsAndScope.agenticScope();
        Map<String,String> bothCvsAndLifeStory = bothCvsAndScope.result();

        System.out.println("=== 用户信息（输入） ===");
        String userStory = bothCvsAndLifeStory.get("lifeStory");
        System.out.println(userStory.length() > 100 ? userStory.substring(0, 300) + " [truncated...]" : lifeStory);
        System.out.println("=== 原始简历（结构化）（中间变量） ===");
        String masterCv = bothCvsAndLifeStory.get("masterCv");
        System.out.println(masterCv.length() > 100 ? masterCv.substring(0, 300) + " [truncated...]" : masterCv);
        System.out.println("=== 定制简历（结构化）（输出） ===");
        String tailoredCv = bothCvsAndLifeStory.get("tailoredCv");
        System.out.println(tailoredCv.length() > 100 ? tailoredCv.substring(0, 300) + " [truncated...]" : tailoredCv);

        // 非结构化和结构化的 agent 会给出相同的定制简历结果
        // （如果有差异，也是源于 LLM 自身的非幂等性）
        // 但结构的 agent 使用起来更优雅，并且因为编译时类型检查而更安全

        System.out.println("=== AGENTIC 作用域 ===");
        System.out.println(AgenticScopePrinter.printPretty(agenticScope, 300));
        // 返回结果示例:
        // AgenticScope {
        //     memoryId = "e705028d-e90e-47df-9709-95953e84878c",
        //             state = {
        //                     bothCvsAndLifeStory = { // output
        //                             masterCv = "...",
        //                            lifeStory = "...",
        //                            tailoredCv = "..."
        //                     },
        //                     instructions = "...", // inputs and intermediary variables
        //                     tailoredCv = "...",
        //                     masterCv = "...",
        //                     lifeStory = "..."
        //             }
        // }
        System.out.println("=== 上下文作为对话（对话中的所有消息） ===");
        System.out.println(AgenticScopePrinter.printConversation(agenticScope.contextAsConversation(), 300));
    }
}
