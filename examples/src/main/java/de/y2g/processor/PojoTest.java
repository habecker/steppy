package de.y2g.processor;

import de.y2g.steppy.SingletonFlowBuilderFactory;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.VerificationException;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class PojoTest {
    private static Logger logger = Logger.getLogger("main");

    public static void main(String[] args) throws VerificationException, ExecutionException, InterruptedException {
        var pool = Executors.newFixedThreadPool(8);
        SingletonFlowBuilderFactory.initialize(pool);

        /*
        SingletonStepRepository.register(ProcessImagesStep.name, new ProcessImagesStep());
        SingletonStepRepository.register(UploadImageStep.name, new UploadImageStep());
        SingletonStepRepository.register(IntegerSourceStep.name, new IntegerSourceStep());
        SingletonStepRepository.register(IntegerProducerStep.name, new IntegerProducerStep());
        SingletonStepRepository.register(ScreamStep.name, new ScreamStep());
        SingletonStepRepository.register(WhisperStep.name, new WhisperStep());

        var flow = SingletonFlowBuilderFactory.builder(Void.class)
                .append(IntegerSourceStep.name)
                .nest(builder -> builder
                        .branch(builder1 -> {
                            builder1.
                                    when((c, i) -> (Integer) i >= 20, builder2 -> builder2.append(ScreamStep.name)).
                                    otherwise(builder2 -> builder2.append(WhisperStep.name));
                        })
                        .concurrent()
                )
                .build();

        flow.invoke(null);

        pool.shutdown();
        try {
            pool.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

}
