package ru.fastdelivery.calc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.fastdelivery.presentation.api.request.Coordinate;
import ru.fastdelivery.presentation.calc.GeoDistanceService;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@ExtendWith(SpringExtension.class)
public class GeoDistanceServiceTest {
    @InjectMocks
    private GeoDistanceService distanceService; // = new GeoDistanceService();

    private static final int EARTH_RADIUS = 6371; // Радиус Земли в километрах

    float latitudeMin = 45;
    float latitudeMax = 65;
    float longitudeMin = 30;
    float longitudeMax = 96;

    @BeforeEach
    void init(){
        distanceService = new GeoDistanceService();
        distanceService.latitudeMin = latitudeMin;
        distanceService.latitudeMax = latitudeMax;
        distanceService.longitudeMin = longitudeMin;
        distanceService.longitudeMax = longitudeMax;
    }

    @Test
    @DisplayName("Расчет расстояния между точками координат")
     void calculateDistanceTest() {
        double lat1 = 55.7522, lon1 = 87.62,  lat2 = 56.9972,  lon2 = 40.9714;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = haversine(dLat) + Math.cos(lat1) * Math.cos(lat2) * haversine(dLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double resultExpected = EARTH_RADIUS * c;
        double resultActual =  distanceService.calculateDistance(55.7522, 87.62, 56.9972, 40.9714);

        assertEquals(resultExpected, resultActual);
    };

    private double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    @Test
    @DisplayName("Проверка валидности точек координат")
    void checkCoordinateTest() {
        Coordinate coordinate = new Coordinate(55.7522f, 87.62f);
        boolean resultExpected = coordinate.latitude() < latitudeMax && coordinate.latitude() >= latitudeMin
                && coordinate.longitude() < longitudeMax && coordinate.longitude() >= longitudeMin;
        System.out.println("resultExpected = " + resultExpected);
        boolean resultActual =  distanceService.checkCoordinate(coordinate);
        System.out.println("resultActual = " + resultActual);
        assertEquals(resultExpected, resultActual);
    }

    @Test
    @DisplayName("Проверка выброс ошибки при не корректной широте")
    public void testCheckCoordinateOutsideLatitudeBounds() {
        Coordinate coordinate = new Coordinate(55.0f, 10.0f);
        assertThrows(IllegalArgumentException.class, () -> distanceService.checkCoordinate(coordinate), "Должно быть вызвано исключение из-за выхода широты за границы");
    }

    @Test
    @DisplayName("Проверка выброс ошибки при не корректной долготе")
    public void testCheckCoordinateOutsideLongitudeBounds() {
        Coordinate coordinate = new Coordinate(25.0f, -25.0f);
        assertThrows(IllegalArgumentException.class, () -> distanceService.checkCoordinate(coordinate), "Должно быть вызвано исключение из-за выхода долготы за границы");
    }
}
