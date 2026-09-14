# Geometry validity

Reads WKT geometries and adds the `is_valid` field with the `IS_VALID` operation.

- Pipeline: `geometry-validity.hpl`
- Input: `data/geometries.csv` (`;` separated, one `geometry` column with WKT geometries)
- Parameters: `INPUT_CSV`, `OUTPUT_CSV`
- Calculation: `IS_VALID`, output field `is_valid`, output type `BOOLEAN`, error mode `RETURN_NULL`

## Expected result

| geometry | is_valid |
| --- | --- |
| `POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))` | Y |
| `POLYGON ((0 0, 2 2, 2 0, 0 2, 0 0))` | N |
| `POLYGON EMPTY` | Y |

The second polygon is a self-intersecting bowtie and therefore invalid. Empty geometries are valid,
but they stay distinguishable from `null` through the `IS_EMPTY` operation.

Text file output writes boolean values as `Y`/`N`. Chain a second Geometry Calculator with
`IS_VALID_REASON` or `IS_VALID_ERROR_TYPE` to get diagnostics for the invalid rows.
See the [Geometry Calculator reference](../../docs/transforms/geometry-calculator.adoc) for all
options.
