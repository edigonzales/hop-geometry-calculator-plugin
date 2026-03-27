package ch.so.agi.hop.geometry.calculator.core;

public final class ScalarResultConverter {

  private ScalarResultConverter() {}

  public static Object convert(Object value, GeometryCalculatorOutputType outputType) {
    if (value == null) {
      return null;
    }
    return switch (outputType) {
      case BOOLEAN -> convertBoolean(value);
      case INTEGER -> convertInteger(value);
      case NUMBER -> convertNumber(value);
      case STRING -> String.valueOf(value);
    };
  }

  private static Boolean convertBoolean(Object value) {
    if (value instanceof Boolean bool) {
      return bool;
    }
    throw new IllegalArgumentException("Expected BOOLEAN-compatible value but got " + value.getClass().getName());
  }

  private static Long convertInteger(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    throw new IllegalArgumentException("Expected INTEGER-compatible value but got " + value.getClass().getName());
  }

  private static Double convertNumber(Object value) {
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    throw new IllegalArgumentException("Expected NUMBER-compatible value but got " + value.getClass().getName());
  }
}
