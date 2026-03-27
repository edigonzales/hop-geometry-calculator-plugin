package ch.so.agi.hop.geometry.calculator.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

class GeometryCalculatorDiagnosticsTest {

  private final WKTReader wktReader = new WKTReader();
  private final GeometryFactory geometryFactory = new GeometryFactory();

  @Test
  void validationDiagnosticsReturnNullForValidGeometryAndDetailsForSelfIntersection()
      throws Exception {
    Geometry valid = wktReader.read("POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
    Geometry invalid = wktReader.read("POLYGON ((0 0, 2 2, 2 0, 0 2, 0 0))");

    GeometryCalculatorFunction reason =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_VALID_REASON)
            .orElseThrow();
    GeometryCalculatorFunction errorType =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_VALID_ERROR_TYPE)
            .orElseThrow();
    GeometryCalculatorFunction x =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_VALID_LOCATION_X)
            .orElseThrow();
    GeometryCalculatorFunction y =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_VALID_LOCATION_Y)
            .orElseThrow();

    assertThat(reason.evaluate(valid)).isNull();
    assertThat(errorType.evaluate(valid)).isNull();
    assertThat(x.evaluate(valid)).isNull();
    assertThat(y.evaluate(valid)).isNull();

    assertThat((String) reason.evaluate(invalid)).contains("Self-intersection");
    assertThat(errorType.evaluate(invalid)).isEqualTo("SELF_INTERSECTION");
    assertThat(x.evaluate(invalid)).isEqualTo(1.0d);
    assertThat(y.evaluate(invalid)).isEqualTo(1.0d);
  }

  @Test
  void validationErrorTypeCanReportTooFewPoints() throws Exception {
    Geometry tooFewPoints =
        geometryFactory.createPolygon(
            geometryFactory.createLinearRing(
                new Coordinate[] {
                  new Coordinate(0, 0),
                  new Coordinate(1, 0),
                  new Coordinate(0, 0),
                  new Coordinate(0, 0)
                }));

    GeometryCalculatorFunction errorType =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_VALID_ERROR_TYPE)
            .orElseThrow();

    assertThat(errorType.evaluate(tooFewPoints)).isEqualTo("TOO_FEW_POINTS");
  }

  @Test
  void simplicityDiagnosticsReturnLocationOnlyForNonSimpleGeometry() throws Exception {
    Geometry simple = wktReader.read("LINESTRING (0 0, 4 0)");
    Geometry nonSimple = wktReader.read("LINESTRING (0 0, 2 2, 0 2, 2 0)");

    GeometryCalculatorFunction isSimple =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_SIMPLE).orElseThrow();
    GeometryCalculatorFunction x =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_SIMPLE_LOCATION_X)
            .orElseThrow();
    GeometryCalculatorFunction y =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_SIMPLE_LOCATION_Y)
            .orElseThrow();

    assertThat(isSimple.evaluate(simple)).isEqualTo(true);
    assertThat(x.evaluate(simple)).isNull();
    assertThat(y.evaluate(simple)).isNull();

    assertThat(isSimple.evaluate(nonSimple)).isEqualTo(false);
    assertThat(x.evaluate(nonSimple)).isEqualTo(1.0d);
    assertThat(y.evaluate(nonSimple)).isEqualTo(1.0d);
  }

  @Test
  void widthHeightAndInteriorPointCoverPolygonLinePointAndEmpty() throws Exception {
    Geometry polygon = wktReader.read("POLYGON ((0 0, 4 0, 4 3, 0 3, 0 0))");
    Geometry line = wktReader.read("LINESTRING (0 0, 4 0)");
    Geometry point = wktReader.read("POINT (5 6)");
    Geometry empty = wktReader.read("POLYGON EMPTY");

    GeometryCalculatorFunction width =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.WIDTH).orElseThrow();
    GeometryCalculatorFunction height =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.HEIGHT).orElseThrow();
    GeometryCalculatorFunction interiorX =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.INTERIOR_POINT_X)
            .orElseThrow();
    GeometryCalculatorFunction interiorY =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.INTERIOR_POINT_Y)
            .orElseThrow();

    assertThat(width.evaluate(polygon)).isEqualTo(4.0d);
    assertThat(height.evaluate(polygon)).isEqualTo(3.0d);
    assertThat(interiorX.evaluate(polygon)).isNotNull();
    assertThat(interiorY.evaluate(polygon)).isNotNull();
    assertThat(interiorX.evaluate(line)).isEqualTo(0.0d);
    assertThat(interiorY.evaluate(line)).isEqualTo(0.0d);
    assertThat(interiorX.evaluate(point)).isEqualTo(5.0d);
    assertThat(interiorY.evaluate(point)).isEqualTo(6.0d);
    assertThat(width.evaluate(empty)).isNull();
    assertThat(height.evaluate(empty)).isNull();
    assertThat(interiorX.evaluate(empty)).isNull();
    assertThat(interiorY.evaluate(empty)).isNull();
  }
}
