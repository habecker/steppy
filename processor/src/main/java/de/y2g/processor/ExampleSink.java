package de.y2g.processor;

import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.streaming.Sink;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class ExampleSink implements Sink<Result<Integer>> {
    private static Logger logger = Logger.getLogger("sink");
    private final ExecutorService pool;

    public ExampleSink(ExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public void close() {
        logger.info("Sink closed");
        pool.shutdown();
        try {
            pool.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept(Result<Integer> result) {
        logger.info(result.getType() + ":" + result.getResult());
    }
}
