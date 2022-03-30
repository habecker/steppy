package de.y2g.processor.example.reddit;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import net.dean.jraw.models.Submission;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component(LogSubmission.NAME)
public class LogSubmission implements Step<Object, Submission, Void> {
    private static final Logger logger = Logger.getLogger(LogSubmission.NAME);

    public static final String NAME = "log-submission";

    @Override
    public Void invoke(Context<Object> context, Submission input) throws ExecutionException {
        logger.info(String.format("%s - %s", input.getAuthor(), input.getTitle()));
        return null;
    }
}
