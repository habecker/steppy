package de.y2g.processor.mail;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.stereotype.Component;

@Component(MailSteps.PROCESS_MESSAGES)
public class ProcessMessagesStep implements Step<ImapConfig, Void, Void> {

    @Override
    public Void invoke(Context<ImapConfig> context, Void input) throws ExecutionException {
        return null;
    }
}
