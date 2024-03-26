package de.y2g.steppy.api.provides;

import de.y2g.steppy.api.Consumes;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Provides;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.Variable;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static de.y2g.steppy.pojo.StaticFlowBuilderFactory.builder;
import static de.y2g.steppy.pojo.StaticFlowBuilderFactory.initialize;
import static de.y2g.steppy.pojo.StaticStepRepository.register;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ProvidesTest {

    @BeforeAll
    static void setup() {
        register(ProviderStep.class);
        register(StaticConsumerStep.class);
        register(ConsumerStep.class);
        initialize(Executors.newSingleThreadExecutor());
    }

    @DisplayName("Consuming step should be able to access a provided object")
    @Test
    void testProvider() throws ExecutionException, ValidationException {
        var flow = builder(None.class, SomeClient.class).append(ProviderStep.class).append(ConsumerStep.class).build();
        assertThat(flow.invoke().getResult()).isEqualTo(new SomeClient("test"));
    }

    @DisplayName("Consuming step in a nested flow should be able to access a provided object")
    @Test
    void testNestedConsuming() throws ExecutionException, ValidationException {
        var flow = builder(None.class, None.class).append(ProviderStep.class).nest(None.class, b -> {
            b.append(StaticConsumerStep.class);
        }).build();
        flow.invoke();
        assertThat(StaticConsumerStep.consumedClient).isEqualTo(new SomeClient("test"));
    }

    @DisplayName("Validation should fail if a step is missing a provider")
    @Test
    void testMissingProvider() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
            () -> builder(None.class, SomeClient.class).append(ConsumerStep.class).build()).withMessageContaining(
            "Flow verification failed with errors: Missing dependencies for step de.y2g.steppy.api.provides.ProvidesTest.ConsumerStep: (SomeClient)");
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> builder(None.class, SomeClient.class).append(ConsumerStep.class).append(ProviderStep.class).build())
            .withMessageContaining(
                "Flow verification failed with errors: Missing dependencies for step de.y2g.steppy.api.provides.ProvidesTest.ConsumerStep: (SomeClient)");
    }

    private record SomeClient(String name) {
    }

    private static class ProviderStep implements Step<None, None, None> {
        @Provides
        Variable<SomeClient> client;

        @Override
        public None invoke(Context<None> context, None input) {
            client.set(context, new SomeClient("test"));
            return None.value();
        }
    }

    private static class StaticConsumerStep implements Step<None, None, None> {
        static SomeClient consumedClient;

        @Consumes
        Variable<SomeClient> client;

        @Override
        public @NotNull None invoke(@NotNull Context<None> context, @NotNull None input) throws ExecutionException {
            consumedClient = client.get(context);
            return None.value();
        }
    }

    private static class ConsumerStep implements Step<None, None, SomeClient> {
        @Consumes
        Variable<SomeClient> client;

        @Override
        public SomeClient invoke(Context<None> context, None input) throws ExecutionException {
            return client.get(context);
        }
    }
}