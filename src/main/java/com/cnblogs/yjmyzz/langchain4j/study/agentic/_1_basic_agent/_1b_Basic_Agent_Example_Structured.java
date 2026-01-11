package com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.domain.Cv;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class _1b_Basic_Agent_Example_Structured {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        CvGeneratorStructuredOutput cvGenerator = AgenticServices
                .agentBuilder(CvGeneratorStructuredOutput.class)
                .chatModel(model)
                .build();

        // 加载个人介绍
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");

        // 让 agent 生成简历
        Cv cvStructured = cvGenerator.generateCv(lifeStory);

        // 打印简历
        System.out.println("=== 简历 ===");
        System.out.println(cvStructured);
    }
}
