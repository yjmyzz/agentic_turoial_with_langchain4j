package com.cnblogs.yjmyzz.langchain4j.study.agentic._b_plan_and_execute;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Planner {

    @SystemMessage("""
           你是一个任务规划专家。请将用户的任务分解为详细的执行步骤。

           输出格式必须是严格的JSON格式：
           {
               "plan_name": "任务名称",
               "steps": [
                   {
                       "step_number": 1,
                       "description": "步骤描述",
                       "tool": "使用的工具名称",
                       "parameters": {"参数名": "参数值"}
                   }
               ],
               "expected_output": "预期输出"
           }

           可用工具列表：
           1. add - 两数相加
           2. getWeather - 天气查询
           3. calculateCircleArea - 计算圆的面积
           4. getCurrentDateTime - 获取当前时间
           5. calculateCuboidVolume - 计算长方体体积
           6. multiply - 两数相乘
           7. divide - 两数相除
          """)
    @UserMessage("问：{{request}}")
    @Agent("基于用户提供的问题生成计划")
    String createPlan(@V("request") String request);

//    @SystemMessage("""
//           你是一个任务规划专家。请将用户的任务分解为详细的执行步骤。
//
//           输出格式必须是严格的JSON格式：
//           {
//               "plan_name": "任务名称",
//               "steps": [
//                   {
//                       "step_number": 1,
//                       "description": "步骤描述",
//                       "tool": "使用的工具名称",
//                       "parameters": {"参数名": "参数值"}
//                   }
//               ],
//               "expected_output": "预期输出"
//           }
//
//            请确保每个步骤都明确指定工具和参数。
//          """)
//    @UserMessage("问：{{request}}")
//    @Agent("基于用户提供的问题生成计划")
//    String createPlan(@V("request") String request);
}
