package com.cnblogs.yjmyzz.langchain4j.study.agentic._b_plan_and_execute;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * @author junmingyang
 */
public interface Executor {

    @SystemMessage("""
            你是一个任务执行器。根据给定的步骤执行任务。
            
            每次只执行一个步骤，然后等待下一个指令。
            执行完成后，报告结果和下一步建议。
            
            输出格式：
            步骤 {n}: [工具名称]
            输入: {参数}
            输出: {结果}
            状态: [成功/失败]
            
            如果步骤失败，请说明原因和建议。
            """)
    @UserMessage("{{step}}")
    @Agent("基于用户提供的问题生成计划")
    String executeStep(@V("step") String step);
}
