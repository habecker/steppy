package de.y2g.processor.endpoint;

import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;

@RestController
public class ProcessEndpoint {
    @Autowired
    private Flow<Void, Integer, Integer> imageFlow;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) throws ExecutionException {

        Collection<Result<Integer>> result = imageFlow.invoke(null, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16));
        StringBuilder resp = new StringBuilder();
        for (var l : result) {
            resp.append(l.getResult());
            resp.append(", ");
        }
        return resp.toString();
    }
}
