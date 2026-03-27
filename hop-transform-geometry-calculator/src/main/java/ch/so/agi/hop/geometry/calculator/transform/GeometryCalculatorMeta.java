package ch.so.agi.hop.geometry.calculator.transform;

import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorErrorMode;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorFunction;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorFunctionRegistry;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorOperationId;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorOutputType;
import ch.so.agi.hop.geometry.calculator.core.GeometryFieldSelection;
import ch.so.agi.hop.geometry.calculator.core.GeometryFieldSelectionResolver;
import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

@Transform(
    id = "GEOMETRY_CALCULATOR_TRANSFORM",
    name = "Geometry Calculator",
    description = "Row-wise geometry measurements, coordinates, extent, and metadata calculations",
    image = "ch/so/agi/hop/geometry/calculator/transform/icons/geometry-calculator.svg",
    categoryDescription = "Geospatial",
    documentationUrl = "",
    keywords = {"geospatial", "geometry", "calculator", "area", "length", "srid"})
public class GeometryCalculatorMeta
    extends BaseTransformMeta<GeometryCalculator, GeometryCalculatorData> {

  private static final GeometryFieldSelectionResolver GEOMETRY_FIELD_SELECTION_RESOLVER =
      new GeometryFieldSelectionResolver();

  @HopMetadataProperty private String inputGeometryFieldName;
  @HopMetadataProperty private String operationId;
  @HopMetadataProperty private String outputFieldName;
  @HopMetadataProperty private GeometryCalculatorOutputType outputType;
  @HopMetadataProperty private GeometryCalculatorErrorMode errorMode;

  @Override
  public void setDefault() {
    inputGeometryFieldName = "";
    operationId = GeometryCalculatorOperationId.AREA.name();
    outputFieldName = "area";
    outputType = GeometryCalculatorOutputType.NUMBER;
    errorMode = GeometryCalculatorErrorMode.RETURN_NULL;
  }

  public void validate(IRowMeta inputRowMeta) throws HopTransformException {
    if (inputRowMeta == null) {
      throw new HopTransformException("No input row metadata available.");
    }

    GeometryFieldSelection selection =
        GEOMETRY_FIELD_SELECTION_RESOLVER.resolve(inputRowMeta, inputGeometryFieldName);
    if (!selection.hasSelection()) {
      throw new HopTransformException("Input geometry field is missing or invalid.");
    }

    GeometryCalculatorFunction function = function();
    if (outputFieldName == null || outputFieldName.isBlank()) {
      throw new HopTransformException("Output field name is required.");
    }
    if (inputRowMeta.indexOfValue(outputFieldName) >= 0) {
      throw new HopTransformException("Output field already exists on the input row.");
    }
    if (outputType == null) {
      throw new HopTransformException("Output type is required.");
    }
    if (!function.allowedOutputTypes().contains(outputType)) {
      throw new HopTransformException(
          "Output type "
              + outputType
              + " is not supported for operation "
              + function.id().name()
              + ".");
    }
  }

  @Override
  public void getFields(
      IRowMeta rowMeta,
      String origin,
      IRowMeta[] info,
      TransformMeta nextTransform,
      IVariables variables,
      IHopMetadataProvider metadataProvider)
      throws HopTransformException {
    IRowMeta safeRowMeta = rowMeta == null ? new RowMeta() : rowMeta;
    validate(safeRowMeta);
    IValueMeta valueMeta = getOutputType().createValueMeta(getOutputFieldName());
    valueMeta.setOrigin(origin);
    safeRowMeta.addValueMeta(valueMeta);
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transformMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    if (input == null || input.length == 0) {
      remarks.add(error("No input received from upstream transforms.", transformMeta));
      return;
    }
    if (prev == null) {
      remarks.add(error("No input row metadata is available from upstream transforms.", transformMeta));
      return;
    }

    GeometryFieldSelection selection =
        GEOMETRY_FIELD_SELECTION_RESOLVER.resolve(prev, inputGeometryFieldName);
    if (!selection.warning().isBlank()) {
      remarks.add(warn(selection.warning(), transformMeta));
    }

    try {
      validate(prev);
      remarks.add(ok("Geometry calculator configuration looks valid.", transformMeta));
    } catch (HopTransformException e) {
      remarks.add(error(e.getMessage(), transformMeta));
    }
  }

  public List<GeometryCalculatorFunction> listOperations() {
    return GeometryCalculatorFunctionRegistry.list();
  }

  public GeometryCalculatorFunction function() throws HopTransformException {
    return GeometryCalculatorFunctionRegistry.find(operationId)
        .orElseThrow(
            () -> new HopTransformException("Unknown geometry calculator operation: " + operationId));
  }

  private ICheckResult error(String message, TransformMeta transformMeta) {
    return new CheckResult(ICheckResult.TYPE_RESULT_ERROR, message, transformMeta);
  }

  private ICheckResult warn(String message, TransformMeta transformMeta) {
    return new CheckResult(ICheckResult.TYPE_RESULT_WARNING, message, transformMeta);
  }

  private ICheckResult ok(String message, TransformMeta transformMeta) {
    return new CheckResult(ICheckResult.TYPE_RESULT_OK, message, transformMeta);
  }

  public String getInputGeometryFieldName() {
    return inputGeometryFieldName;
  }

  public void setInputGeometryFieldName(String inputGeometryFieldName) {
    this.inputGeometryFieldName = inputGeometryFieldName;
  }

  public String getOperationId() {
    return operationId;
  }

  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  public String getOutputFieldName() {
    return outputFieldName;
  }

  public void setOutputFieldName(String outputFieldName) {
    this.outputFieldName = outputFieldName;
  }

  public GeometryCalculatorOutputType getOutputType() {
    return outputType == null ? GeometryCalculatorOutputType.NUMBER : outputType;
  }

  public void setOutputType(GeometryCalculatorOutputType outputType) {
    this.outputType = outputType;
  }

  public GeometryCalculatorErrorMode getErrorMode() {
    return errorMode == null ? GeometryCalculatorErrorMode.RETURN_NULL : errorMode;
  }

  public void setErrorMode(GeometryCalculatorErrorMode errorMode) {
    this.errorMode = errorMode;
  }
}
