package ru.fastdelivery.presentation.calc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fastdelivery.domain.common.currency.Currency;
import ru.fastdelivery.domain.common.currency.CurrencyFactory;
import ru.fastdelivery.domain.common.price.Price;
import ru.fastdelivery.domain.common.weight.Weight;
import ru.fastdelivery.domain.delivery.pack.Pack;
import ru.fastdelivery.domain.delivery.shipment.Shipment;
import ru.fastdelivery.presentation.api.request.CalculatePackagesRequest;
import ru.fastdelivery.presentation.api.request.CargoPackage;
import ru.fastdelivery.presentation.api.request.Coordinate;
import ru.fastdelivery.presentation.api.response.CalculatePackagesResponse;
import ru.fastdelivery.usecase.TariffCalculateUseCase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/calculate/")
@RequiredArgsConstructor
@Tag(name = "Расчеты стоимости доставки")
public class CalculateController {
    private final TariffCalculateUseCase tariffCalculateUseCase;
    private final CurrencyFactory currencyFactory;
    private final GeoDistanceService distanceService;
    private final TariffCalculateVolumeService tariffService;

    private static final double DISTANCE_KRATNO = 450;

    @PostMapping
    @Operation(summary = "Расчет стоимости по упаковкам груза")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    public CalculatePackagesResponse calculate(
            @Valid @RequestBody CalculatePackagesRequest request) {

        Set<Double> summSet = new HashSet<>();
        request.packages().forEach(pack ->
            summSet.add(tariffService.calcCubeMetres(pack.length(), pack.width(), pack.height()))
        );
        double totalSummaVolume = summSet.stream().mapToDouble(Double::doubleValue).sum();
        double totalCostAllPackageVolume =
            tariffService.getCostAllPackageByVolume(totalSummaVolume);

        Currency currencyCode = currencyFactory.create(request.currencyCode());
        Price priceByVolume = new Price(new BigDecimal(totalCostAllPackageVolume), currencyCode);

        Coordinate coordinateFrom = new Coordinate(request.departure().latitude(),
            request.departure().longitude());
        Coordinate coordinateTo = new Coordinate(request.destination().latitude(),
            request.destination().longitude());

        distanceService.checkCoordinate(coordinateFrom);
        distanceService.checkCoordinate(coordinateTo);

        double distance = distanceService.calculateDistance(coordinateFrom.latitude(),
                coordinateFrom.longitude(), coordinateTo.latitude(), coordinateTo.longitude());

        List<Pack> packsWeights = request.packages().stream()
            .map(CargoPackage::weight)
            .map(Weight::new)
            .map(Pack::new)
            .toList();

        Shipment shipment =
            new Shipment(packsWeights, currencyFactory.create(request.currencyCode()));
        Price calculatedPrice = tariffCalculateUseCase.calc(shipment);
        Price basePrice = calculatedPrice.max(priceByVolume);

        double parts = distance / DISTANCE_KRATNO;

        Price minimalPrice = tariffCalculateUseCase.minimalPrice();

        if (distance / DISTANCE_KRATNO > 1) {
            BigDecimal summa = (BigDecimal.valueOf(parts).multiply(basePrice.amount()))
                .setScale(2, RoundingMode.HALF_UP);
            Price priceMoreThenKratnoDistance = new Price(summa, currencyCode);
            return new CalculatePackagesResponse(priceMoreThenKratnoDistance, minimalPrice);
        }
        return new CalculatePackagesResponse(basePrice, minimalPrice);
    }
}

