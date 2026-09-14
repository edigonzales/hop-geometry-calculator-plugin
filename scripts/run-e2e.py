#!/usr/bin/env python3
"""Run the packaged Geometry Calculator against an isolated Hop installation.

Installs the plugin and Geometry Type ZIPs, runs the deterministic E2E pipeline and executes every
documentation example from ``examples/`` against the expected results in ``e2e/expected/``.
"""

from __future__ import annotations

import argparse
import csv
import os
from pathlib import Path
import subprocess
import tempfile
import zipfile


ROOT = Path(__file__).resolve().parents[1]
PLUGIN_ROOT = "plugins/transforms/hop-geometry-calculator"
GEOMETRY_ROOT = "plugins/misc/hop-geometry-type"


def extract_plugin(zip_path: Path, hop_home: Path, expected_root: str) -> None:
    if not zip_path.is_file():
        raise SystemExit(f"Missing plugin ZIP: {zip_path}")
    with zipfile.ZipFile(zip_path) as archive:
        if archive.testzip() is not None:
            raise SystemExit(f"Corrupt plugin ZIP: {zip_path}")
        entries = archive.namelist()
        prefix = expected_root.rstrip("/") + "/"
        if not any(entry.startswith(prefix) for entry in entries):
            raise SystemExit(f"{zip_path.name} lacks installation root {prefix}")
        for entry in entries:
            path = Path(entry)
            if path.is_absolute() or ".." in path.parts:
                raise SystemExit(f"Unsafe ZIP entry {entry!r} in {zip_path.name}")
        archive.extractall(hop_home)


def run_pipeline(
    hop_home: Path, pipeline: Path, env: dict[str, str], parameters: dict[str, Path]
) -> None:
    command = [
        str(hop_home / "hop-run.sh"),
        "-r",
        "local",
        "-f",
        str(pipeline),
    ]
    for name, value in parameters.items():
        command.extend(["-p", f"{name}={value}"])
    print("==>", " ".join(command), flush=True)
    subprocess.run(command, check=True, env=env, timeout=300)


def check_output(output_file: Path) -> None:
    if not output_file.is_file():
        raise SystemExit(f"Hop did not create the expected output: {output_file}")
    with output_file.open(newline="", encoding="utf-8") as stream:
        rows = list(csv.reader(stream, delimiter=";"))
    if rows != [["geometry", "x_coord"], ["POINT (3 4)", "3.0"], ["POINT (7 8)", "7.0"]]:
        raise SystemExit(f"Unexpected Geometry Calculator output: {rows!r}")


def example_pipelines() -> list[Path]:
    pipelines = sorted((ROOT / "examples").glob("*/*.hpl"))
    if not pipelines:
        raise SystemExit("No example pipelines found under examples/")
    names = [pipeline.parent.name for pipeline in pipelines]
    if len(names) != len(set(names)):
        raise SystemExit(f"Ambiguous example names: {names}")
    return pipelines


def check_example_output(pipeline: Path, output_file: Path, expected_file: Path) -> None:
    if not expected_file.is_file():
        raise SystemExit(f"Missing expected output for {pipeline.relative_to(ROOT)}: {expected_file}")
    if not output_file.is_file():
        raise SystemExit(f"Hop did not create the expected output: {output_file}")
    actual = output_file.read_text(encoding="utf-8")
    expected = expected_file.read_text(encoding="utf-8")
    if actual != expected:
        raise SystemExit(
            f"Output of {pipeline.relative_to(ROOT)} differs from {expected_file}.\n"
            f"--- actual ---\n{actual}--- expected ---\n{expected}"
        )
    print(f"Example {output_file.name} matches {expected_file.relative_to(ROOT)}", flush=True)


def run_examples(hop_home: Path, env: dict[str, str], work: Path) -> None:
    for pipeline in example_pipelines():
        name = pipeline.parent.name
        data_files = sorted((pipeline.parent / "data").glob("*.csv"))
        if len(data_files) != 1:
            raise SystemExit(
                f"Expected exactly one CSV in {pipeline.parent / 'data'}, found {data_files}"
            )
        output_dir = work / "examples" / name
        output_dir.mkdir(parents=True)
        output_file = output_dir / f"{name}.csv"
        run_pipeline(
            hop_home,
            pipeline,
            env,
            {"INPUT_CSV": data_files[0], "OUTPUT_CSV": output_file},
        )
        check_example_output(pipeline, output_file, ROOT / "e2e/expected" / f"{name}.csv")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--hop-home", required=True, type=Path)
    parser.add_argument("--plugin-zip", required=True, type=Path)
    parser.add_argument("--geometry-zip", required=True, type=Path)
    args = parser.parse_args()

    hop_home = args.hop_home.resolve()
    if not (hop_home / "hop-run.sh").is_file():
        raise SystemExit(f"Not an Apache Hop home: {hop_home}")
    if (hop_home / PLUGIN_ROOT).exists():
        raise SystemExit(f"Hop home already contains the Calculator plugin: {hop_home / PLUGIN_ROOT}")
    if (hop_home / GEOMETRY_ROOT).exists():
        raise SystemExit(f"Hop home already contains Geometry Type: {hop_home / GEOMETRY_ROOT}")

    with tempfile.TemporaryDirectory(prefix="hop-geometry-calculator-e2e-") as temporary:
        work = Path(temporary)
        config = work / "config"
        audit = work / "audit"
        input_dir = work / "input"
        output_dir = work / "output"
        input_dir.mkdir()
        output_dir.mkdir()
        (config / "metadata/pipeline-run-configuration").mkdir(parents=True)
        audit.mkdir()
        (config / "metadata/pipeline-run-configuration/local.json").write_text(
            '{\n'
            '  "name": "local",\n'
            '  "engineRunConfiguration": {"Local": {"rowset_size": "2", "safe_mode": true}},\n'
            '  "configurationVariables": []\n'
            '}\n',
            encoding="utf-8",
        )
        (input_dir / "geometry.csv").write_text(
            "geometry\nPOINT (3 4)\nPOINT (7 8)\n",
            encoding="utf-8",
        )

        extract_plugin(args.geometry_zip, hop_home, GEOMETRY_ROOT)
        extract_plugin(args.plugin_zip, hop_home, PLUGIN_ROOT)

        env = os.environ.copy()
        env["HOP_CONFIG_FOLDER"] = str(config)
        env["HOP_AUDIT_FOLDER"] = str(audit)
        if env.get("JAVA_HOME"):
            env["HOP_JAVA_HOME"] = env["JAVA_HOME"]

        run_pipeline(
            hop_home,
            ROOT / "e2e/geometry-calculator.hpl",
            env,
            {"E2E_INPUT_DIR": input_dir, "E2E_OUTPUT_DIR": output_dir},
        )
        check_output(output_dir / "geometry-calculator.csv")

        run_examples(hop_home, env, work)

    print("Installed Hop Geometry Calculator E2E OK")
    print("Documentation examples OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
