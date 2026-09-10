package com.example.filemarlin.dto;

import tools.jackson.databind.JsonNode;

public record SignalWSRequest(String type, String targetId, JsonNode clientData) {
}
