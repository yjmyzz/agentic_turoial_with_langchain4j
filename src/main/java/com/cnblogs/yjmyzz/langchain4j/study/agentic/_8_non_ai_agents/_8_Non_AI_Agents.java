package com.cnblogs.yjmyzz.langchain4j.study.agentic._8_non_ai_agents;


import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.HrCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.ManagerCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.TeamMemberCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 这里展示如何在智能体工作流中使用非AI智能体（普通Java操作符）。
 * 非AI智能体只是普通的方法，但可以像其他类型的智能体一样使用。
 * 它们非常适合确定性的操作，如计算、数据转换和聚合，这些操作不需要LLM参与。
 * 将更多步骤外包给非AI智能体，你的工作流将更快、更准确、更经济。
 * 对于需要强制确定性执行的步骤，非AI智能体比工具更受青睐。
 * 在这个例子中，我们希望评审者的综合评分是确定性计算的，而不是由LLM计算。
 * 我们同样基于综合评分确定性地更新数据库中的申请状态。
 * by 菩提树下的杨过（yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _8_Non_AI_Agents {

    public static void main(String[] args) throws IOException {

        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        // 2. 构建并行评审步骤的AI子智能体
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(model)
                .outputKey("hrReview")
                .build();

        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(model)
                .outputKey("managerReview")
                .build();

        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(model)
                .outputKey("teamMemberReview")
                .build();

        // 3. 构建组合的并行智能体
        var executor = Executors.newFixedThreadPool(3);  // 保留引用以便后续关闭

        UntypedAgent parallelReviewWorkflow = AgenticServices
                .parallelBuilder()
                .subAgents(hrReviewer, managerReviewer, teamReviewer)
                .executor(executor)
                .build();

        // 4. 构建包含非AI智能体的完整工作流
        UntypedAgent collectFeedback = AgenticServices
                .sequenceBuilder()
                .subAgents(
                        parallelReviewWorkflow,
                        new ScoreAggregator(), // 非AI智能体不需要AgenticServices构建器。outputKey 'combinedCvReview' 已在类中定义
                        new StatusUpdate(), // 以'combinedCvReview'作为输入，不需要输出
                        AgenticServices.agentAction(agenticScope -> { // 添加非AI智能体的另一种方式，可以操作AgenticScope
                            CvReview review = (CvReview) agenticScope.readState("combinedCvReview");
                            agenticScope.writeState("scoreAsPercentage", review.score * 100); // 当不同系统的智能体通信时，通常需要输出转换
                        })
                )
                .outputKey("scoreAsPercentage") // outputKey在ScoreAggregator.java中的非AI智能体注解中定义
                .build();

        // 5. 加载输入数据
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");

        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "hrRequirements", hrRequirements,
                "phoneInterviewNotes", phoneInterviewNotes,
                "jobDescription", jobDescription
        );

        // 6. 调用工作流
        double scoreAsPercentage = (double) collectFeedback.invoke(arguments);
        executor.shutdown();

        System.out.println("=== 百分比形式的评分 ===");
        System.out.println(scoreAsPercentage);
        // 从日志中我们可以看到，申请状态也已经相应更新

    }
}