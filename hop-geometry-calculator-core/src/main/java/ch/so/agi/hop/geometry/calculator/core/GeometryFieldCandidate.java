package ch.so.agi.hop.geometry.calculator.core;

public record GeometryFieldCandidate(
    String fieldName, int index, boolean geometryValueMeta, boolean heuristic) {}
