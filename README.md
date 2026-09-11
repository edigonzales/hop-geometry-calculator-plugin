# hop-geometry-calculator-plugin

Apache Hop 2.19.0 transform plugin that adds a dedicated `Geometry Calculator` for row-wise GIS
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
  - SQL/MM WKB curve types `CIRCULARSTRING`, `COMPOUNDCURVE`, `CURVEPOLYGON`, `MULTICURVE`, and `MULTISURFACE`
  - binary preservation of curve geometries across isolated plugin classloaders
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

- Java 21 (Java 25 is also covered by the compatibility matrix)
- Maven
- access to Maven Central and `https://jars.interlis.guru`
- `ch.so.agi:hop-geometry-type:0.2.0-SNAPSHOT` reachable in the Maven snapshot repository

The Geometry Type snapshot is a normal Maven `0.2.0-SNAPSHOT` dependency. Maven resolves the
current snapshot through repository metadata; timestamped snapshot versions are never pinned in
this repository. Geometry Type and JTS are provided by the separately installed Geometry Type
plugin and are deliberately not duplicated in the Calculator ZIP.

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

## CI and publication

The GitHub Actions matrix runs Java 21 and 25 on Ubuntu, macOS and Windows. Ubuntu with Java 21
is the canonical run: it executes `clean verify`, validates the installation ZIP and creates the
only publishable bundle. The other five jobs execute compatibility tests with `clean test`.

The canonical job builds against the current `hop-geometry-type-plugin` main snapshot. The
Installed-Hop E2E installs both ZIPs into a clean Apache Hop 2.19.0 installation and runs the
deterministic pipeline in `e2e/geometry-calculator.hpl`.

The published Maven ZIP coordinate is:

```text
ch.so.agi:hop-geometry-calculator-plugin:0.1.0-SNAPSHOT
```

Publication uses the shared `hop-plugin-ci` workflow and `INTERLIS_MAVEN_USERNAME` /
`INTERLIS_MAVEN_TOKEN` secrets. Pull requests publish nothing; GitHub plugin releases are no
longer created. The canonical ZIP is validated before publication and is published without a
rebuild.

## Function semantics

Geometry calculations use the units of the input geometry / CRS as-is. The plugin does not
reproject and does not perform unit conversion.

### True-curve semantics

SQL/MM curve geometries are preserved exactly while they are parsed and passed into the calculator.
`GEOMETRY_TYPE` reports the curve-aware names `CircularString`, `CompoundCurve`, `CurvePolygon`,
`MultiCurve`, and `MultiSurface` instead of the inherited JTS base type names.

Scalar calculations still use the JTS view of the geometry. The custom curve classes supplied by
`hop-geometry-type` expose a densified linear representation through their JTS superclass, so
`LENGTH`, `AREA`, `PERIMETER`, envelopes, centroid, validity, simplicity, coordinate counts, and
similar operations are evaluated on that segmented representation. They are not analytic
circle/arc calculations and do not use the exact curve control points directly.

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
- SQL/MM curve WKB types 8-12 and foreign plugin-classloader preservation
- curve-aware `GEOMETRY_TYPE` results and segmented curve measurement semantics
- scalar conversion and output typing
- function semantics for point, line, polygon, multi, empty, invalid, and `null` geometries
- validation reason/error type/location diagnostics
- simplicity diagnostics
- width, height, and interior-point helper functions
- transform metadata defaults, validation, output metadata, and XML roundtrip
- transform runtime for both `RETURN_NULL` and `FAIL`
- transform runtime with curve WKB input

Run tests only:

```bash
mvn test
```

## Development notes

- The plugin is intentionally append-only in v1.
- Multiple calculations per transform are out of scope for this first version.
- No formula parser, CRS transformation, or geoprocessing overlay/edit operations are included.
