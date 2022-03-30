package de.y2g.processor.example.reddit;

import de.y2g.steppy.api.*;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Source;
import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.pagination.DefaultPaginator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import static de.y2g.processor.example.reddit.RedditConnectStep.REDDIT_CLIENT_STATE;

@DependsOn(RedditConnectStep.NAME)
@Component(CrawlSubredditStep.NAME)
public class CrawlSubredditStep implements Step<CrawlSubredditStep.Config, Void, Source<Submission>> {
    public static final String NAME = "subreddit-crawler";

    @State(name = REDDIT_CLIENT_STATE, global = true)
    Variable<RedditClient> client;


    @Override
    public Source<Submission> invoke(Context<Config> context, Void input) throws ExecutionException {
        DefaultPaginator<Submission> paginator = client.get(context).subreddit(context.getConfiguration().getSubredditName()).posts().build();

        Iterator<Listing<Submission>> it = paginator.iterator();

        int limit = context.getConfiguration().getSubmissionLimit();

        Source<Submission> source = new Source<Submission>() {
            Queue<Submission> batch = new LinkedList<>();
            Iterator<Listing<Submission>> it = paginator.iterator();
            boolean active = true;
            int counter = 0;

            @Override
            public boolean isActive() {
                return active && counter < limit;
            }

            @Override
            public synchronized boolean next(Duration timeout, Consumer<Submission> consumer) throws InterruptedException {
                if (!isActive())
                    return false;

                if (batch.isEmpty() && it.hasNext()) {
                    batch.addAll(it.next());
                }

                Submission submission = batch.poll();

                if (submission == null) {
                    active = false;
                    return active;
                }

                consumer.accept(submission);
                counter++;

                return true;
            }

            @Override
            public void close() {
                active = false;
            }
        };

        return source;
    }

    public interface Config {
        String getSubredditName();

        int getSubmissionLimit();
    }
}
