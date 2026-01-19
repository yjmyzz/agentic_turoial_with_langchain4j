package com.cnblogs.yjmyzz.langchain4j.study.agentic._7_supervisor_orchestration;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.V;

public interface HiringSupervisor {
//    @Agent("顶级招聘主管，协调候选人评估和决策流程。若候选人综合评分低于0.8，则拒绝，否则安排面试。")
    @Agent("顶级招聘主管，协调候选人评估和决策流程。")
    ResultWithAgenticScope<String> invoke(@V("request") String request, @V("supervisorContext") String supervisorContext);
}
