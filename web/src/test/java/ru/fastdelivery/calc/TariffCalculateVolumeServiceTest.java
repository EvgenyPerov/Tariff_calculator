package ru.fastdelivery.calc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import ru.fastdelivery.presentation.calc.TariffCalculateVolumeService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TariffCalculateVolumeServiceTest {
    @InjectMocks
    private TariffCalculateVolumeService tariffCalculateVolumeService;
    private static final BigInteger KRATNO = BigInteger.valueOf(50);
    public double costPerCube = 18500;

    @BeforeEach
    void init(){
        tariffCalculateVolumeService = new TariffCalculateVolumeService();
        tariffCalculateVolumeService.costPerCube = costPerCube;
    }

    @Test
    @DisplayName("Расчет расстояния между точками координат")
    void calcCubeMetresTest() {
        BigInteger length = BigInteger.valueOf(400), width = BigInteger.valueOf(500), height = BigInteger.valueOf(600);

        var multi = roundValue(length).multiply(roundValue(width)).multiply(roundValue(height)).toString();
        var cubeMillimetres = new BigDecimal(multi);
        BigDecimal cubeMetres = cubeMillimetres.divide(BigDecimal.valueOf(1_000_000_000),4, RoundingMode.HALF_UP);

        double resultExpected = cubeMetres.doubleValue();
        double resultActual = tariffCalculateVolumeService.calcCubeMetres(length, width, height);

        assertEquals(resultExpected, resultActual);
    }

    @Test
    @DisplayName("Расчет расстояния между точками координат")
    void getCostAllPackageByVolumeTest(){
        double totalSummaVolume = 4.5;
        double resultExpected =  totalSummaVolume * costPerCube;
        double resultActual = tariffCalculateVolumeService.getCostAllPackageByVolume(totalSummaVolume);
        assertEquals(resultExpected, resultActual);
    }

    private BigInteger roundValue(BigInteger value) {
        BigInteger ostatok = value.mod(KRATNO);
        return (ostatok.intValue() < KRATNO.intValue() / 2) ?
                value.subtract(ostatok) : value.add(KRATNO.subtract(ostatok));

    }
}
