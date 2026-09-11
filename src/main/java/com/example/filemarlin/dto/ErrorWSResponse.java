package com.example.filemarlin.dto;

import tools.jackson.databind.JsonNode;

public record ErrorWSResponse(String type, String message, JsonNode clientData) {
}
