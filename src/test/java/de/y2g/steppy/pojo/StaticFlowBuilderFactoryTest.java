package de.y2g.steppy.pojo;

import de.y2g.steppy.api.None;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StaticFlowBuilderFactoryTest {
    @Test
    void mustInitializeFactory() {
        StaticFlowBuilderFactory.reset();
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> StaticFlowBuilderFactory.builder(None.class, None.class, None.class)).withMessageContaining("was not initialized");
    }

}