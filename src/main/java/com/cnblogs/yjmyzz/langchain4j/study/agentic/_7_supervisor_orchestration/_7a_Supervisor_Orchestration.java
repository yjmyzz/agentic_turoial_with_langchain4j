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
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


import java.io.IOException;

/**
 * 到目前为止，我们构建的都是确定性工作流：
 * - 顺序、并行、条件、循环以及它们的组合。
 * 你还可以构建一个监督者智能体系统，其中智能体会动态决定调用哪些子智能体以及调用顺序。
 * 在这个示例中，监督者协调招聘工作流：
 * 他负责运行HR/经理/团队评审，然后要么安排面试，要么发送拒绝邮件。
 * 就像组合工作流示例的第二部分，但现在是"自组织的"
 * 注意：监督者超级智能体可以像其他类型的超级智能体一样用于组合工作流中。
 * 重要提示：这个示例使用GPT-4o-mini运行大约需要50秒。你可以在PRETTY日志中持续查看执行过程。
 * 有方法可以加速执行，请参见本文件末尾的注释。
 * by 菩提树下的杨过（yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _7a_Supervisor_Orchestration {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        // 1. 定义所有子智能体
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(model)
                .outputKey("hrReview")
                .build();
        // 重要：如果我们为多个智能体使用相同的方法名
        // （在此例中：所有评审者都使用'reviewCv'方法），最好为智能体命名，像这样：
        // @Agent(name = "managerReviewer", description = "基于职位描述审查简历，提供反馈和评分")

        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(model)
                .outputKey("managerReview")
                .build();

        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(model)
                .outputKey("teamMemberReview")
                .build();

        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .build();

        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .build();

        // 2. 构建监督者智能体
        SupervisorAgent hiringSupervisor = AgenticServices.supervisorBuilder()
                .chatModel(model)
                .subAgents(hrReviewer, managerReviewer, teamReviewer, interviewOrganizer, emailAssistant)
                .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                .responseStrategy(SupervisorResponseStrategy.SUMMARY) // 我们想要执行过程的摘要，而不是检索特定响应
                .supervisorContext("始终使用所有可用的评审者。始终用英语回答。调用智能体时，使用纯JSON（无反引号，换行符使用反斜杠+n）。") // 监督者行为的可选上下文
                .build();
        // 重要须知：监督者一次调用一个智能体，然后审查其计划以选择下一个调用的智能体
        // 监督者无法并行执行智能体
        // 如果智能体被标记为异步，监督者将覆盖该设置（无异步执行）并发出警告

        // 3. 加载候选人简历和职位描述
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        // 开始计时
        long start = System.nanoTime();
        // 4. 用自然语言请求调用监督者
        String result = (String) hiringSupervisor.invoke(
                "评估以下候选人：\n" +
                        "候选人简历：\n" + candidateCv + "\n\n" +
                        "候选人联系方式：\n" + candidateContact + "\n\n" +
                        "职位描述：\n" + jobDescription + "\n\n" +
                        "HR要求：\n" + hrRequirements + "\n\n" +
                        "电话面试记录：\n" + phoneInterviewNotes
        );
        long end = System.nanoTime();
        double elapsedSeconds = (end - start) / 1_000_000_000.0;
        // 在日志中你会注意到最终调用了'done'智能体，这是监督者完成调用系列的方式

        System.out.println("=== 监督者运行完成，耗时 " + elapsedSeconds + " 秒 ===");
        System.out.println(result);
    }

    // 高级用例：
    // 参见 _7b_Supervisor_Orchestration_Advanced.java 了解：
    // - 类型化监督者，
    // - 上下文工程，
    // - 输出策略，
    // - 调用链观察，

    // 关于延迟：
    // 整个流程运行通常需要超过60秒。
    // 一个解决方案是使用快速推理提供商如CEREBRAS，
    // 它将在10秒内运行整个流程，但会犯更多错误。
    // 要使用CEREBRAS尝试此示例，获取一个密钥（点击获取免费API密钥）
    // https://inference-docs.cerebras.ai/quickstart
    // 并保存在环境变量中作为"CEREBRAS_API_KEY"
    // 然后将第38行改为：
    // private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel("CEREBRAS");
}