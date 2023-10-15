package de.y2g.steppy.api.state;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Scope;
import de.y2g.steppy.api.State;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.Variable;
import de.y2g.steppy.api.exception.ExecutionException;

public class GlobalStateStepA implements Step<None, Integer, Integer> {
    @State(name = "global-a", scope = Scope.FLOW)
    Variable<Integer> a;

    @Override
    public Integer invoke(Context<None> context, Integer input) throws ExecutionException {
        var add = a.get(context);
        if (add == null)
            add = 0;
        a.set(context, input);
        return input + add;
    }
}
