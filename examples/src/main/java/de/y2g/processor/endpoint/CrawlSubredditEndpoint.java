package de.y2g.processor.endpoint;

import de.y2g.processor.example.reddit.config.CrawlSubredditConfig;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

@RestController
public class CrawlSubredditEndpoint {
    private final Flow<CrawlSubredditConfig, Void, Void> redditFlow;

    public CrawlSubredditEndpoint(Flow<CrawlSubredditConfig, Void, Void> redditFlow) {
        this.redditFlow = redditFlow;
    }

    @PostMapping("/reddit-crawler")
    public void greeting(@RequestParam(value = "username") @Nonnull String user, @RequestParam(value = "password") @Nonnull String password, @RequestParam(value = "subreddit") @Nonnull String subreddit, @RequestParam(value = "limit") int limit) throws ExecutionException {
        CrawlSubredditConfig config = new CrawlSubredditConfig(user, password, subreddit, limit);
        redditFlow.invoke(config);
    }
}
