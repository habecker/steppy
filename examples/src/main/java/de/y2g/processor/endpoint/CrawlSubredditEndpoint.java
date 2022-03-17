package de.y2g.processor.endpoint;

import de.y2g.processor.example.reddit.config.CrawlSubredditConfig;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collection;

@RestController
public class CrawlSubredditEndpoint {
    private final Flow<CrawlSubredditConfig, Void, Void> redditFlow;

    public CrawlSubredditEndpoint(Flow<CrawlSubredditConfig, Void, Void> redditFlow) {
        this.redditFlow = redditFlow;
    }

    @PostMapping("/reddit-crawler")
    public void crawl(@Valid @RequestBody CrawlSubredditConfig config) throws ExecutionException {
        redditFlow.invoke(config);
    }
}
