package ru.fastdelivery.presentation.calc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Service
public class TariffCalculateVolumeService {
    private static final BigInteger KRATNO = BigInteger.valueOf(50);

    @Value("${cost.rub.perCube}")
    public double costPerCube;

    public double calcCubeMetres(BigInteger length, BigInteger width, BigInteger height) {
        var multi =
            roundValue(length).multiply(roundValue(width)).multiply(roundValue(height)).toString();
        var cubeMilimetres = new BigDecimal(multi);
        BigDecimal cubeMetres =
            cubeMilimetres.divide(BigDecimal.valueOf(1_000_000_000), 4, RoundingMode.HALF_UP);
        return cubeMetres.doubleValue();
    }

    public double getCostAllPackageByVolume(double totalSummaVolume) {
        return totalSummaVolume * costPerCube;
    }

    private BigInteger roundValue(BigInteger value) {
        BigInteger ostatok = value.mod(KRATNO);
        return (ostatok.intValue() < KRATNO.intValue() / 2)
            ? value.subtract(ostatok) : value.add(KRATNO.subtract(ostatok));

    }
}
