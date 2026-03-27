package ch.so.agi.hop.geometry.calculator.core;

public enum GeometryCalculatorCategory {
  MEASUREMENTS("Measurements"),
  COORDINATES("Coordinates"),
  EXTENT("Extent"),
  STRUCTURE("Structure"),
  METADATA("Metadata");

  private final String label;

  GeometryCalculatorCategory(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
