package com.cnblogs.yjmyzz.langchain4j.study.agentic._6_composed_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

public interface CvImprovementLoop {
    @Agent("通过迭代定制和评审来改进简历，直至达到合格分数")
    String improveCv(@V("cv") String cv, @V("jobDescription") String jobDescription);
}