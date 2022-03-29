package de.y2g.processor;

import de.y2g.processor.example.ProcessImagesStep;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.validation.VerificationException;
import de.y2g.steppy.spring.FlowSupport;
import de.y2g.steppy.spring.SpringFlowBuilderFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import(FlowSupport.class)
public class ProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }

    @Bean(name = "imageFlow")
    public Flow<Void, Integer, Integer> imageFlow(SpringFlowBuilderFactory flowBuilderFactory) throws VerificationException {
        return flowBuilderFactory.builder(Void.class, Integer.class, Integer.class).append(ProcessImagesStep.name).concurrent().build();
    }
}
