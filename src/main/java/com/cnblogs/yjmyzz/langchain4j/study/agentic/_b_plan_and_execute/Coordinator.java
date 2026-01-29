package com.cnblogs.yjmyzz.langchain4j.study.agentic._b_plan_and_execute;

import com.cnblogs.yjmyzz.langchain4j.study.agentic._a_react.SampleTools;
import com.cnblogs.yjmyzz.langchain4j.study.util.MurmurHash;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author junmingyang
 */
public class Coordinator {

    private final Planner planner;
    private final Executor executor;
    private final SampleTools tools;
    private final Map<String, Object> context;


    public Coordinator(ChatModel model, SampleTools tools) {

        this.tools = tools;
        this.context = new HashMap<>();

        //创建规划器
        this.planner = AgenticServices.agentBuilder(Planner.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15))
                // 如果明确知道工具的列表，可以显式提供，这里就无需再绑定,以减少token使用
                //.tools(this.tools)
                .build();

        //创建执行器
        this.executor = AgenticServices.agentBuilder(Executor.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15))
                .tools(this.tools)
                .build();
    }


    public Map<String, Object> executeTask(String task) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 任务: " + task);
        System.out.println("=".repeat(80));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", task);
        result.put("start_time", LocalDateTime.now().toString());

        try {
            // 阶段1: 规划
            System.out.println("\n📋 阶段1: 任务规划");
            System.out.println("-".repeat(40));
            String planJson = planner.createPlan(task);
            System.out.println("生成的计划:\n" + planJson);

            // 解析计划（简化版，实际应该使用JSON解析）
            List<Map<String, String>> steps = parsePlan(planJson);
            result.put("plan", steps);

            // 阶段2: 执行
            System.out.println("\n⚡ 阶段2: 执行计划");
            System.out.println("-".repeat(40));

            List<Map<String, Object>> executionResults = new ArrayList<>();

            for (int i = 0; i < steps.size(); i++) {
                Map<String, String> step = steps.get(i);
                System.out.printf("\n📝 步骤 %d/%d: %s%n",
                        i + 1, steps.size(), step.get("description"));

                // 构建步骤指令
                String stepInstruction = buildStepInstruction(step);

                // 执行步骤
                String stepResult = executor.executeStep(stepInstruction);
                System.out.println("执行结果:\n" + stepResult);

                // 保存结果
                Map<String, Object> stepResultMap = new HashMap<>();
                stepResultMap.put("step_number", i + 1);
                stepResultMap.put("description", step.get("description"));
                stepResultMap.put("tool", step.get("tool"));
                stepResultMap.put("result", stepResult);
                executionResults.add(stepResultMap);

                // 更新上下文
                updateContext(task, step, stepResult);

                // 短暂暂停，避免过快执行
                Thread.sleep(1000);
            }

            result.put("execution_results", executionResults);
            result.put("status", "completed");

        } catch (Exception e) {
            System.err.println("❌ 任务执行失败: " + e.getMessage());
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        result.put("end_time", LocalDateTime.now().toString());
        return result;
    }

    private List<Map<String, String>> parsePlan(String planJson) {
        // 简化的计划解析（实际应用中应该使用完整的JSON解析）
        List<Map<String, String>> steps = new ArrayList<>();

        // 使用正则表达式提取步骤信息
        Pattern stepPattern = Pattern.compile(
                "\"step_number\":\\s*(\\d+).*?" +
                        "\"description\":\\s*\"([^\"]+)\".*?" +
                        "\"tool\":\\s*\"([^\"]+)\"",
                Pattern.DOTALL
        );

        Matcher matcher = stepPattern.matcher(planJson);
        while (matcher.find()) {
            Map<String, String> step = new HashMap<>();
            step.put("step_number", matcher.group(1));
            step.put("description", matcher.group(2));
            step.put("tool", matcher.group(3));
            steps.add(step);
        }

        // 如果没有匹配到，创建默认步骤
        if (steps.isEmpty()) {
            Map<String, String> defaultStep = new HashMap<>();
            defaultStep.put("step_number", "1");
            defaultStep.put("description", "执行任务: " + planJson);
            defaultStep.put("tool", "analyzeText");
            steps.add(defaultStep);
        }

        return steps;
    }

    private String buildStepInstruction(Map<String, String> step) {
        return String.format(
                "执行步骤 %s:\n" +
                        "描述: %s\n" +
                        "工具: %s\n" +
                        "请使用指定工具完成此步骤。",
                step.get("step_number"),
                step.get("description"),
                step.get("tool")
        );
    }

    private void updateContext(String task, Map<String, String> step, String result) {
        // 将步骤结果存入上下文，供后续步骤使用（可选)
        String key = MurmurHash.murmur3_32Hash(task) + "_step_" + step.get("step_number") + "_result";
        context.put(key, result);
    }

    public void printContext() {
        System.out.println("-".repeat(50) + "\n上下文: ");
        context.forEach((key, value) -> System.out.println(key + " => \n" + value + "\n" + "-".repeat(30)));
    }
}
