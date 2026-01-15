package com.cnblogs.yjmyzz.langchain4j.study.agentic._2_sequential_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent.CvGenerator;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
 * by 菩提树下的杨过（yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _2a_Sequential_Agent_Example {

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
        String instructions = "根据以下职位描述优化简历。" + StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 由于使用的是无类型Agent，需要传递参数映射
        Map<String, Object> arguments = Map.of(
                // 匹配 CvGenerator.java 中的变量名
                "lifeStory", lifeStory,
                // 匹配 CvTailor.java 中的变量名
                "instructions", instructions
        );

        // 调用合成 Agent 生成优化后的简历
        String tailoredCv = (String) tailoredCvGenerator.invoke(arguments);

        // 打印生成的简历
        System.out.println("=== 优化后的简历（无类型代理）===");
        System.out.println((String) tailoredCv);
        // 可以尝试，当使用job_description_fullstack.txt 作为输入时，
        // 生成的简历会有很大不同
    }
}
