package de.y2g.processor.mail;

import de.y2g.steppy.api.After;
import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Phase;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component(MailSteps.SAVE_MESSAGE)
public class SaveMessageStep implements Step<ImapConfig, MimeMessage, MimeMessage> {

    @Override
    public MimeMessage invoke(Context<ImapConfig> context, MimeMessage input) throws ExecutionException {
        return null;
    }
}
