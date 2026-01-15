package com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
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

/**
 * 此示例演示了条件式智能体工作流。
 * 基于评分和候选人资料，我们将执行以下操作之一：
 * - 调用一个智能体，为该候选人的现场面试做好一切准备
 * - 调用一个智能体，发送一封友好的邮件，告知我们不会推进该候选人的申请*
 * by 菩提树下的杨过(yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _5a_Conditional_Workflow_Example {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        // 2. 在此包中定义两个子智能体：
        //      - EmailAssistant.java
        //      - InterviewOrganizer.java

        // 3. 使用AgenticServices创建所有智能体
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(model)
                .tools(new OrganizingTools()) // 该智能体可以使用那里定义的所有工具
                .build();
        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .contentRetriever(RagProvider.loadHouseRulesRetriever()) // 这是如何为智能体添加RAG的方式
                .build();

        // 4. 构建条件式工作流
        UntypedAgent candidateResponder = AgenticServices // 使用UntypedAgent，除非您定义了合成的智能体，请参见_2_Sequential_Agent_Example
                .conditionalBuilder()
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score >= 0.8, interviewOrganizer)
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score < 0.8, emailAssistant)
                .build();
        // 重要提示：当定义了多个条件时，它们会按顺序执行。
        // 如果您想在这里并行执行，请使用异步智能体，如_5b_Conditional_Workflow_Example_Async中所示

        // 5. 从resources/documents/中的文本文件加载参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview cvReviewFail = new CvReview(0.6, "简历不错，但缺少一些与后端职位相关的技术细节。");
        CvReview cvReviewPass = new CvReview(0.9, "简历非常出色，符合后端职位的所有要求。");

        // 5. 因为我们使用了无类型智能体，所以需要传递所有输入参数的映射
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", cvReviewPass // 更改为cvReviewFail以查看另一个分支
        );

        // 5. 调用条件式智能体，根据评审结果对候选人做出回应
        candidateResponder.invoke(arguments);
        // 在这个例子中，我们没有对AgenticScope做有意义的更改，
        // 并且我们也没有一个有意义的输出来打印，因为工具执行了最终动作。
        // 我们将在控制台打印工具执行了哪些动作（邮件已发送，申请状态已更新）

        // 当您在调试模式下观察日志时，工具调用的结果'success'仍然会发送给模型，
        // 并且模型仍然会回答类似"邮件已发送给John Doe，通知他..."的内容

        // 补充信息：如果工具是您的最后一步操作，并且您不想在此之后再次调用模型，
        // 您通常会添加`@Tool(returnBehavior = ReturnBehavior.IMMEDIATE)`
        // https://docs.langchain4j.dev/tutorials/tools#returning-immediately-the-result-of-a-tool-execution-request
        // !!! 但是，在智能体工作流中，工具使用IMMEDIATE RETURN BEHAVIOR是不推荐的，
        // 因为立即返回行为会将工具结果存储在AgenticScope中，可能会导致问题

        // 补充信息：这是一个通过代码检查条件实现路由行为的示例。
        // 路由行为也可以通过让LLM决定最佳的工具/智能体来继续执行，这可以通过以下方式实现：
        // - Supervisor智能体：将操作智能体，参见_7_supervisor_orchestration
        // - 将AiServices作为工具使用，如下所示
        // RouterService routerService = AiServices.builder(RouterAgent.class)
        //        .chatModel(model)
        //        .tools(medicalExpert, legalExpert, technicalExpert)
        //        .build();
        //
        // 最佳选择取决于您的用例：
        //
        // - 使用条件式智能体时，您硬编码调用标准
        // - 与使用AiServices或Supervisor相比，LLM决定调用哪个专家
        //
        // - 使用智能体解决方案（条件式、监督式），所有中间状态和调用链都存储在AgenticScope中
        // - 与使用AiServices相比，跟踪调用链或中间状态要困难得多

    }
}