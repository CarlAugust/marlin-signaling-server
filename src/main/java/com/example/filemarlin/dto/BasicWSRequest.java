package com.example.filemarlin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;

public record BasicWSRequest(
        String type, JsonNode clientData
) { }
