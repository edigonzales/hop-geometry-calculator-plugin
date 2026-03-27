package ch.so.agi.hop.geometry.calculator.transform;

import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorErrorMode;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorFunction;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorOutputType;
import ch.so.agi.hop.geometry.calculator.core.GeometryFieldSelection;
import ch.so.agi.hop.geometry.calculator.core.GeometryFieldSelectionResolver;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GeometryCalculatorDialog extends BaseTransformDialog {

  private static final GeometryFieldSelectionResolver GEOMETRY_FIELD_SELECTION_RESOLVER =
      new GeometryFieldSelectionResolver();

  private final GeometryCalculatorMeta input;
  private final List<GeometryCalculatorFunction> operations;

  private Combo wInputGeometryField;
  private Combo wOperation;
  private Combo wOutputType;
  private Combo wErrorMode;
  private Text wOutputField;
  private Label wDescription;
  private Label wGeometryHint;
  private Label wFieldStatus;
  private Composite content;

  public GeometryCalculatorDialog(
      Shell parent, IVariables variables, GeometryCalculatorMeta transformMeta, PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    this.input = transformMeta;
    this.operations = input.listOperations();
  }

  @Override
  public String open() {
    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    shell.setMinimumSize(760, 560);
    PropsUi.setLook(shell);
    setShellImage(shell, input);
    shell.setText("Geometry Calculator");

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();
    shell.setLayout(formLayout);

    int margin = PropsUi.getMargin();

    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText("Name");
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.right = new FormAttachment(props.getMiddlePct(), -margin);
    fdlTransformName.top = new FormAttachment(0, margin);
    wlTransformName.setLayoutData(fdlTransformName);

    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment(props.getMiddlePct(), 0);
    fdTransformName.right = new FormAttachment(100, 0);
    fdTransformName.top = new FormAttachment(0, margin);
    wTransformName.setLayoutData(fdTransformName);

    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText("OK");
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText("Cancel");
    setButtonPositions(new Button[] {wOk, wCancel}, margin, null);

    content = new Composite(shell, SWT.NONE);
    PropsUi.setLook(content);
    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = margin;
    gridLayout.verticalSpacing = margin;
    content.setLayout(gridLayout);
    FormData fdContent = new FormData();
    fdContent.left = new FormAttachment(0, 0);
    fdContent.right = new FormAttachment(100, 0);
    fdContent.top = new FormAttachment(wTransformName, margin * 2);
    fdContent.bottom = new FormAttachment(wOk, -margin * 2);
    content.setLayoutData(fdContent);

    wInputGeometryField = addCombo("Input geometry field");
    wOperation = addCombo("Operation");
    for (GeometryCalculatorFunction function : operations) {
      wOperation.add(function.displayLabel());
    }
    wOutputField = addText("Output field");
    wOutputType = addCombo("Output type");
    wErrorMode = addCombo("Error mode");
    for (GeometryCalculatorErrorMode mode : GeometryCalculatorErrorMode.values()) {
      wErrorMode.add(mode.name());
    }
    wDescription = addInfoLabel("");
    wGeometryHint = addInfoLabel("");
    wFieldStatus = addInfoLabel("");

    attachListeners();
    loadFieldChoices();
    getData();
    refreshOperationDetails();

    wOk.addListener(SWT.Selection, event -> ok());
    wCancel.addListener(SWT.Selection, event -> cancel());

    BaseDialog.defaultShellHandling(shell, value -> ok(), value -> cancel());
    return transformName;
  }

  private void attachListeners() {
    wTransformName.addModifyListener(event -> input.setChanged());
    wInputGeometryField.addModifyListener(event -> input.setChanged());
    wOperation.addModifyListener(
        event -> {
          input.setChanged();
          refreshOperationDetails();
        });
    wOutputField.addModifyListener(event -> input.setChanged());
    wOutputType.addModifyListener(event -> input.setChanged());
    wErrorMode.addModifyListener(event -> input.setChanged());
  }

  private void loadFieldChoices() {
    List<String> messages = new ArrayList<>();
    wInputGeometryField.removeAll();
    try {
      GeometryFieldSelection selection =
          GEOMETRY_FIELD_SELECTION_RESOLVER.resolve(
              pipelineMeta.getPrevTransformFields(variables, transformName),
              input.getInputGeometryFieldName());
      applySelection(wInputGeometryField, selection);
      collectSelectionMessage(messages, selection.warning());
      if (selection.fieldNames().isEmpty()) {
        messages.add("No geometry field candidates found on the input.");
      }
    } catch (Exception e) {
      messages.add("Unable to inspect upstream fields: " + rootCauseMessage(e));
    }
    setInfoLabel(wFieldStatus, String.join("\n", messages));
  }

  private void getData() {
    wTransformName.setText(transformName == null ? "" : transformName);
    selectOperation(input.getOperationId());
    wOutputField.setText(defaultText(input.getOutputFieldName()));
    wErrorMode.setText(input.getErrorMode().name());
    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void selectOperation(String operationId) {
    for (int index = 0; index < operations.size(); index++) {
      if (operations.get(index).id().name().equals(operationId)) {
        wOperation.select(index);
        return;
      }
    }
    if (!operations.isEmpty()) {
      wOperation.select(0);
    }
  }

  private void refreshOperationDetails() {
    GeometryCalculatorFunction function = currentFunction();
    setInfoLabel(wDescription, function.description());
    setInfoLabel(wGeometryHint, "Supported input: " + function.geometryHint());

    String currentOutputType = wOutputType.getText();
    wOutputType.removeAll();
    for (GeometryCalculatorOutputType outputType : function.allowedOutputTypes()) {
      wOutputType.add(outputType.name());
    }
    if (!currentOutputType.isBlank() && function.allowedOutputTypes().contains(GeometryCalculatorOutputType.valueOf(currentOutputType))) {
      wOutputType.setText(currentOutputType);
    } else {
      wOutputType.setText(
          defaultText(
              input.getOutputType() != null
                      && function.allowedOutputTypes().contains(input.getOutputType())
                  ? input.getOutputType().name()
                  : function.defaultOutputType().name()));
    }

    if (wOutputField.getText().isBlank()) {
      wOutputField.setText(defaultOutputField(function));
    }
    content.layout(true, true);
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }
    if (wInputGeometryField.getText().isBlank()) {
      showValidationWarning("Please select an input geometry field.");
      return;
    }
    if (wOutputField.getText().isBlank()) {
      showValidationWarning("Please enter an output field name.");
      return;
    }
    if (wOutputType.getText().isBlank()) {
      showValidationWarning("Please select an output type.");
      return;
    }

    transformName = wTransformName.getText();
    GeometryCalculatorFunction function = currentFunction();
    input.setInputGeometryFieldName(wInputGeometryField.getText());
    input.setOperationId(function.id().name());
    input.setOutputFieldName(wOutputField.getText());
    input.setOutputType(GeometryCalculatorOutputType.valueOf(wOutputType.getText()));
    input.setErrorMode(GeometryCalculatorErrorMode.valueOf(defaultText(wErrorMode.getText(), GeometryCalculatorErrorMode.RETURN_NULL.name())));
    dispose();
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private GeometryCalculatorFunction currentFunction() {
    int selectionIndex = Math.max(0, wOperation.getSelectionIndex());
    return operations.get(selectionIndex);
  }

  private String defaultOutputField(GeometryCalculatorFunction function) {
    return function.id().name().toLowerCase();
  }

  private Combo addCombo(String labelText) {
    Label label = addLabel(labelText);
    Combo combo = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
    combo.setLayoutData(defaultGridData());
    combo.setData("label", label);
    PropsUi.setLook(combo);
    return combo;
  }

  private Text addText(String labelText) {
    Label label = addLabel(labelText);
    Text text = new Text(content, SWT.SINGLE | SWT.BORDER);
    text.setLayoutData(defaultGridData());
    text.setData("label", label);
    PropsUi.setLook(text);
    return text;
  }

  private Label addLabel(String labelText) {
    Label label = new Label(content, SWT.RIGHT);
    label.setText(labelText);
    label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    PropsUi.setLook(label);
    return label;
  }

  private Label addInfoLabel(String labelText) {
    Label label = new Label(content, SWT.WRAP);
    label.setText(labelText);
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    PropsUi.setLook(label);
    return label;
  }

  private GridData defaultGridData() {
    return new GridData(SWT.FILL, SWT.CENTER, true, false);
  }

  private void applySelection(Combo combo, GeometryFieldSelection selection) {
    combo.setItems(selection.fieldNames().toArray(String[]::new));
    if (selection.hasSelection()) {
      combo.setText(selection.selectedField());
    } else {
      combo.deselectAll();
      combo.clearSelection();
      combo.setText("");
    }
  }

  private void collectSelectionMessage(List<String> messages, String warning) {
    if (warning != null && !warning.isBlank() && !messages.contains(warning)) {
      messages.add(warning);
    }
  }

  private void setInfoLabel(Label label, String text) {
    label.setText(defaultText(text));
    ((GridData) label.getLayoutData()).exclude = text == null || text.isBlank();
    label.setVisible(text != null && !text.isBlank());
  }

  private void showValidationWarning(String message) {
    MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    messageBox.setText("Invalid configuration");
    messageBox.setMessage(message);
    messageBox.open();
  }

  private String defaultText(String value) {
    return value == null ? "" : value;
  }

  private String defaultText(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
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
