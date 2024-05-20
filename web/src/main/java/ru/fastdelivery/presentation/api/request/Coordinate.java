package ru.fastdelivery.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public record Coordinate(
        @Schema(description = "Координаты, широта от 45 до 65 %", example = "63.398660")
        @NotNull
        Float latitude,

        @Schema(description = "Координаты, долгота  от 30 до 96 %", example = "55.027532")
        @NotNull
        Float longitude) {}
