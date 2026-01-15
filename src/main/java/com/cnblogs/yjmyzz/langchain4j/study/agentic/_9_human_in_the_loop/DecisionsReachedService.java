package com.cnblogs.yjmyzz.langchain4j.study.agentic._9_human_in_the_loop;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DecisionsReachedService {
    @SystemMessage("根据交互内容，如果已达成决策则返回true，" +
            "如果需要进一步讨论寻找解决方案则返回false。")
    @UserMessage("""
            迄今为止的交互：
             秘书：{{proposal}}
             受邀者：{{candidateAnswer}}
    """)
    boolean isDecisionReached(@V("proposal") String proposal, @V("candidateAnswer") String candidateAnswer);
}
