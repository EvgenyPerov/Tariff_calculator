package ru.fastdelivery.presentation.calc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import ru.fastdelivery.presentation.api.response.CalculatePackagesResponse;
import ru.fastdelivery.usecase.TariffCalculateUseCase;
import ru.fastdelivery.usecase.TariffCalculateVolume;

import java.math.BigDecimal;
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

    @Value("${cost.rub.perCube}")
    private String costPerCube;

    @PostMapping
    @Operation(summary = "Расчет стоимости по упаковкам груза")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    public CalculatePackagesResponse calculate(
            @Valid @RequestBody CalculatePackagesRequest request) {
        List<Pack> packsWeights = request.packages().stream()
                .map(CargoPackage::weight)
                .map(Weight::new)
                .map(Pack::new)
                .toList();

        Shipment shipment = new Shipment(packsWeights, currencyFactory.create(request.currencyCode()));
        Price calculatedPrice = tariffCalculateUseCase.calc(shipment);
        Price minimalPrice = tariffCalculateUseCase.minimalPrice();

        Set<Double> summSet = new HashSet<>();
        request.packages().forEach(pack -> {
            TariffCalculateVolume tariffCalculateVolume = new TariffCalculateVolume(pack.length(),
                    pack.width(), pack.height());
            double val = tariffCalculateVolume.calcCubeMetres();
            summSet.add(val);
        });

        double totalSummaVolume = summSet.stream().mapToDouble(Double::doubleValue).sum();
        double totalCostAllPackageVolume = totalSummaVolume * Double.parseDouble(costPerCube);

        Price price = new Price(new BigDecimal(totalCostAllPackageVolume),
                currencyFactory.create(request.currencyCode()));

        return new CalculatePackagesResponse(calculatedPrice.max(price), minimalPrice);
    }
}

