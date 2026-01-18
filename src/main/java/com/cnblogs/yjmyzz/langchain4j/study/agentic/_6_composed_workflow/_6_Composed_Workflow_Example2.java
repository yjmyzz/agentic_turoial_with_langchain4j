package com.cnblogs.yjmyzz.langchain4j.study.agentic._6_composed_workflow;


import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._1_basic_agent.CvGenerator;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow.CvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow.ScoredCvTailor;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.HrCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.ManagerCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.TeamMemberCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.EmailAssistant;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.InterviewOrganizer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.OrganizingTools;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.RagProvider;
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
 * 每个智能体，无论是单任务智能体、顺序工作流...，都仍然是一个Agent对象。
 * 这使得智能体可以完全组合。你可以：
 * - 将较小的智能体捆绑成超级智能体
 * - 使用子智能体分解任务
 * - 在任何层级混合顺序、并行、循环、监督...等工作流
 * 在这个示例中，我们将把之前构建的组合智能体（顺序、并行等）
 * 组合成两个更大的复合智能体，来编排整个申请流程。
 * by 菩提树下的杨过(yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _6_Composed_Workflow_Example2 {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);
        RagProvider ragProvider = context.getBean("ragProvider", RagProvider.class);

        ////////////////// 候选人组合工作流 //////////////////////
        // 我们将从个人履历 > 简历 > 评审 > 评审循环直到通过
        // 然后将简历通过电子邮件发送给公司

        // 1. 为候选人工作流创建所有必要的智能体
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(model)
                .outputKey("cv")
                .build();

        ScoredCvTailor scoredCvTailor = AgenticServices
                .agentBuilder(ScoredCvTailor.class)
                .chatModel(model)
                .outputKey("cv")
                .build();

        CvReviewer cvReviewer = AgenticServices
                .agentBuilder(CvReviewer.class)
                .chatModel(model)
                .outputKey("cvReview")
                .build();

        // 2. 创建简历改进的循环工作流
        UntypedAgent cvImprovementLoop = AgenticServices
                .loopBuilder()
                .subAgents(scoredCvTailor, cvReviewer)
                .outputKey("cv")
                .exitCondition(agenticScope -> {
                    CvReview review = (CvReview) agenticScope.readState("cvReview");
                    System.out.println("简历评审分数: " + review.score);
                    if (review.score >= 0.8)
                        System.out.println("简历已足够好，退出循环。\n");
                    return review.score >= 0.8;
                })
                .maxIterations(3)
                .build();

        // 3. 创建完整的候选人工作流：生成 > 评审 > 改进循环
        CandidateWorkflow candidateWorkflow = AgenticServices
                .sequenceBuilder(CandidateWorkflow.class)
                .subAgents(cvGenerator, cvReviewer, cvImprovementLoop)
                // 这里我们在sequenceBuilder中使用组合智能体cvImprovementLoop
                // 我们还需要cvReviewer来在进入循环前生成第一次评审
                .outputKey("cv")
                .build();

        // 4. 加载输入数据
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. 执行候选人工作流
        String candidateCv = candidateWorkflow.processCandidate(lifeStory, jobDescription);
        // 注意：输入参数和中间参数都存储在一个AgenticScope中
        // 这个作用域对系统中的所有智能体都可用，无论我们有多少层级的组合

        System.out.println("=== 候选人工作流完成 ===");
        System.out.println("最终简历: " + candidateCv);

        System.out.println("\n\n\n\n");

        ////////////////// 招聘团队组合工作流 //////////////////////
        // 我们收到包含候选人简历和联系方式的电子邮件。我们进行了电话HR面试。
        // 现在我们通过3个并行评审，然后将结果传入条件流程来决定邀请或拒绝。

        // 1. 为招聘团队工作流创建所有必要的智能体
        HrCvReviewer hrCvReviewer = AgenticServices
                .agentBuilder(HrCvReviewer.class)
                .chatModel(model)
                .outputKey("hrReview")
                .build();

        ManagerCvReviewer managerCvReviewer = AgenticServices
                .agentBuilder(ManagerCvReviewer.class)
                .chatModel(model)
                .outputKey("managerReview")
                .build();

        TeamMemberCvReviewer teamMemberCvReviewer = AgenticServices
                .agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(model)
                .outputKey("teamMemberReview")
                .build();

        EmailAssistant emailAssistant = AgenticServices
                .agentBuilder(EmailAssistant.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .build();

        InterviewOrganizer interviewOrganizer = AgenticServices
                .agentBuilder(InterviewOrganizer.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .contentRetriever(ragProvider.loadHouseRulesRetriever())
                .build();

        // 2. 创建并行评审工作流
        UntypedAgent parallelReviewWorkflow = AgenticServices
                .parallelBuilder()
                .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer)
                .executor(Executors.newFixedThreadPool(3))
                .outputKey("combinedCvReview")
                .output(agenticScope -> {
                    CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
                    CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
                    String feedback = String.join("\n",
                            "HR评审: " + hrReview.feedback,
                            "经理评审: " + managerReview.feedback,
                            "团队成员评审: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;
                    System.out.println("最终平均简历评审分数: " + avgScore + "\n");
                    return new CvReview(avgScore, feedback);
                })
                .build();

        // 3. 创建最终决策的条件工作流
        UntypedAgent decisionWorkflow = AgenticServices
                .conditionalBuilder()
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("combinedCvReview")).score >= 0.8, interviewOrganizer)
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("combinedCvReview")).score < 0.8, emailAssistant)
                .build();

        // 4. 创建完整的招聘团队工作流：并行评审 → 决策
        HiringTeamWorkflow hiringTeamWorkflow = AgenticServices
                .sequenceBuilder(HiringTeamWorkflow.class)
                .subAgents(parallelReviewWorkflow, decisionWorkflow)
                .build();

        // 5. 加载输入数据
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");


        // 6. 执行招聘团队工作流
        hiringTeamWorkflow.processApplication(candidateCv, jobDescription, hrRequirements, phoneInterviewNotes, candidateContact);
        System.out.println("=== 招聘团队工作流完成 ===");
        System.out.println("并行评审完成并已做出决策");


    }
}