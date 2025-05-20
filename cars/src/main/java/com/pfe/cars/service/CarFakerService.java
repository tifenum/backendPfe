package com.pfe.cars.service;

import com.github.javafaker.Faker;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CarFakerService {
    private final Faker faker;
    private final Random random;

    public CarFakerService() {
        this.faker = new Faker();
        this.random = new Random();
    }

    @Getter
    public static class Car {
        private final String pickupCountry;
        private final String pickupCity;
        private final List<CarType> carTypes;

        public Car(String pickupCountry, String pickupCity, List<CarType> carTypes) {
            this.pickupCountry = pickupCountry;
            this.pickupCity = pickupCity;
            this.carTypes = carTypes;
        }

        @Override
        public String toString() {
            return "\nLocation: " + pickupCity + ", " + pickupCountry +
                    "\nCar Types:\n" + carTypes;
        }
    }

    @Getter
    public static class CarType {
        private final String type;
        private final int pricePerDay;
        private final List<String> features;
        private final String passengers;

        public CarType(String type, int pricePerDay, List<String> features, String passengers) {
            this.type = type;
            this.pricePerDay = pricePerDay;
            this.features = features;
            this.passengers = passengers;
        }

        @Override
        public String toString() {
            return "  - Type: " + type + ", Price: $" + pricePerDay + "/day, Features: " + features + ", Passengers: " + passengers;
        }
    }

    public List<Car> generateMultipleFakeCars(String pickupCountry, String pickupCity, String carType, String passengers, String transmission, int numCars) {
        List<Car> cars = new ArrayList<>();
        List<CarType> carTypes = generateFakeCarTypes(numCars * 2, carType, passengers, transmission);

        for (int i = 0; i < Math.min(numCars, carTypes.size()); i++) {
            cars.add(new Car(pickupCountry, pickupCity, List.of(carTypes.get(i))));
        }

        if (cars.size() < numCars) {
            CarType defaultCarType = generateDefaultCarType(carType, passengers, transmission);
            cars.add(new Car(pickupCountry, pickupCity, List.of(defaultCarType)));
        }

        return cars;
    }

    public Car generateFakeCar(String pickupCountry, String pickupCity, String carType, String passengers, String transmission) {
        List<CarType> carTypes = generateFakeCarTypes(5, carType, passengers, transmission);
        return new Car(pickupCountry, pickupCity, carTypes);
    }

    private List<CarType> generateFakeCarTypes(int numCarTypes, String filterCarType, String passengers, String transmission) {
        List<CarType> carTypes = new ArrayList<>();
        String[] carTypeOptions = {"Economy", "Compact", "SUV", "Luxury"};
        String[] featureOptions = {
                "GPS Navigation", "Automatic Transmission", "Child Seat", "Unlimited Mileage",
                "Bluetooth", "Air Conditioning", "Hybrid Engine", "Leather Seats"
        };
        String[] passengerOptions = {"1-2", "3-4", "5+"};

        for (int i = 0; i < numCarTypes; i++) {
            String type = filterCarType != null && !filterCarType.isEmpty() ? filterCarType : faker.options().option(carTypeOptions);
            int pricePerDay = switch (type) {
                case "Economy" -> faker.number().numberBetween(30, 61);
                case "Compact" -> faker.number().numberBetween(40, 81);
                case "SUV" -> faker.number().numberBetween(60, 121);
                case "Luxury" -> faker.number().numberBetween(100, 201);
                default -> 50;
            };
            List<String> features = generateRandomFeatures(featureOptions, transmission);
            String passengerCount = passengers != null && !passengers.isEmpty() ? passengers : getDefaultPassengersForCarType(type);
            carTypes.add(new CarType(type, pricePerDay, features, passengerCount));
        }

        if (passengers != null && !passengers.trim().isEmpty()) {
            carTypes = carTypes.stream()
                    .filter(ct -> isCarTypeSuitableForPassengers(ct.getType(), passengers))
                    .collect(Collectors.toList());
        }

        if (transmission != null && !transmission.trim().isEmpty() && transmission.equalsIgnoreCase("Automatic")) {
            carTypes = carTypes.stream()
                    .filter(ct -> ct.getFeatures().contains("Automatic Transmission"))
                    .collect(Collectors.toList());
        }

        if (carTypes.isEmpty()) {
            carTypes.add(generateDefaultCarType(filterCarType, passengers, transmission));
        }

        return carTypes;
    }

    private CarType generateDefaultCarType(String filterCarType, String passengers, String transmission) {
        String type = filterCarType != null && !filterCarType.isEmpty() ? filterCarType : "Economy";
        int pricePerDay = switch (type) {
            case "Economy" -> 50;
            case "Compact" -> 60;
            case "SUV" -> 80;
            case "Luxury" -> 120;
            default -> 50;
        };
        List<String> features = new ArrayList<>(Arrays.asList("Air Conditioning", "Bluetooth"));
        if (transmission != null && transmission.equalsIgnoreCase("Automatic")) {
            features.add("Automatic Transmission");
        }
        String passengerCount = passengers != null && !passengers.isEmpty() ? passengers : getDefaultPassengersForCarType(type);
        return new CarType(type, pricePerDay, features, passengerCount);
    }

    private List<String> generateRandomFeatures(String[] featureOptions, String transmission) {
        int numFeatures = random.nextInt(3) + 2;
        List<String> features = new ArrayList<>();
        List<String> availableFeatures = new ArrayList<>(Arrays.asList(featureOptions));

        if (transmission != null && transmission.equalsIgnoreCase("Automatic")) {
            features.add("Automatic Transmission");
            availableFeatures.remove("Automatic Transmission");
            numFeatures--;
        }

        for (int i = 0; i < numFeatures && !availableFeatures.isEmpty(); i++) {
            String feature = faker.options().option(availableFeatures.toArray(new String[0]));
            features.add(feature);
            availableFeatures.remove(feature);
        }
        return features;
    }

    private boolean isCarTypeSuitableForPassengers(String carType, String passengers) {
        return switch (passengers) {
            case "1-2" -> carType.equals("Economy") || carType.equals("Compact") || carType.equals("Luxury");
            case "3-4" -> carType.equals("Compact") || carType.equals("SUV") || carType.equals("Luxury");
            case "5+" -> carType.equals("SUV") || carType.equals("Luxury");
            default -> true;
        };
    }

    private String getDefaultPassengersForCarType(String carType) {
        return switch (carType) {
            case "Economy" -> "1-2";
            case "Compact" -> "3-4";
            case "SUV" -> "5+";
            case "Luxury" -> "3-4";
            default -> "1-2";
        };
    }
}
