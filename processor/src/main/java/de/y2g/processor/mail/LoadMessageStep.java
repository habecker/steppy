package de.y2g.processor.mail;

import de.y2g.steppy.api.After;
import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Phase;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component(MailSteps.LOAD_MESSAGE)
public class LoadMessageStep implements Step<ImapConfig, Void, MimeMessage> {

    @Override
    public MimeMessage invoke(Context<ImapConfig> context, Void input) throws ExecutionException {
        return null;
    }
}
