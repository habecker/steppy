package de.y2g.processor.example.reddit.config;

import de.y2g.processor.example.reddit.CrawlSubredditStep;
import de.y2g.processor.example.reddit.RedditConnectStep;

import javax.annotation.Nonnull;
import javax.validation.constraints.Positive;

public class CrawlSubredditConfig implements RedditConnectStep.Config, CrawlSubredditStep.Config {

    @Nonnull
    private final String subreddit;

    @Nonnull
    private final String user;

    @Nonnull
    private final String password;

    @Positive
    private final int limit;

    public CrawlSubredditConfig(String user, String password, String subreddit, int limit) {
        this.user = user;
        this.password = password;
        this.subreddit = subreddit;
        this.limit = limit;
    }

    @Override
    public String getSubredditName() {
        return subreddit;
    }

    @Override
    public int getSubmissionLimit() {
        return limit;
    }

    @Override
    public String getUsername() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
