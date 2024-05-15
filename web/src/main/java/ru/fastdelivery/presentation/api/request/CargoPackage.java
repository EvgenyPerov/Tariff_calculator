package ru.fastdelivery.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.fastdelivery.domain.common.weight.Weight;

import java.math.BigInteger;

public record CargoPackage(
        @Schema(description = "Вес упаковки, граммы", example = "5667.45")
        BigInteger weight,

        @Schema(description = "Габариты упаковки, длина", example = "345")
        BigInteger length,

        @Schema(description = "Габариты упаковки, ширина", example = "589")
        BigInteger width,

        @Schema(description = "Габариты упаковки, высота", example = "234")
        BigInteger height
) {
        public CargoPackage {
                if (isLessThanZero(length) || isLessThanZero(width) || isLessThanZero(height)) {
                        throw new IllegalArgumentException("Габариты не могут быть меньше нуля!");
                }
                if (isMoreThenMaxGabarite(length) || isMoreThenMaxGabarite(width) || isMoreThenMaxGabarite(height)) {
                        throw new IllegalArgumentException("Один или более габаритов не может быть больше 1500 мм");
                }
        }

        private static boolean isLessThanZero(BigInteger price) {
                return BigInteger.ZERO.compareTo(price) > 0;
        }

        private static boolean isMoreThenMaxGabarite(BigInteger gabarite) {
                return BigInteger.valueOf(1_500).compareTo(gabarite) < 0;
        }
}
