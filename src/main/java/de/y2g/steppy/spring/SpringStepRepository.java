package de.y2g.steppy.spring;

import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.StepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Component
public class SpringStepRepository extends StepRepository {

    private final AutowireCapableBeanFactory context;

    @Autowired
    public SpringStepRepository(AutowireCapableBeanFactory context) {
        this.context = context;
    }

    @Override
    public Step create(String name) {
        return this.context.getBean(name, Step.class);
    }

    @Override
    protected Step create(Class<? extends Step> stepType) {
        return this.context.getBean(stepType);
    }

}
