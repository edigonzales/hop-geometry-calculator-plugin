package ch.so.agi.hop.geometry.calculator.core;

import java.util.Set;
import java.util.function.Predicate;
import org.locationtech.jts.geom.Geometry;

public final class GeometryCalculatorFunction {

  @FunctionalInterface
  interface Evaluator {
    Object evaluate(Geometry geometry) throws Exception;
  }

  private final GeometryCalculatorOperationId id;
  private final String label;
  private final String description;
  private final String geometryHint;
  private final GeometryCalculatorCategory category;
  private final GeometryCalculatorOutputType defaultOutputType;
  private final Set<GeometryCalculatorOutputType> allowedOutputTypes;
  private final Predicate<Geometry> supports;
  private final Evaluator evaluator;

  GeometryCalculatorFunction(
      GeometryCalculatorOperationId id,
      String label,
      String description,
      String geometryHint,
      GeometryCalculatorCategory category,
      GeometryCalculatorOutputType defaultOutputType,
      Set<GeometryCalculatorOutputType> allowedOutputTypes,
      Predicate<Geometry> supports,
      Evaluator evaluator) {
    this.id = id;
    this.label = label;
    this.description = description;
    this.geometryHint = geometryHint;
    this.category = category;
    this.defaultOutputType = defaultOutputType;
    this.allowedOutputTypes = Set.copyOf(allowedOutputTypes);
    this.supports = supports;
    this.evaluator = evaluator;
  }

  public GeometryCalculatorOperationId id() {
    return id;
  }

  public String label() {
    return label;
  }

  public String description() {
    return description;
  }

  public String geometryHint() {
    return geometryHint;
  }

  public GeometryCalculatorCategory category() {
    return category;
  }

  public GeometryCalculatorOutputType defaultOutputType() {
    return defaultOutputType;
  }

  public Set<GeometryCalculatorOutputType> allowedOutputTypes() {
    return allowedOutputTypes;
  }

  public String displayLabel() {
    return label() + " (" + category().getLabel() + ")";
  }

  public boolean supports(Geometry geometry) {
    return geometry != null && supports.test(geometry);
  }

  public Object evaluate(Geometry geometry) throws Exception {
    return evaluator.evaluate(geometry);
  }
}
