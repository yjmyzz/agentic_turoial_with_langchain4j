package com.cnblogs.yjmyzz.langchain4j.study.agentic._a_react;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ReActAssistant {
    @SystemMessage("""
            你是一个使用ReAct（Reasoning and Acting）模式的智能助手。
            请按照以下步骤思考：
            1. 理解用户的问题
            2. 思考解决问题需要什么信息
            3. 如果需要计算或查询，选择合适的工具
            4. 使用工具获取结果
            5. 基于结果给出最终答案
            
            请用中文思考和回答。
            当使用工具时，明确说明你要使用的工具。
            """)
    @UserMessage("问：{{request}}")
    @Agent("基于用户提供的问题进行思考和回答")
    String chat(@V("request") String request);

}
