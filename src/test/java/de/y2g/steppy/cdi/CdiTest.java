package de.y2g.steppy.cdi;

import de.y2g.steppy.api.FlowBuilderFactory;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.jboss.weld.junit5.WeldInitiator.createWeld;

@ExtendWith(WeldJunit5Extension.class)
public class CdiTest {
    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            createWeld().beanClasses(Step.class, NoopStep.class, CdiStepRepository.class, TestConfiguration.class, CdiFlowBuilderFactory.class))
        .build();

    @Test
    void mustExecuteFlow(FlowBuilderFactory flows) throws ExecutionException {
        flows.builder(None.class, None.class).append("noop").append(NoopStep.class).build().invoke();
    }
}
