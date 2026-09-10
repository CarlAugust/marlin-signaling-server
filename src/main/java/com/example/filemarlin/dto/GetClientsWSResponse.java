package com.example.filemarlin.dto;

import tools.jackson.databind.JsonNode;

public record GetClientsWSResponse(String type, String[] clients, JsonNode clientData) {
}
