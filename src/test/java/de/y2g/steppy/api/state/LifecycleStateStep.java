package de.y2g.steppy.api.state;

import de.y2g.steppy.api.After;
import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Scope;
import de.y2g.steppy.api.State;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.Variable;
import de.y2g.steppy.api.exception.ExecutionException;

import java.util.ArrayList;
import java.util.List;

public class LifecycleStateStep implements Step<None, Integer, Integer> {
    static List<Integer> values = new ArrayList<>();

    @State
    Variable<Integer> a;

    @Before(Scope.FLOW)
    public void beforeFlow(Context<None> context) {
        a.set(context, 100);
        values.add(a.get(context));
    }

    @Before(Scope.STEP)
    public void beforeStep(Context<None> context) {
        values.add(a.get(context));
    }

    @After(Scope.STEP)
    public void afterStep(Context<None> context) {
        values.add(a.get(context));
    }

    @After(Scope.FLOW)
    public void afterFlow(Context<None> context) {
        values.add(a.get(context));
    }

    @Override
    public Integer invoke(Context<None> context, Integer input) throws ExecutionException {
        values.add(a.get(context));
        return a.get(context);
    }
}
