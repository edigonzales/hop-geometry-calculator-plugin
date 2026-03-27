package ch.so.agi.hop.geometry.calculator.core;

import java.util.Set;
import org.locationtech.jts.geom.Geometry;

public interface GeometryCalculatorFunction {

  GeometryCalculatorOperationId id();

  String label();

  String description();

  String geometryHint();

  GeometryCalculatorCategory category();

  GeometryCalculatorOutputType defaultOutputType();

  Set<GeometryCalculatorOutputType> allowedOutputTypes();

  boolean supports(Geometry geometry);

  Object evaluate(Geometry geometry) throws Exception;

  default String displayLabel() {
    return label() + " (" + category().getLabel() + ")";
  }
}
