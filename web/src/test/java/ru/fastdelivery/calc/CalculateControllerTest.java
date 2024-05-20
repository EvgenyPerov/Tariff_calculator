package ru.fastdelivery.calc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.fastdelivery.ControllerTest;
import ru.fastdelivery.domain.common.currency.CurrencyFactory;
import ru.fastdelivery.domain.common.price.Price;
import ru.fastdelivery.presentation.api.request.CalculatePackagesRequest;
import ru.fastdelivery.presentation.api.request.CargoPackage;
import ru.fastdelivery.presentation.api.request.Coordinate;
import ru.fastdelivery.presentation.api.response.CalculatePackagesResponse;
import ru.fastdelivery.presentation.calc.GeoDistanceService;
import ru.fastdelivery.presentation.calc.TariffCalculateVolumeService;
import ru.fastdelivery.usecase.TariffCalculateUseCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class CalculateControllerTest extends ControllerTest {

    final String baseCalculateApi = "/api/v1/calculate/";
    @MockBean
    TariffCalculateUseCase useCase;
    @MockBean
    CurrencyFactory currencyFactory;
    @MockBean
    GeoDistanceService distanceService;
    @MockBean
    TariffCalculateVolumeService tariffService;

    private static Coordinate destination;
    private static Coordinate departure;

    @BeforeAll
    static void init(){
        destination = new Coordinate(56.9972f, 40.9714f);
        departure = new Coordinate(55.7522f, 87.62f);
    }

    @Test
    @DisplayName("Валидные данные для расчета стоимость -> Ответ 200")
    void whenValidInputData_thenReturn200() {
        BigInteger length = BigInteger.valueOf(345);
        BigInteger width = BigInteger.valueOf(589);
        BigInteger height = BigInteger.valueOf(234);
        double volumeCube = length.multiply(width).multiply(height).doubleValue();

        var request = new CalculatePackagesRequest(List.of(new CargoPackage
                (BigInteger.TEN, length, width, height)), "RUB", destination , departure);
        var rub = new CurrencyFactory(code -> true).create("RUB");
        System.out.println("rub="+rub);
        when(useCase.calc(any())).thenReturn(new Price(BigDecimal.valueOf(10), rub));
        when(useCase.minimalPrice()).thenReturn(new Price(BigDecimal.valueOf(5), rub));
        when(tariffService.calcCubeMetres(length, width, height)).thenReturn(volumeCube);
        when(tariffService.getCostAllPackageByVolume(volumeCube)).thenReturn(18300.0);
        when(currencyFactory.create(anyString())).thenReturn(rub);
        when(distanceService.checkCoordinate(any(Coordinate.class))).thenReturn(true);
        when(distanceService.calculateDistance(departure.latitude(),
                departure.longitude(), destination.latitude(), destination.longitude())).thenReturn(2500.0);

        ResponseEntity<CalculatePackagesResponse> response =
                restTemplate.postForEntity(baseCalculateApi, request, CalculatePackagesResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Список упаковок == null -> Ответ 400")
    void whenEmptyListPackages_thenReturn400() {
        var request = new CalculatePackagesRequest(null, "RUB", destination , departure);

        ResponseEntity<String> response = restTemplate.postForEntity(baseCalculateApi, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
