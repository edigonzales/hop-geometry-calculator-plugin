# hop-geometry-calculator-plugin

Apache Hop 2.17 transform plugin that adds a dedicated `Geometry Calculator` for row-wise GIS
field calculations on native geometry values and geometry-compatible WKT/WKB inputs.

## Implemented scope

- Single-input, row-wise `Geometry Calculator` transform
- One configured calculation per transform
- Supported operations:
  - `AREA`
  - `LENGTH`
  - `PERIMETER`
  - `WIDTH`
  - `HEIGHT`
  - `X`
  - `Y`
  - `Z`
  - `CENTROID_X`
  - `CENTROID_Y`
  - `INTERIOR_POINT_X`
  - `INTERIOR_POINT_Y`
  - `XMIN`
  - `YMIN`
  - `XMAX`
  - `YMAX`
  - `GEOMETRY_TYPE`
  - `NUM_POINTS`
  - `NUM_GEOMETRIES`
  - `IS_EMPTY`
  - `IS_VALID`
  - `IS_VALID_REASON`
  - `IS_VALID_ERROR_TYPE`
  - `IS_VALID_LOCATION_X`
  - `IS_VALID_LOCATION_Y`
  - `IS_SIMPLE`
  - `IS_SIMPLE_LOCATION_X`
  - `IS_SIMPLE_LOCATION_Y`
  - `SRID`
- Input geometry detection:
  - native `ValueMetaGeometry`
  - geometry-compatible `String` / `Binary` fields with `geom|geometry|wkt|wkb` naming
  - WKT / EWKT / WKB bytes / WKB hex parsing
- Output types:
  - metric / coordinate values: `NUMBER`, `STRING`
  - counts and `SRID`: `INTEGER`, `NUMBER`, `STRING`
  - boolean values: `BOOLEAN`, `STRING`
  - diagnostic text and `GEOMETRY_TYPE`: `STRING`
- Error modes:
  - `RETURN_NULL` (default)
  - `FAIL`

## Modules

- `./hop-geometry-calculator-core`
  - Geometry input parsing, field detection, function registry, scalar conversion.
- `./hop-transform-geometry-calculator`
  - Apache Hop transform, dialog, metadata, runtime, icon.
- `./assemblies/assemblies-hop-geometry-calculator`
  - Install ZIP assembly under `plugins/transforms/hop-geometry-calculator`.

## Build

Full build:

```bash
mvn clean verify
```

Fast build without tests:

```bash
mvn -DskipTests package
```

Build prerequisites:

- Java 17
- Maven
- access to Maven Central and the configured `sogeo` repositories
- `hop-geometry-type` reachable in the local/remote Maven repositories

## Produced artifacts

After `mvn package`:

- Core JAR:
  `hop-geometry-calculator-core/target/hop-geometry-calculator-core-<version>.jar`
- Transform JAR:
  `hop-transform-geometry-calculator/target/hop-transform-geometry-calculator-<version>.jar`
- Plugin ZIP:
  `assemblies/assemblies-hop-geometry-calculator/target/hop-geometry-calculator-plugin-<version>.zip`

The ZIP installs to:

```text
plugins/transforms/hop-geometry-calculator
```

## Install in Hop

Manual install:

```bash
unzip -o assemblies/assemblies-hop-geometry-calculator/target/hop-geometry-calculator-plugin-<version>.zip -d "$HOP_HOME"
```

Fast local sync:

```bash
./scripts/dev-sync-hop-plugin.sh "$HOP_HOME"
```

If `HOP_HOME` is exported:

```bash
./scripts/dev-sync-hop-plugin.sh
```

## Function semantics

Geometry calculations use the units of the input geometry / CRS as-is. The plugin does not
reproject and does not perform unit conversion.

### Geometry family rules

- `AREA`, `PERIMETER`: polygonal geometries only
- `LENGTH`: lineal and polygonal geometries
- `WIDTH`, `HEIGHT`: any parseable geometry, return `null` for empty geometries
- `X`, `Y`, `Z`: non-empty points only
- `CENTROID_X`, `CENTROID_Y`: non-empty valid geometries
- `INTERIOR_POINT_X`, `INTERIOR_POINT_Y`: any parseable geometry, return `null` for empty geometries
- `XMIN`, `YMIN`, `XMAX`, `YMAX`: any non-empty parseable geometry
- `GEOMETRY_TYPE`, `NUM_POINTS`, `NUM_GEOMETRIES`, `IS_EMPTY`, `IS_VALID`, `IS_SIMPLE`, `SRID`:
  any parseable geometry, including empty geometries
- `IS_VALID_REASON`, `IS_VALID_ERROR_TYPE`, `IS_VALID_LOCATION_X`, `IS_VALID_LOCATION_Y`:
  any parseable geometry; valid geometries return `null` for the diagnostic detail outputs
- `IS_SIMPLE_LOCATION_X`, `IS_SIMPLE_LOCATION_Y`:
  any parseable geometry; simple geometries return `null`

### Null, empty, invalid, and parse failures

- `null` input values produce `null` output values
- empty geometries stay distinguishable from `null`
- `IS_EMPTY` returns `true` only for empty geometries
- `IS_VALID` delegates to JTS validation
- `IS_VALID_REASON` uses the JTS `TopologyValidationError#getMessage()` text
- `IS_VALID_ERROR_TYPE` uses stable symbolic names such as `SELF_INTERSECTION` or `TOO_FEW_POINTS`
- validation and simplicity locations are exposed only as scalar `X` / `Y` values; no geometry output is produced
- `SRID` returns `null` for `<= 0`
- `Z` returns `null` when a point has no Z ordinate
- `WIDTH`, `HEIGHT`, `INTERIOR_POINT_X`, and `INTERIOR_POINT_Y` return `null` for empty geometries
- in `RETURN_NULL` mode, parse/type/compatibility failures return `null` and the transform logs
  one summary line at the end
- in `FAIL` mode, the first such failure raises a `HopException`

## Output type matrix

| Operation group | Allowed output types |
| --- | --- |
| `AREA`, `LENGTH`, `PERIMETER`, `WIDTH`, `HEIGHT`, `X`, `Y`, `Z`, `CENTROID_X`, `CENTROID_Y`, `INTERIOR_POINT_X`, `INTERIOR_POINT_Y`, `XMIN`, `YMIN`, `XMAX`, `YMAX`, `IS_VALID_LOCATION_X`, `IS_VALID_LOCATION_Y`, `IS_SIMPLE_LOCATION_X`, `IS_SIMPLE_LOCATION_Y` | `NUMBER`, `STRING` |
| `NUM_POINTS`, `NUM_GEOMETRIES`, `SRID` | `INTEGER`, `NUMBER`, `STRING` |
| `IS_EMPTY`, `IS_VALID`, `IS_SIMPLE` | `BOOLEAN`, `STRING` |
| `GEOMETRY_TYPE`, `IS_VALID_REASON`, `IS_VALID_ERROR_TYPE` | `STRING` |

## Tests

The automated test suite covers:

- WKT / EWKT / WKB geometry input parsing
- scalar conversion and output typing
- function semantics for point, line, polygon, multi, empty, invalid, and `null` geometries
- validation reason/error type/location diagnostics
- simplicity diagnostics
- width, height, and interior-point helper functions
- transform metadata defaults, validation, output metadata, and XML roundtrip
- transform runtime for both `RETURN_NULL` and `FAIL`

Run tests only:

```bash
mvn test
```

## Development notes

- The plugin is intentionally append-only in v1.
- Multiple calculations per transform are out of scope for this first version.
- No formula parser, CRS transformation, or geoprocessing overlay/edit operations are included.
