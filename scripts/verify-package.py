#!/usr/bin/env python3
"""Validate the canonical Geometry Calculator installation ZIP."""

from __future__ import annotations

import argparse
import hashlib
import io
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile


ROOT = Path(__file__).resolve().parents[1]
POM_NAMESPACE = "{http://maven.apache.org/POM/4.0.0}"
PLUGIN_ROOT = "plugins/transforms/hop-geometry-calculator"
GEOMETRY_TYPE_VERSION = "0.2.0-SNAPSHOT"


def project_version() -> str:
    root = ET.parse(ROOT / "pom.xml").getroot()
    version = root.findtext(f"{POM_NAMESPACE}version") or root.findtext("version")
    if not version:
        raise SystemExit("Could not resolve project.version from pom.xml")
    return version


def pom_property(name: str) -> str:
    root = ET.parse(ROOT / "pom.xml").getroot()
    value = root.findtext(f"{POM_NAMESPACE}properties/{POM_NAMESPACE}{name}")
    if not value:
        raise SystemExit(f"Could not resolve Maven property {name!r} from pom.xml")
    return value


def sha256_bytes(content: bytes) -> str:
    return hashlib.sha256(content).hexdigest()


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def check_safe_path(name: str) -> None:
    path = Path(name)
    if path.is_absolute() or ".." in path.parts:
        raise SystemExit(f"ZIP contains an unsafe path: {name}")


def validate_jar(name: str, content: bytes, required: set[str]) -> None:
    with zipfile.ZipFile(io.BytesIO(content)) as jar:
        if jar.testzip() is not None:
            raise SystemExit(f"JAR is corrupt: {name}")
        entries = set(jar.namelist())
        classes = [entry for entry in entries if entry.endswith(".class")]
        forbidden_prefixes = ("org/apache/hop/", "org/eclipse/swt/")
        if any(entry.startswith(forbidden_prefixes) for entry in classes):
            raise SystemExit(f"JAR embeds Hop or SWT classes: {name}")
        missing = sorted(required - entries)
        if missing:
            raise SystemExit(f"JAR {name} is missing required entries: {missing}")


def validate(path: Path, version: str) -> dict[str, object]:
    expected_name = f"hop-geometry-calculator-plugin-{version}.zip"
    if not path.is_file():
        raise SystemExit(f"Missing package ZIP: {path}")
    if path.name != expected_name:
        raise SystemExit(f"Unexpected package name {path.name!r}; expected {expected_name!r}")

    candidate_zips = sorted(path.parent.glob(f"hop-geometry-calculator-plugin-*.zip"))
    if candidate_zips != [path]:
        raise SystemExit(f"Expected exactly one Calculator ZIP in {path.parent}, found {candidate_zips}")

    plugin_jar_name = f"{PLUGIN_ROOT}/hop-transform-geometry-calculator-{version}.jar"
    core_jar_name = f"{PLUGIN_ROOT}/lib/hop-geometry-calculator-core.jar"
    icon_name = "ch/so/agi/hop/geometry/calculator/transform/icons/geometry-calculator.svg"

    with zipfile.ZipFile(path) as archive:
        if archive.testzip() is not None:
            raise SystemExit(f"Package ZIP is corrupt: {path}")
        entries = archive.namelist()
        for entry in entries:
            check_safe_path(entry)
        files = {entry for entry in entries if not entry.endswith("/")}

        plugin_jars = sorted(
            entry
            for entry in files
            if entry.startswith(f"{PLUGIN_ROOT}/")
            and entry.endswith(".jar")
            and "/lib/" not in entry
        )
        if plugin_jars != [plugin_jar_name]:
            raise SystemExit(f"Expected exactly one plugin JAR {plugin_jar_name!r}, found {plugin_jars}")

        library_jars = sorted(
            entry for entry in files if entry.startswith(f"{PLUGIN_ROOT}/lib/") and entry.endswith(".jar")
        )
        if library_jars != [core_jar_name]:
            raise SystemExit(f"Expected exactly one Calculator runtime JAR {core_jar_name!r}, found {library_jars}")

        forbidden_names = (
            "hop-geometry-type",
            "jts-core",
            "geolatte-geom",
            "slf4j-api",
        )
        forbidden = sorted(
            entry for entry in files if entry.endswith(".jar") and any(name in Path(entry).name for name in forbidden_names)
        )
        if forbidden:
            raise SystemExit(f"Package contains shared/runtime libraries that must be provided elsewhere: {forbidden}")

        plugin_content = archive.read(plugin_jar_name)
        core_content = archive.read(core_jar_name)
        validate_jar(
            plugin_jar_name,
            plugin_content,
            {
                "META-INF/jandex.idx",
                "ch/so/agi/hop/geometry/calculator/transform/GeometryCalculatorMeta.class",
                "ch/so/agi/hop/geometry/calculator/transform/GeometryCalculatorClassLoaderBootstrap.class",
                icon_name,
            },
        )
        validate_jar(core_jar_name, core_content, set())

        source_icon = ROOT / "hop-transform-geometry-calculator/src/main/resources" / icon_name
        if not source_icon.is_file():
            raise SystemExit(f"Missing source icon: {source_icon}")
        with zipfile.ZipFile(io.BytesIO(plugin_content)) as plugin_jar:
            packaged_icon = plugin_jar.read(icon_name)
        if source_icon.read_bytes() != packaged_icon:
            raise SystemExit("Packaged geometry-calculator.svg differs from the source resource")

        declared_geometry_type_version = pom_property("hop.geometry.type.version")
        if declared_geometry_type_version != GEOMETRY_TYPE_VERSION:
            raise SystemExit(
                "Calculator must use the normal Geometry Type snapshot "
                f"{GEOMETRY_TYPE_VERSION}, found {declared_geometry_type_version}"
            )

    return {
        "schemaVersion": 1,
        "version": version,
        "zipFile": str(path),
        "sha256": sha256_file(path),
        "pluginRoot": PLUGIN_ROOT,
        "pluginJar": plugin_jar_name,
        "pluginJarSha256": sha256_bytes(plugin_content),
        "coreJar": core_jar_name,
        "coreJarSha256": sha256_bytes(core_content),
        "geometryTypeVersion": GEOMETRY_TYPE_VERSION,
    }


def main() -> int:
    version = project_version()
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--zip",
        type=Path,
        default=ROOT / f"assemblies/assemblies-hop-geometry-calculator/target/hop-geometry-calculator-plugin-{version}.zip",
    )
    args = parser.parse_args()
    report = validate(args.zip, version)
    output = ROOT / "target/package-verification.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
