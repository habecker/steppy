package de.y2g.steppy.spring;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.exception.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { TestConfiguration.class, FlowSupport.class })
public class SpringFlowTest {
    @Autowired
    private SpringFlowBuilderFactory flow;

    @Test
    void mustExecuteFLow() throws ExecutionException {
        flow.builder(None.class, None.class, None.class).append("noop").append(NoopStep.class).build().invoke(None.value());
    }
}
