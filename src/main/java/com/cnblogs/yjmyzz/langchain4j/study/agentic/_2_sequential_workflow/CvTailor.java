package com.cnblogs.yjmyzz.langchain4j.study.agentic._2_sequential_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 简历优化器
 */
public interface CvTailor {

    @Agent("根据特定要求优化简历")
    @SystemMessage("""
            这里有一份需要根据特定职位描述、反馈或其他指示进行优化的简历。
            你可以让简历看起来更符合要求，但不能编造事实。
            如果去掉不相关的内容能让简历更符合要求，你可以进行删减。
            目标是让申请人获得面试机会，并能在面试中证明简历内容。不要让简历过于冗长。
            
            原始简历：{{masterCv}}
            """)
    @UserMessage("""
            以下是优化简历的具体要求：{{instructions}}
            """)
    String tailorCv(@V("masterCv") String masterCv, @V("instructions") String instructions);
}
