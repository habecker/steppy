package de.y2g.processor;

import de.y2g.processor.example.IntegerProducerStep;
import de.y2g.processor.example.IntegerSourceStep;
import de.y2g.processor.example.ProcessImagesStep;
import de.y2g.processor.example.UploadImageStep;
import de.y2g.steppy.SingletonFlowBuilderFactory;
import de.y2g.steppy.SingletonStepRepository;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Sink;
import de.y2g.steppy.api.validation.VerificationException;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PojoTest {
    private static Logger logger = Logger.getLogger("main");
    public static void main(String[] args) throws VerificationException, ExecutionException, InterruptedException {
        var pool = Executors.newFixedThreadPool(8);
        SingletonFlowBuilderFactory.initialize(pool);

        SingletonStepRepository.register(ProcessImagesStep.name, new ProcessImagesStep());
        SingletonStepRepository.register(UploadImageStep.name, new UploadImageStep());
        SingletonStepRepository.register(IntegerSourceStep.name, new IntegerSourceStep());
        SingletonStepRepository.register(IntegerProducerStep.name, new IntegerProducerStep());

        var flow = SingletonFlowBuilderFactory.builder(Void.class)
                .append(IntegerSourceStep.name)
                .nest(builder -> builder
                        .append(ProcessImagesStep.name)
                        .append(ProcessImagesStep.name)
                        .append(IntegerProducerStep.name)
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
    }

}
