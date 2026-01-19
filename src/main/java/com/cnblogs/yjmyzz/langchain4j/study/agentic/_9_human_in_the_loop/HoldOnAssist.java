package com.cnblogs.yjmyzz.langchain4j.study.agentic._9_human_in_the_loop;

import dev.langchain4j.agentic.Agent;

public class HoldOnAssist {

    @Agent(description = "招聘流程暂缓")
    public void abort() {
        System.out.println("招聘流程暂缓");
    }
}
