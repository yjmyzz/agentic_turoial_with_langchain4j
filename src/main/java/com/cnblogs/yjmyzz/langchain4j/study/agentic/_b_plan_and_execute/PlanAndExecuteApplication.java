package com.cnblogs.yjmyzz.langchain4j.study.agentic._b_plan_and_execute;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._a_react.ReActAssistant;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._a_react.SampleTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * PlanAndExecute模式演示应用程序
 * 
 * 这个应用程序演示了AI智能体中的Plan-and-Execute（规划并执行）设计模式。
 * 该模式将复杂任务分解为两个阶段：
 * 1. 规划阶段（Planning）：将用户任务分解为一系列具体的执行步骤
 * 2. 执行阶段（Executing）：按顺序执行规划好的步骤
 * 
 * 核心组件：
 * - Planner：负责将复杂任务分解为具体的执行步骤
 * - Executor：负责按步骤执行任务
 * - Coordinator：协调规划和执行过程，并管理上下文信息
 * 
 * 应用场景：
 * 适用于需要多步推理和工具调用的复杂任务，如数学计算、天气查询、
 * 时间获取等需要多个步骤才能完成的任务。
 * 
 * @author junmingyang
 */
@SpringBootApplication
public class PlanAndExecuteApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);
        SampleTools sampleTools = context.getBean("sampleTools", SampleTools.class);

        String[] testTasks = {
                "计算 15 加上 27 等于多少？",
                "北京现在的天气怎么样？",
                "计算半径为5的圆的面积",
                "现在是几点？",
                "计算长方体的体积，长10，宽5，高3",
                "帮我算一下 (25 × 4) ÷ 2 等于多少？"
        };

        Coordinator coordinator = new Coordinator(model, sampleTools);

        for (int i = 0; i < testTasks.length; i++) {
            System.out.printf("\n📦 测试用例 %d/%d%n", i + 1, testTasks.length);

            Map<String, Object> result = coordinator.executeTask(testTasks[i]);

            // 打印总结
            System.out.println("\n✅ 任务完成总结:");
            System.out.println("-".repeat(40));
            System.out.println("任务: " + result.get("task"));
            System.out.println("状态: " + result.get("status"));
            System.out.println("耗时: " + calculateDuration(
                    (String) result.get("start_time"),
                    (String) result.get("end_time")
            ));

            if (result.containsKey("execution_results")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> executions =
                        (List<Map<String, Object>>) result.get("execution_results");
                System.out.println("执行步骤数: " + executions.size());
            }

            System.out.println("=".repeat(60));

            // 任务间暂停
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        coordinator.printContext();
    }


    private static String calculateDuration(String start, String end) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            Duration duration = Duration.between(startTime, endTime);
            return String.format("%d秒", duration.getSeconds());
        } catch (Exception e) {
            return "未知";
        }
    }
}
