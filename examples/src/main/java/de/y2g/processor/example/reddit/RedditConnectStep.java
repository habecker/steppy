package de.y2g.processor.example.reddit;

import de.y2g.steppy.api.*;
import de.y2g.steppy.api.exception.ExecutionException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import org.springframework.stereotype.Component;

@Component(RedditConnectStep.NAME)
public class RedditConnectStep implements Step<RedditConnectStep.Config, Void, Void> {
    public static final String NAME = "reddit-connect";
    public static final String REDDIT_CLIENT_STATE = "reddit-client";
    public static final String CLIENT_ID_PROPERTY = "reddit.client-id";
    public static final String CLIENT_SECRET_PROPERTY = "reddit.client-secret";

    @State(name = REDDIT_CLIENT_STATE, global = true)
    Variable<RedditClient> client;

    @Before(Phase.FLOW)
    void connect(Context<RedditConnectStep.Config> context) throws ExecutionException {

        String clientId = System.getProperty(CLIENT_ID_PROPERTY);
        String clientSecret = System.getProperty(CLIENT_SECRET_PROPERTY);

        if (clientId == null || clientSecret == null) {
            throw new ExecutionException(String.format("Properties must be set: %s, %s", CLIENT_ID_PROPERTY, CLIENT_SECRET_PROPERTY));
        }

        Credentials oauthCreds = Credentials.script(context.getConfiguration().getUsername(), context.getConfiguration().getPassword(), clientId, clientSecret);

        UserAgent userAgent = new UserAgent("bot", "reddit-crawler", "1.0.0", "Steppy-Example");
        RedditClient reddit = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds);

        client.set(context, reddit);
    }

    @Override
    public Void invoke(Context<Config> context, Void input) throws ExecutionException {
        return null;
    }

    public interface Config {
        String getUsername();

        String getPassword();
    }
}
