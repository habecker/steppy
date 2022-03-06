package de.y2g.steppy;

import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.StepRepository;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

@Dependent
@SuppressWarnings({"unchecked","rawtypes"})
public class WeldStepRepository extends StepRepository {
    @Override
    protected Step create(String name) {
        BeanManager beanManager = CDI.current().getBeanManager();
        Bean<Step> bean = (Bean<Step>) beanManager.getBeans(name).stream().filter(b -> Step.class.isAssignableFrom(b.getBeanClass())).iterator().next();
        CreationalContext ctx = beanManager.createCreationalContext(bean);
        return (Step) beanManager.getReference(bean, bean.getBeanClass(), ctx);
    }
}
