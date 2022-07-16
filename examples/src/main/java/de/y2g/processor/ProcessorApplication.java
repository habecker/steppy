package de.y2g.processor;

import de.y2g.processor.example.reddit.CrawlSubredditStep;
import de.y2g.processor.example.reddit.LogSubmissionStep;
import de.y2g.processor.example.reddit.RedditConnectStep;
import de.y2g.processor.example.reddit.config.CrawlSubredditConfig;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.validation.VerificationException;
import de.y2g.steppy.spring.FlowSupport;
import de.y2g.steppy.spring.SpringFlowBuilderFactory;
import net.dean.jraw.models.Submission;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import(FlowSupport.class)
public class ProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }

    @Bean(name = "redditFlow")
    public Flow<CrawlSubredditConfig, Void, Void> redditFlow(SpringFlowBuilderFactory flowBuilderFactory) throws VerificationException {
        return flowBuilderFactory
                .builder(CrawlSubredditConfig.class, Void.class, Void.class)
                .append(RedditConnectStep.NAME)
                .append(CrawlSubredditStep.NAME)
                .nest(nestedBuilder -> nestedBuilder
                        .branch(branchBuilder -> branchBuilder
                                .when((context, submission) -> !((Submission) submission).isNsfw(),
                                        builder1 -> builder1.append(LogSubmissionStep.NAME))
                                .otherwiseContinue()
                        )
                        .concurrent()
                )
                .build();
    }
}
