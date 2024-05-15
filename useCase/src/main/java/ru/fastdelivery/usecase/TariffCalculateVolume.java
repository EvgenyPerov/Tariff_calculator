package ru.fastdelivery.usecase;

import jakarta.inject.Named;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Named
@Data
@RequiredArgsConstructor
public class TariffCalculateVolume {
    private BigInteger length;

    private BigInteger width;

    private BigInteger height;

    private BigInteger kratno;

    public TariffCalculateVolume(BigInteger length, BigInteger width, BigInteger height) {
        this.length = length;
        this.width = width;
        this.height = height;
        kratno = new BigInteger("50");
    }

    public TariffCalculateVolume getAroundValues() {
        return new TariffCalculateVolume(roundValue(length), roundValue(width), roundValue(height));
    }

    private BigInteger roundValue(BigInteger value) {
        BigInteger ostatok = value.mod(kratno);
        return (ostatok.intValue() < kratno.intValue() / 2) ?
                value.subtract(ostatok) : value.add(kratno.subtract(ostatok));

    }

    public double calcCubeMetres() {
        var multi = roundValue(length).multiply(roundValue(width)).multiply(roundValue(height)).toString();
        var cubeMilimetres = new BigDecimal(multi);
        BigDecimal cubeMetres = cubeMilimetres.divide(BigDecimal.valueOf(1_000_000_000),4, RoundingMode.HALF_UP);
        return cubeMetres.doubleValue();
    }

}
