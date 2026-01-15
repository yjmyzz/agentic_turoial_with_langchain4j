package com.cnblogs.yjmyzz.langchain4j.study.agentic._7_supervisor_orchestration;


import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.HrCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.ManagerCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.TeamMemberCvReviewer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.EmailAssistant;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.InterviewOrganizer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.OrganizingTools;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/**
 * 高级监督者示例，使用显式AgenticScope来检查演化中的上下文
 * 在这个示例中，我们构建了一个与_7a_Supervisor_Orchestration类似的监督者，
 * 但我们探索了监督者的一些额外功能：
 * - 类型化监督者，
 * - 上下文工程，
 * - 输出策略，
 * - 调用链观察，
 * - 上下文演化检查
 * by 菩提树下的杨过（yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _7b_Supervisor_Orchestration_Advanced {


    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        // 1. 定义子智能体
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(model)
                .build();
        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(model)
                .build();
        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(model)
                .build();
        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .outputKey("response")
                .build();
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .outputKey("response")
                .build();

        // 2. 构建监督者
        HiringSupervisor hiringSupervisor = AgenticServices
                .supervisorBuilder(HiringSupervisor.class)
                .chatModel(model)
                .subAgents(hrReviewer, managerReviewer, teamReviewer, interviewOrganizer, emailAssistant)
                .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                // 根据你的监督者需要了解子智能体做了什么，
                // 你可以选择contextGenerationStrategy：CHAT_MEMORY、SUMMARIZATION或CHAT_MEMORY_AND_SUMMARIZATION
                .responseStrategy(SupervisorResponseStrategy.SCORED) // 此策略使用评分模型来决定是最后的响应还是摘要最能解决用户请求
                // 这里的输出函数将覆盖响应策略
                .supervisorContext("策略：始终首先检查HR，如有需要则升级，拒绝低匹配度的候选人。")
                .build();

        // 3. 加载输入数据
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        String request = "评估此候选人，并安排面试或发送拒绝邮件。\n"
                + "候选人简历：\n" + candidateCv + "\n"
                + "候选人联系方式：\n" + candidateContact + "\n"
                + "职位描述：\n" + jobDescription + "\n"
                + "HR要求：\n" + hrRequirements + "\n"
                + "电话面试记录：\n" + phoneInterviewNotes;

        // 4. 调用监督者
        long start = System.nanoTime();
        ResultWithAgenticScope<String> decision = hiringSupervisor.invoke(request, "经理技术评审是最重要的。");
        long end = System.nanoTime();

        System.out.println("=== 招聘监督者在 " + ((end - start) / 1_000_000_000.0) + " 秒内完成 ===");
        System.out.println(decision.result());

        // 打印收集的上下文
        System.out.println("\n=== 对话形式的上下文 ===");
        System.out.println(decision.agenticScope().contextAsConversation()); // 将在下一个版本中生效
    }
}