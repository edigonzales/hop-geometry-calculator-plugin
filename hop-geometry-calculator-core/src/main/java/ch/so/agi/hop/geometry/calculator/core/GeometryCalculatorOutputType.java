package ch.so.agi.hop.geometry.calculator.core;

import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.core.row.value.ValueMetaString;

public enum GeometryCalculatorOutputType {
  NUMBER,
  INTEGER,
  BOOLEAN,
  STRING;

  public IValueMeta createValueMeta(String fieldName) {
    return switch (this) {
      case NUMBER -> new ValueMetaNumber(fieldName);
      case INTEGER -> new ValueMetaInteger(fieldName);
      case BOOLEAN -> new ValueMetaBoolean(fieldName);
      case STRING -> new ValueMetaString(fieldName);
    };
  }
}
