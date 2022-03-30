package de.y2g.processor.example.reddit.config;

import de.y2g.processor.example.reddit.CrawlSubreddit;
import de.y2g.processor.example.reddit.RedditConnect;

public class CrawlSubredditConfig implements RedditConnect.Config, CrawlSubreddit.Config {
    private String subreddit;
    private String user;
    private String password;
    private int limit;

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
