package de.y2g.processor;

import de.y2g.processor.cdi.ProcessImagesStep;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.FlowBuilderFactory;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.VerificationException;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

public class CdiTest {
    private static Logger logger = Logger.getLogger("main");
    public static void main(String[] args) throws VerificationException, ExecutionException {
        SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        FlowBuilderFactory factory;
        /** disable discovery and register bean classes manually */
        try (SeContainer container = initializer.disableDiscovery().addBeanClasses(FlowBuilderFactory.class).initialize()) {
            factory = container.select(FlowBuilderFactory.class).get();
        }
        Flow<Void, Integer, Integer> flow = factory.builder(Void.class, Integer.class, Integer.class).append(ProcessImagesStep.name).concurrent().build();
        Collection<Integer> result = flow.invoke(null, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16));
        StringBuilder resp = new StringBuilder();
        for (var l : result) {
            resp.append(l);
            resp.append(", ");
        }
        logger.info(resp.toString());
    }
}
