package ch.so.agi.hop.geometry.calculator.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

class GeometryCalculatorFunctionRegistryTest {

  private final WKTReader wktReader = new WKTReader();

  @Test
  void areaAndPerimeterWorkForPolygons() throws Exception {
    Geometry polygon = wktReader.read("POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");

    GeometryCalculatorFunction area =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.AREA).orElseThrow();
    GeometryCalculatorFunction perimeter =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.PERIMETER).orElseThrow();

    assertThat(area.supports(polygon)).isTrue();
    assertThat(area.evaluate(polygon)).isEqualTo(4.0d);
    assertThat(perimeter.supports(polygon)).isTrue();
    assertThat(perimeter.evaluate(polygon)).isEqualTo(8.0d);
  }

  @Test
  void coordinateAndExtentFunctionsFollowSemantics() throws Exception {
    Geometry point = wktReader.read("POINT Z (1 2 3)");
    Geometry empty = wktReader.read("POINT EMPTY");
    Geometry invalid = wktReader.read("POLYGON ((0 0, 2 2, 2 0, 0 2, 0 0))");
    invalid.setSRID(2056);

    GeometryCalculatorFunction x =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.X).orElseThrow();
    GeometryCalculatorFunction z =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.Z).orElseThrow();
    GeometryCalculatorFunction xmin =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.XMIN).orElseThrow();
    GeometryCalculatorFunction centroidX =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.CENTROID_X).orElseThrow();
    GeometryCalculatorFunction isEmpty =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_EMPTY).orElseThrow();
    GeometryCalculatorFunction srid =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.SRID).orElseThrow();

    assertThat(x.evaluate(point)).isEqualTo(1.0d);
    assertThat(z.evaluate(point)).isEqualTo(3.0d);
    assertThat(xmin.evaluate(invalid)).isEqualTo(0.0d);
    assertThat(centroidX.supports(invalid)).isFalse();
    assertThat(isEmpty.evaluate(empty)).isEqualTo(true);
    assertThat(srid.evaluate(invalid)).isEqualTo(2056L);
  }

  @Test
  void structureFunctionsWorkForCollectionsAndInvalidGeometries() throws Exception {
    Geometry multi =
        wktReader.read("MULTIPOINT ((0 0), (1 1), (2 2))");
    Geometry invalid =
        wktReader.read("POLYGON ((0 0, 2 2, 2 0, 0 2, 0 0))");

    GeometryCalculatorFunction numPoints =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.NUM_POINTS).orElseThrow();
    GeometryCalculatorFunction numGeometries =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.NUM_GEOMETRIES).orElseThrow();
    GeometryCalculatorFunction isValid =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.IS_VALID).orElseThrow();
    GeometryCalculatorFunction geometryType =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.GEOMETRY_TYPE).orElseThrow();

    assertThat(numPoints.evaluate(multi)).isEqualTo(3L);
    assertThat(numGeometries.evaluate(multi)).isEqualTo(3L);
    assertThat(isValid.evaluate(invalid)).isEqualTo(false);
    assertThat(geometryType.evaluate(multi)).isEqualTo("MultiPoint");
  }
}
