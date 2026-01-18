package com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CvGenerator {
    @UserMessage("""
            以下是我的个人生活和职业经历信息，
            请将其整理成一份清晰、完整的中文简历。
            
            注意事项：
            1. 不要编造任何事实
            2. 不要遗漏任何技能或经历
            3. 这份简历后续会进一步优化，请确保内容完整
            
            我的个人经历：{{lifeStory}}
            """)
    @Agent("基于用户提供的信息生成规范的简历")
    String generateCv(@V("lifeStory") String userInfo);
}
