package ch.so.agi.hop.geometry.calculator.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ScalarResultConverterTest {

  @Test
  void convertsSupportedTypes() {
    assertThat(ScalarResultConverter.convert(7L, GeometryCalculatorOutputType.INTEGER)).isEqualTo(7L);
    assertThat(ScalarResultConverter.convert(7L, GeometryCalculatorOutputType.NUMBER)).isEqualTo(7.0d);
    assertThat(ScalarResultConverter.convert(true, GeometryCalculatorOutputType.BOOLEAN)).isEqualTo(true);
    assertThat(ScalarResultConverter.convert(3.5d, GeometryCalculatorOutputType.STRING)).isEqualTo("3.5");
    assertThat(ScalarResultConverter.convert(null, GeometryCalculatorOutputType.STRING)).isNull();
  }

  @Test
  void rejectsIncompatibleConversions() {
    assertThatThrownBy(() -> ScalarResultConverter.convert("abc", GeometryCalculatorOutputType.NUMBER))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ScalarResultConverter.convert(1L, GeometryCalculatorOutputType.BOOLEAN))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
