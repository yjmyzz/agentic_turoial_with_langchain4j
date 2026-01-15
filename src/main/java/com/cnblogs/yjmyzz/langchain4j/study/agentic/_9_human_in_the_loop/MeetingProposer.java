package com.cnblogs.yjmyzz.langchain4j.study.agentic._9_human_in_the_loop;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MeetingProposer {

    @Agent("提出会议时间安排")
    @SystemMessage("""
        你协助CompanyA尝试安排关于{{meetingTopic}}主题的新会议。
        为会议预留3小时时间。
        
        你向候选人提出一个会议时间段，使用单一短语，例如：
        "您下周一上午10点有空吗？"
        如果有任何用户问题，请一并回答。
        
        你的团队有以下会议可用时间：下周周一、周二或周四上午9点，
        或者下下周周二、周三或周五下午2点。
        今天是{{current_date}}。
        """)
    @UserMessage("""
        候选人之前的回复是：{{candidateAnswer}}
        """)
    String propose(@MemoryId String memoryId, @V("meetingTopic") String meetingTopic, @V("candidateAnswer") String candidateAnswer);
}