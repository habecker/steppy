package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SerialFlowProxy<C, I, R> extends FlowProxy<C, I, R> implements Flow<C, I, R> {

    public SerialFlowProxy(Typing<C, I, R> typing, @Nonnull List<StepProxy> steps) {
        super(typing, steps);
    }

    @Override
    public Collection<Result<R>> invoke(C configuration, Collection<I> inputs) throws ExecutionException {
        var context = new Context<>(configuration);
        var result = new ArrayList<Result<R>>(inputs.size());
        try {
            callBefore(context);


            for (I input : inputs) {
                try {
                    Result<R> data = invokeSingleItem(context, input);
                    result.add(data);
                } catch (ExecutionException e) {
                    result.add(new Result<R>(Result.Type.FAILED, e));
                }
            }
        } finally {
            callAfter(context);
        }

        return result;
    }

}
