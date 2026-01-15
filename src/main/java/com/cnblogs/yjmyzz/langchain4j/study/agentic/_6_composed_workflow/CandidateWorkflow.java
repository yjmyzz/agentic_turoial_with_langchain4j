package com.cnblogs.yjmyzz.langchain4j.study.agentic._6_composed_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;


public interface CandidateWorkflow {
    @Agent("根据个人履历和职位描述生成主简历，通过反馈循环针对职位描述进行定制，直至达到合格分数")
    String processCandidate(@V("lifeStory") String userInfo, @V("jobDescription") String jobDescription);
}
