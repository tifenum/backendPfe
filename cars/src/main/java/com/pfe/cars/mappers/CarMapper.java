package com.pfe.cars.mappers;

import com.pfe.cars.service.CarFakerService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CarMapper {

    public List<Map<String, Object>> toMapList(List<CarFakerService.Car> cars) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CarFakerService.Car car : cars) {
            Map<String, Object> carMap = new HashMap<>();
            carMap.put("pickupCountry", car.getPickupCountry());
            carMap.put("pickupCity", car.getPickupCity());
            carMap.put("carTypes", mapCarTypes(car.getCarTypes()));
            carMap.put("bookingLink", "/car-details?pickupCountry=" + encode(car.getPickupCountry()) +
                    "&pickupCity=" + encode(car.getPickupCity()) +
                    "&carType=" + encode(car.getCarTypes().get(0).getType()));
            result.add(carMap);
        }
        return result;
    }

    private List<Map<String, Object>> mapCarTypes(List<CarFakerService.CarType> carTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CarFakerService.CarType carType : carTypes) {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("type", carType.getType());
            typeMap.put("pricePerDay", carType.getPricePerDay());
            typeMap.put("features", carType.getFeatures());
            typeMap.put("passengers", carType.getPassengers());
            result.add(typeMap);
        }
        return result;
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}