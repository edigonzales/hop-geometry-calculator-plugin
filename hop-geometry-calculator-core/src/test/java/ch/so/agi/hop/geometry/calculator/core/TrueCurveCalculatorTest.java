package ch.so.agi.hop.geometry.calculator.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.atolcd.hop.gis.geometry.curve.CircularString;
import com.atolcd.hop.gis.geometry.curve.CompoundCurve;
import com.atolcd.hop.gis.geometry.curve.CurveGeometrySupport;
import com.atolcd.hop.gis.geometry.curve.CurvePolygon;
import com.atolcd.hop.gis.geometry.curve.MultiCurve;
import com.atolcd.hop.gis.geometry.curve.MultiSurface;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.apache.hop.core.row.value.ValueMetaString;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

class TrueCurveCalculatorTest {

  private final GeometryInputReader reader = new GeometryInputReader();

  @Test
  void readsAllSupportedTrueCurveWkbTypesWithoutLinearizingThem() {
    GeometryFactory factory = new GeometryFactory();
    List<Geometry> curves =
        List.of(
            openCircularString(factory),
            compoundCurve(factory),
            curvePolygon(factory),
            multiCurve(factory),
            multiSurface(factory));

    for (Geometry source : curves) {
      source.setSRID(2056);
      GeometryInputResult result =
          reader.read(new ValueMetaString("geometry_wkb"), CurveGeometrySupport.writeWkb(source));

      assertThat(result.failure()).isNull();
      assertThat(result.geometry()).isExactlyInstanceOf(source.getClass());
      assertThat(result.geometry().getSRID()).isEqualTo(2056);
    }
  }

  @Test
  void reportsTrueCurveGeometryTypeNames() throws Exception {
    GeometryFactory factory = new GeometryFactory();
    GeometryCalculatorFunction geometryType =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.GEOMETRY_TYPE)
            .orElseThrow();

    assertThat(geometryType.evaluate(openCircularString(factory))).isEqualTo("CircularString");
    assertThat(geometryType.evaluate(compoundCurve(factory))).isEqualTo("CompoundCurve");
    assertThat(geometryType.evaluate(curvePolygon(factory))).isEqualTo("CurvePolygon");
    assertThat(geometryType.evaluate(multiCurve(factory))).isEqualTo("MultiCurve");
    assertThat(geometryType.evaluate(multiSurface(factory))).isEqualTo("MultiSurface");
  }

  @Test
  void measurementsUseTheInheritedSegmentedJtsRepresentation() throws Exception {
    GeometryFactory factory = new GeometryFactory();
    CircularString circularString = openCircularString(factory);
    CurvePolygon curvePolygon = curvePolygon(factory);

    GeometryCalculatorFunction length =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.LENGTH).orElseThrow();
    GeometryCalculatorFunction area =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.AREA).orElseThrow();
    GeometryCalculatorFunction perimeter =
        GeometryCalculatorFunctionRegistry.find(GeometryCalculatorOperationId.PERIMETER)
            .orElseThrow();

    assertThat(length.supports(circularString)).isTrue();
    assertThat((Double) length.evaluate(circularString)).isEqualTo(circularString.getLength());
    assertThat((Double) length.evaluate(circularString)).isCloseTo(Math.PI, within(0.01d));

    assertThat(area.supports(curvePolygon)).isTrue();
    assertThat((Double) area.evaluate(curvePolygon)).isEqualTo(curvePolygon.getArea());
    assertThat((Double) perimeter.evaluate(curvePolygon))
        .isEqualTo(curvePolygon.getBoundary().getLength());
  }

  @Test
  void preservesForeignCurveAcrossPluginClassloaderBoundary() throws Exception {
    CurvePolygon source = curvePolygon(new GeometryFactory());
    source.setSRID(2056);
    byte[] wkb = CurveGeometrySupport.writeWkb(source);

    try (ForeignCurveGeometryHandle foreignGeometry = ForeignCurveGeometryHandle.create(wkb, 2056)) {
      GeometryInputResult result = reader.read(null, foreignGeometry.geometry());

      assertThat(result.failure()).isNull();
      assertThat(result.geometry()).isInstanceOf(CurvePolygon.class);
      assertThat(result.geometry().getSRID()).isEqualTo(2056);
      CurvePolygon parsed = (CurvePolygon) result.geometry();
      assertThat(parsed.getCurveRings()).hasSize(1);
      assertThat(parsed.getCurveRings().get(0)).isInstanceOf(CircularString.class);
      CircularString ring = (CircularString) parsed.getCurveRings().get(0);
      assertThat(ring.getControlPoints()).hasSize(5);
      assertThat(ring.getControlPoints()[1].getX()).isEqualTo(4.0d);
    }
  }

  private static CircularString openCircularString(GeometryFactory factory) {
    return new CircularString(
        new Coordinate[] {
          new Coordinate(0, 0), new Coordinate(1, 1), new Coordinate(2, 0)
        },
        factory);
  }

  private static CompoundCurve compoundCurve(GeometryFactory factory) {
    LineString tail =
        factory.createLineString(new Coordinate[] {new Coordinate(2, 0), new Coordinate(3, 0)});
    return new CompoundCurve(List.of(openCircularString(factory), tail), factory);
  }

  private static CurvePolygon curvePolygon(GeometryFactory factory) {
    CircularString ring =
        new CircularString(
            new Coordinate[] {
              new Coordinate(0, 0),
              new Coordinate(4, 0),
              new Coordinate(4, 4),
              new Coordinate(0, 4),
              new Coordinate(0, 0)
            },
            factory);
    return new CurvePolygon(List.of(ring), factory);
  }

  private static MultiCurve multiCurve(GeometryFactory factory) {
    return new MultiCurve(
        List.of(
            factory.createLineString(
                new Coordinate[] {new Coordinate(-2, 0), new Coordinate(-1, 0)}),
            openCircularString(factory)),
        factory);
  }

  private static MultiSurface multiSurface(GeometryFactory factory) {
    return new MultiSurface(
        List.of(square(factory, -3, -3, -2, -2), curvePolygon(factory)), factory);
  }

  private static Polygon square(
      GeometryFactory factory, double minX, double minY, double maxX, double maxY) {
    return factory.createPolygon(
        new Coordinate[] {
          new Coordinate(minX, minY),
          new Coordinate(maxX, minY),
          new Coordinate(maxX, maxY),
          new Coordinate(minX, maxY),
          new Coordinate(minX, minY)
        });
  }

  private record ForeignCurveGeometryHandle(URLClassLoader classLoader, Object geometry)
      implements AutoCloseable {
    private static ForeignCurveGeometryHandle create(byte[] wkb, int srid) throws Exception {
      URL geometryTypeLocation =
          CurveGeometrySupport.class.getProtectionDomain().getCodeSource().getLocation();
      URL jtsLocation = Geometry.class.getProtectionDomain().getCodeSource().getLocation();
      URLClassLoader classLoader =
          new URLClassLoader(new URL[] {geometryTypeLocation, jtsLocation}, null);
      Class<?> supportClass =
          Class.forName(
              "com.atolcd.hop.gis.geometry.curve.CurveGeometrySupport", true, classLoader);
      Object geometry = supportClass.getMethod("readWkb", byte[].class).invoke(null, (Object) wkb);
      geometry.getClass().getMethod("setSRID", int.class).invoke(geometry, srid);
      return new ForeignCurveGeometryHandle(classLoader, geometry);
    }

    @Override
    public void close() throws Exception {
      classLoader.close();
    }
  }
}
