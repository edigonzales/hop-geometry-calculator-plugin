package ch.so.agi.hop.geometry.calculator.transform;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.hop.core.annotations.Transform;
import org.junit.jupiter.api.Test;

class GeometryCalculatorPluginContractTest {

  @Test
  void usesTheSharedGeometryClassLoaderGroup() {
    Transform plugin = GeometryCalculatorMeta.class.getAnnotation(Transform.class);

    assertThat(plugin).isNotNull();
    assertThat(plugin.classLoaderGroup()).isEqualTo("sogeo-geometry");
  }
}
