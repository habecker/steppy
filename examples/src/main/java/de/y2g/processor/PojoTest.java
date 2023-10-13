package de.y2g.processor;

import de.y2g.processor.example.reddit.CrawlSubredditStep;
import de.y2g.processor.example.reddit.LogSubmissionStep;
import de.y2g.processor.example.reddit.RedditConnectStep;
import de.y2g.processor.example.reddit.config.CrawlSubredditConfig;
import de.y2g.steppy.SingletonFlowBuilderFactory;
import de.y2g.steppy.SingletonStepRepository;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.VerificationException;
import net.dean.jraw.models.Submission;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PojoTest {
    private static final Logger logger = Logger.getLogger("main");

    public static void main(String[] args) throws VerificationException, ExecutionException, InterruptedException {
        var pool = Executors.newFixedThreadPool(8);
        SingletonFlowBuilderFactory.initialize(pool);

        SingletonStepRepository.register(CrawlSubredditStep.NAME, new CrawlSubredditStep());
        SingletonStepRepository.register(LogSubmissionStep.NAME, new LogSubmissionStep());
        SingletonStepRepository.register(RedditConnectStep.NAME, new RedditConnectStep());

        var flow = SingletonFlowBuilderFactory.builder(CrawlSubredditConfig.class)
                .append(RedditConnectStep.NAME)
                .append(CrawlSubredditStep.NAME)
                // TODO:
                // .when(trigger, ...)
                .nest(nestedBuilder -> nestedBuilder
                        .branch(branchBuilder -> branchBuilder
                                .when((context, submission) -> !((Submission) submission).isNsfw(),
                                        builder1 -> builder1.append(LogSubmissionStep.NAME))
                                .otherwiseContinue()
                        )
                        .concurrent()
                )
                .build();

        String user = System.getProperty("user");
        String password = System.getProperty("password");
        String subreddit = System.getProperty("subreddit");

        flow.invoke(new CrawlSubredditConfig(user, password, subreddit, 100));

        pool.shutdown();
        try {
            pool.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
