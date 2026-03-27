package ch.so.agi.hop.geometry.calculator.transform;

import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorErrorMode;
import ch.so.agi.hop.geometry.calculator.core.GeometryFieldSelectionResolver;
import ch.so.agi.hop.geometry.calculator.core.GeometryInputResult;
import ch.so.agi.hop.geometry.calculator.core.ScalarResultConverter;
import java.util.stream.Collectors;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.locationtech.jts.geom.Geometry;

public class GeometryCalculator
    extends BaseTransform<GeometryCalculatorMeta, GeometryCalculatorData> {

  private static final GeometryFieldSelectionResolver GEOMETRY_FIELD_SELECTION_RESOLVER =
      new GeometryFieldSelectionResolver();

  public GeometryCalculator(
      TransformMeta transformMeta,
      GeometryCalculatorMeta meta,
      GeometryCalculatorData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  @Override
  public boolean processRow() throws HopException {
    Object[] row = getRow();
    if (row == null) {
      logReturnNullSummary();
      setOutputDone();
      return false;
    }

    if (first) {
      first = false;
      initializeFromInputRowMeta();
    }

    Object[] outputRow = RowDataUtil.resizeArray(row, data.outputRowMeta.size());
    outputRow[data.outputFieldIndex] = evaluateRow(row);
    putRow(data.outputRowMeta, outputRow);
    return true;
  }

  private void initializeFromInputRowMeta() throws HopTransformException {
    data.function = meta.function();
    data.outputRowMeta = (IRowMeta) getInputRowMeta().clone();
    meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);
    data.inputGeometryFieldIndex =
        GEOMETRY_FIELD_SELECTION_RESOLVER.requireFieldIndex(
            getInputRowMeta(), meta.getInputGeometryFieldName(), "Input geometry field");
    data.outputFieldIndex = data.outputRowMeta.indexOfValue(meta.getOutputFieldName());
    if (data.outputFieldIndex < 0) {
      throw new HopTransformException(
          "Output field was not found on the output row: " + meta.getOutputFieldName());
    }
  }

  private Object evaluateRow(Object[] row) throws HopException {
    IValueMeta valueMeta = getInputRowMeta().getValueMeta(data.inputGeometryFieldIndex);
    Object value = data.inputGeometryFieldIndex < row.length ? row[data.inputGeometryFieldIndex] : null;
    GeometryInputResult inputResult = data.inputReader.read(valueMeta, value);

    if (inputResult.nullValue()) {
      return null;
    }
    if (inputResult.hasFailure()) {
      return handleFailure("parse failure", "Unable to parse geometry input", inputResult.failure());
    }

    Geometry geometry = inputResult.geometry();
    if (!data.function.supports(geometry)) {
      return handleFailure(
          unsupportedReason(geometry), "Geometry is not compatible with the configured operation", null);
    }

    try {
      Object scalarValue = data.function.evaluate(geometry);
      return ScalarResultConverter.convert(scalarValue, meta.getOutputType());
    } catch (Exception e) {
      return handleFailure("evaluation failure", "Unable to evaluate geometry operation", e);
    }
  }

  private Object handleFailure(String reason, String message, Exception cause) throws HopException {
    String detail =
        message
            + " '"
            + data.function.id().name()
            + "' on field '"
            + getInputRowMeta().getValueMeta(data.inputGeometryFieldIndex).getName()
            + "'";
    if (meta.getErrorMode() == GeometryCalculatorErrorMode.FAIL) {
      if (cause == null) {
        throw new HopException(detail);
      }
      throw new HopException(detail + ": " + rootCauseMessage(cause), cause);
    }

    data.errorCounts.merge(reason, 1L, Long::sum);
    return null;
  }

  private String unsupportedReason(Geometry geometry) {
    if (geometry == null) {
      return "null geometry";
    }
    if (geometry.isEmpty()) {
      return "empty geometry";
    }
    if ((data.function.id().name().startsWith("CENTROID")) && !geometry.isValid()) {
      return "invalid geometry";
    }
    return "incompatible geometry type";
  }

  private void logReturnNullSummary() {
    if (meta.getErrorMode() != GeometryCalculatorErrorMode.RETURN_NULL || data.errorCounts.isEmpty()) {
      return;
    }
    String summary =
        data.errorCounts.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
    logBasic(
        "Geometry Calculator RETURN_NULL summary for "
            + data.function.id().name()
            + ": "
            + summary);
  }

  private String rootCauseMessage(Throwable throwable) {
    Throwable current = throwable;
    while (current.getCause() != null && current.getCause() != current) {
      current = current.getCause();
    }
    if (current.getMessage() == null || current.getMessage().isBlank()) {
      return current.getClass().getName();
    }
    return current.getClass().getSimpleName() + ": " + current.getMessage();
  }
}
