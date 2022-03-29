package de.y2g.processor.mail;

import de.y2g.steppy.api.*;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.stereotype.Component;

@Component(MailSteps.IMAP_CONNECT)
public class ImapConnectStep implements Step<ImapConfig, Void, Void> {

    @Before(Phase.FLOW)
    void connect(Context<ImapConfig> context) {
        var config = context.getConfiguration();
    }

    @After(Phase.FLOW)
    void disconnect() {

    }

    @Override
    public Void invoke(Context<ImapConfig> context, Void input) throws ExecutionException {
        return null;
    }
}
