package ch.so.agi.hop.geometry.calculator.core;

import java.util.Set;
import java.util.function.Predicate;
import org.locationtech.jts.geom.Geometry;

final class SimpleGeometryCalculatorFunction implements GeometryCalculatorFunction {

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

  SimpleGeometryCalculatorFunction(
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

  @Override
  public GeometryCalculatorOperationId id() {
    return id;
  }

  @Override
  public String label() {
    return label;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public String geometryHint() {
    return geometryHint;
  }

  @Override
  public GeometryCalculatorCategory category() {
    return category;
  }

  @Override
  public GeometryCalculatorOutputType defaultOutputType() {
    return defaultOutputType;
  }

  @Override
  public Set<GeometryCalculatorOutputType> allowedOutputTypes() {
    return allowedOutputTypes;
  }

  @Override
  public boolean supports(Geometry geometry) {
    return geometry != null && supports.test(geometry);
  }

  @Override
  public Object evaluate(Geometry geometry) throws Exception {
    return evaluator.evaluate(geometry);
  }
}
