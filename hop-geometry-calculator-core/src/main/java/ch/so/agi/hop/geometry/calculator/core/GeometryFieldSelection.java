package ch.so.agi.hop.geometry.calculator.core;

import java.util.List;

public record GeometryFieldSelection(List<String> fieldNames, String selectedField, String warning) {

  public boolean hasSelection() {
    return selectedField != null && !selectedField.isBlank();
  }
}
