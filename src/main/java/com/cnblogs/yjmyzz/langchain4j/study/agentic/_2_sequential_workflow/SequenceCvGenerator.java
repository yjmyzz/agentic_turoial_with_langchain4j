package com.cnblogs.yjmyzz.langchain4j.study.agentic._2_sequential_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.V;

import java.util.Map;

/**
 * 根据个人资料和岗位要求，生成优化后的简历
 * @author junmingyang
 */
public interface SequenceCvGenerator {
    @Agent("根据用户提供的信息和特定要求生成简历，内容简洁，避免空行")
    ResultWithAgenticScope<Map<String, String>> generateTailoredCv(@V("lifeStory") String lifeStory, @V("instructions") String instructions);
}