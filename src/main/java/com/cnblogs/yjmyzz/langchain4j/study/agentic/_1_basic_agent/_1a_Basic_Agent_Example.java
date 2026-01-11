package com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/**
 该示例演示了如何实现一个基础Agent(改编自<a href="https://github.com/langchain4j/langchain4j-examples">langchain4j官网示例</a>)
 注意：Agent只有与其他Agent结合使用时才更有用，后续步骤中将展示这一点。
 如果只有一个Agent，使用 AiService 会是更好的选择。
 这个基础Agent将用户的个人简介转换成一个简洁而完整的简历。
 注意：运行此程序可能需要一些时间，因为输出的简历会相当长，LLM也需要一些时间处理。
 @author 菩提树下的杨过（yjmyzz.cnblogs.com)
 @see <a href="https://github.com/langchain4j/langchain4j-examples/blob/main/agentic-tutorial/src/main/java/_1_basic_agent/_1a_Basic_Agent_Example.java">_1a_Basic_Agent_Example</a>
 */
@SpringBootApplication
public class _1a_Basic_Agent_Example {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(model)
                //定义输出对象的键
                .outputKey("masterCv")
                .build();

        // 加载个人介绍
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");

        // 让 agent 生成简历
        String cv = cvGenerator.generateCv(lifeStory);

        // 打印简历
        System.out.println("=== 简历 ===");
        System.out.println(cv);
    }


}
