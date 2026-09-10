package com.example.filemarlin.dto;

import tools.jackson.databind.JsonNode;

public record SignalWSResponse(
        String type,
        String senderId,
        JsonNode clientData
) { }
