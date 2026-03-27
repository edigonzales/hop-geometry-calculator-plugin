package ch.so.agi.hop.geometry.calculator.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.hop.core.row.value.ValueMetaString;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

class GeometryInputReaderTest {

  private final GeometryInputReader reader = new GeometryInputReader();

  @Test
  void parsesWktEwktAndWkb() throws Exception {
    ValueMetaString valueMeta = new ValueMetaString("geom");

    GeometryInputResult wktResult = reader.read(valueMeta, "POINT (1 2)");
    assertThat(wktResult.failure()).isNull();
    assertThat(wktResult.geometry()).isNotNull();
    assertThat(wktResult.geometry().getGeometryType()).isEqualTo("Point");

    GeometryInputResult ewktResult = reader.read(valueMeta, "SRID=2056;POINT (7 8)");
    assertThat(ewktResult.geometry()).isNotNull();
    assertThat(ewktResult.geometry().getSRID()).isEqualTo(2056);

    Geometry source = new WKTReader().read("LINESTRING (0 0, 1 1)");
    byte[] wkbBytes = new WKBWriter().write(source);
    GeometryInputResult bytesResult = reader.read(valueMeta, wkbBytes);
    assertThat(bytesResult.geometry()).isNotNull();
    assertThat(bytesResult.geometry().getGeometryType()).isEqualTo("LineString");

    GeometryInputResult hexResult = reader.read(valueMeta, toHex(wkbBytes));
    assertThat(hexResult.geometry()).isNotNull();
    assertThat(hexResult.geometry().getGeometryType()).isEqualTo("LineString");
  }

  @Test
  void keepsEmptyGeometryDistinctFromNull() {
    GeometryInputResult result = reader.read(new ValueMetaString("geom"), "POINT EMPTY");

    assertThat(result.nullValue()).isFalse();
    assertThat(result.failure()).isNull();
    assertThat(result.geometry()).isNotNull();
    assertThat(result.geometry().isEmpty()).isTrue();
  }

  @Test
  void returnsFailureForInvalidGeometryText() {
    GeometryInputResult result = reader.read(new ValueMetaString("geom"), "not a geometry");

    assertThat(result.nullValue()).isFalse();
    assertThat(result.geometry()).isNull();
    assertThat(result.failure()).isNotNull();
  }

  private static String toHex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      builder.append(String.format("%02x", value));
    }
    return builder.toString();
  }
}
