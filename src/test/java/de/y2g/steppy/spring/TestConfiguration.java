package de.y2g.steppy.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan
public class TestConfiguration {

    @Bean(name = "flowTaskExecutor")
    public TaskExecutor taskExecutor() {
        // todo: make configurable
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setThreadNamePrefix("flow_task_executor_thread");
        executor.initialize();

        return executor;
    }
}
