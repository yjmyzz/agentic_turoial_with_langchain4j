package com.cnblogs.yjmyzz.langchain4j.study;

import dev.langchain4j.chain.Chain;
import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * langchain4j学习项目主启动类
 *
 * @author 菩提树下的杨过
 * @version 1.0.0
 */
@SpringBootApplication
public class AgentDesignPatternApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);
        promptChain(model);
    }

    static void promptChain(ChatModel model) {
        Chain<String, String> chain = ConversationalChain.builder().chatModel(model).build();
        String answer = chain.execute("1加1等于几？");
        System.out.printf("answer : %s\n", answer);
        answer = chain.execute("再加2呢");
        System.out.printf("answer : %s\n", answer);
    }
} 