package ru.fastdelivery.presentation.calc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.fastdelivery.presentation.api.request.Coordinate;

@Service
public class GeoDistanceService {

    @Value("${coordinate.latitudeMin}")
    public float latitudeMin;
    @Value("${coordinate.latitudeMax}")
    public float latitudeMax;
    @Value("${coordinate.longitudeMin}")
    public float longitudeMin;
    @Value("${coordinate.longitudeMax}")
    public float longitudeMax;

    private static final int EARTH_RADIUS = 6371; // Радиус Земли в километрах

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double radiansLatitude = Math.toRadians(lat2 - lat1);
        double radiansLongitude = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = haversine(radiansLatitude) + Math.cos(lat1) * Math.cos(lat2)
            * haversine(radiansLongitude);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Возвращаем расстояние в километрах
    }

    public boolean checkCoordinate(Coordinate coordinate) {

        if (coordinate.latitude() >= latitudeMax || coordinate.latitude() < latitudeMin
            || coordinate.longitude() >= longitudeMax || coordinate.longitude() < longitudeMin) {
            throw new IllegalArgumentException(
                "Введенные координаты выходят за допустимые пределы");
        }
        return true;
    }

    private double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
