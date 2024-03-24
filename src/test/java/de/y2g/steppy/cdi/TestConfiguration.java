package de.y2g.steppy.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TestConfiguration {

    @Produces
    public ExecutorService executorService() {
        BlockingQueue<Runnable> workQueue = new SynchronousQueue<>();
        return new ThreadPoolExecutor(1, 2, 0, TimeUnit.SECONDS, workQueue);
    }
}
