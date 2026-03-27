package ch.so.agi.hop.geometry.calculator.transform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorErrorMode;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorOutputType;
import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeometryCalculatorMetaTest {

  @BeforeEach
  void initHop() throws Exception {
    HopEnvironment.init();
  }

  @AfterEach
  void resetHop() {
    HopEnvironment.reset();
  }

  @Test
  void xmlRoundTripPreservesConfiguration() throws Exception {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geom");
    meta.setOperationId("CENTROID_X");
    meta.setOutputFieldName("centroid_x");
    meta.setOutputType(GeometryCalculatorOutputType.STRING);
    meta.setErrorMode(GeometryCalculatorErrorMode.FAIL);

    String xml = "<" + TransformMeta.XML_TAG + ">" + meta.getXml() + "</" + TransformMeta.XML_TAG + ">";

    GeometryCalculatorMeta copy = new GeometryCalculatorMeta();
    copy.loadXml(XmlHandler.loadXmlString(xml, TransformMeta.XML_TAG), null);

    assertThat(copy.getInputGeometryFieldName()).isEqualTo("geom");
    assertThat(copy.getOperationId()).isEqualTo("CENTROID_X");
    assertThat(copy.getOutputFieldName()).isEqualTo("centroid_x");
    assertThat(copy.getOutputType()).isEqualTo(GeometryCalculatorOutputType.STRING);
    assertThat(copy.getErrorMode()).isEqualTo(GeometryCalculatorErrorMode.FAIL);
  }

  @Test
  void getFieldsAppendsConfiguredOutputType() throws Exception {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry");
    meta.setOperationId("IS_VALID");
    meta.setOutputFieldName("is_valid");
    meta.setOutputType(GeometryCalculatorOutputType.BOOLEAN);

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaGeometry("geometry"));

    meta.getFields(rowMeta, "origin", null, null, new Variables(), null);

    assertThat(rowMeta.size()).isEqualTo(2);
    assertThat(rowMeta.getValueMeta(1).getName()).isEqualTo("is_valid");
    assertThat(rowMeta.getValueMeta(1).getTypeDesc()).isEqualTo("Boolean");
  }

  @Test
  void checkRejectsDuplicateOutputField() {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry");
    meta.setOperationId("AREA");
    meta.setOutputFieldName("geometry");

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaGeometry("geometry"));

    List<ICheckResult> remarks = new ArrayList<>();
    meta.check(
        remarks,
        null,
        null,
        rowMeta,
        new String[] {"upstream"},
        new String[0],
        null,
        new Variables(),
        null);

    assertThat(remarks)
        .extracting(ICheckResult::getText)
        .anySatisfy(text -> assertThat(text).contains("Output field already exists on the input row."));
  }

  @Test
  void checkRejectsUnsupportedOutputType() {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry");
    meta.setOperationId("GEOMETRY_TYPE");
    meta.setOutputFieldName("geometry_type");
    meta.setOutputType(GeometryCalculatorOutputType.NUMBER);

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaGeometry("geometry"));

    List<ICheckResult> remarks = new ArrayList<>();
    meta.check(
        remarks,
        null,
        null,
        rowMeta,
        new String[] {"upstream"},
        new String[0],
        null,
        new Variables(),
        null);

    assertThat(remarks)
        .extracting(ICheckResult::getText)
        .anySatisfy(
            text ->
                assertThat(text)
                    .contains("Output type NUMBER is not supported for operation GEOMETRY_TYPE."));
  }

  @Test
  void validateAllowsSupportedOutputTypesForNewFunctions() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaGeometry("geometry"));

    GeometryCalculatorMeta validReason = new GeometryCalculatorMeta();
    validReason.setDefault();
    validReason.setInputGeometryFieldName("geometry");
    validReason.setOperationId("IS_VALID_REASON");
    validReason.setOutputFieldName("is_valid_reason");
    validReason.setOutputType(GeometryCalculatorOutputType.STRING);

    GeometryCalculatorMeta isSimple = new GeometryCalculatorMeta();
    isSimple.setDefault();
    isSimple.setInputGeometryFieldName("geometry");
    isSimple.setOperationId("IS_SIMPLE");
    isSimple.setOutputFieldName("is_simple");
    isSimple.setOutputType(GeometryCalculatorOutputType.BOOLEAN);

    GeometryCalculatorMeta width = new GeometryCalculatorMeta();
    width.setDefault();
    width.setInputGeometryFieldName("geometry");
    width.setOperationId("WIDTH");
    width.setOutputFieldName("width");
    width.setOutputType(GeometryCalculatorOutputType.NUMBER);

    assertThatCode(() -> validReason.validate(rowMeta)).doesNotThrowAnyException();
    assertThatCode(() -> isSimple.validate(rowMeta)).doesNotThrowAnyException();
    assertThatCode(() -> width.validate(rowMeta)).doesNotThrowAnyException();
  }

  @Test
  void checkRejectsUnsupportedOutputTypeForWidth() {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry");
    meta.setOperationId("WIDTH");
    meta.setOutputFieldName("width");
    meta.setOutputType(GeometryCalculatorOutputType.BOOLEAN);

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaGeometry("geometry"));

    List<ICheckResult> remarks = new ArrayList<>();
    meta.check(
        remarks,
        null,
        null,
        rowMeta,
        new String[] {"upstream"},
        new String[0],
        null,
        new Variables(),
        null);

    assertThat(remarks)
        .extracting(ICheckResult::getText)
        .anySatisfy(
            text ->
                assertThat(text).contains("Output type BOOLEAN is not supported for operation WIDTH."));
  }
}
