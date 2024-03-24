package de.y2g.steppy.api.configuration;

import de.y2g.steppy.api.Configurations;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.exception.MissingConfigurationException;
import de.y2g.steppy.api.streaming.InfiniteSource;
import de.y2g.steppy.api.streaming.SimpleSink;
import de.y2g.steppy.api.validation.ValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.Executors;

import static de.y2g.steppy.pojo.StaticFlowBuilderFactory.builder;
import static de.y2g.steppy.pojo.StaticFlowBuilderFactory.initialize;
import static de.y2g.steppy.pojo.StaticStepRepository.register;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ConfigurationTest {

    @BeforeAll
    static void setup() {
        register(ReturnConfigStep.class);
        register(ReturnDataClassConfigStep.class);
        initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testDefaultConfiguration() throws ExecutionException, ValidationException {
        var flow = builder(None.class, RecordConfiguration.class).append(ReturnDataClassConfigStep.class).build();
        assertThat(flow.invoke().getResult()).isEqualTo(new RecordConfiguration(1000));
    }

    @Test
    void testAccessConfiguration() throws ExecutionException, ValidationException {
        var flow = builder(None.class, Integer.class).append(ReturnConfigStep.class).build();
        assertThat(flow.invoke(Configurations.of(10)).getResult()).isEqualTo(10);
    }

    @DisplayName("Test missing configuration while streaming concurrently")
    @Test
    void testMissingConfigurationWhileStreamingConcurrently() throws ExecutionException {
        var builder = builder(None.class, Integer.class).append(ReturnConfigStep.class);
        builder.concurrent();
        var flow = builder.build();
        flow.stream(new InfiniteSource<>(None.value()), new SimpleSink<>());
        // TODO: assertion
    }

    @DisplayName("Test missing configuration while streaming")
    @Test
    void testMissingConfigurationWhileStreaming() {
        var builder = builder(None.class, Integer.class).append(ReturnConfigStep.class);
        var flow = builder.build();
        assertThatExceptionOfType(MissingConfigurationException.class).isThrownBy(
            () -> flow.stream(new InfiniteSource<>(None.value()), new SimpleSink<>())).matches(e -> {
            var missing = e.getMissingConfigurations();
            return missing.size() == 1 && missing.get(0) == Integer.class;
        });
    }

    @DisplayName("Test missing configuration")
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testMissingConfiguration(boolean concurrent) {
        var builder = builder(None.class, Integer.class).append(ReturnConfigStep.class);
        if (concurrent) {
            builder.concurrent();
        }
        var flow = builder.build();
        assertThatExceptionOfType(MissingConfigurationException.class).isThrownBy(() -> flow.invoke(Configurations.empty())).matches(e -> {
            var missing = e.getMissingConfigurations();
            return missing.size() == 1 && missing.get(0) == Integer.class;
        });
    }

    private record RecordConfiguration(Integer value) {
        private RecordConfiguration() {
            this(1000);
        }
    }

    private static class ReturnDataClassConfigStep implements Step<RecordConfiguration, None, RecordConfiguration> {
        @Override
        public RecordConfiguration invoke(Context<RecordConfiguration> context, None input) throws ExecutionException {
            return context.getConfiguration();
        }
    }

    private static class ReturnConfigStep implements Step<Integer, None, Integer> {
        @Override
        public Integer invoke(Context<Integer> context, None input) throws ExecutionException {
            return context.getConfiguration();
        }
    }
}