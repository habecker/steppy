package de.y2g.steppy.cdi;

import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.StepRepository;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;

@Dependent
public class CdiStepRepository extends StepRepository {
    @Inject
    private Instance<Step<?, ?, ?>> instance;

    @Override
    protected Step create(String name) {
        return instance.select(NamedLiteral.of(name)).get();
    }

    @Override
    protected Step create(Class<? extends Step> stepType) {
        return instance.select(stepType).get();
    }
}
