package com.pfe.cars.service;

import com.github.javafaker.Faker;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
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
        private final String provider;
        private final String pickupCountry;
        private final String pickupCity;
        private final List<CarType> carTypes;

        public Car(String provider, String pickupCountry, String pickupCity, List<CarType> carTypes) {
            this.provider = provider;
            this.pickupCountry = pickupCountry;
            this.pickupCity = pickupCity;
            this.carTypes = carTypes;
        }

        @Override
        public String toString() {
            return "Car Provider: " + provider +
                    "\nLocation: " + pickupCity + ", " + pickupCountry +
                    "\nCar Types:\n" + carTypes;
        }
    }

    @Getter
    public static class CarType {
        private final String type;
        private final int pricePerDay;
        private final List<String> features;

        public CarType(String type, int pricePerDay, List<String> features) {
            this.type = type;
            this.pricePerDay = pricePerDay;
            this.features = features;
        }

        @Override
        public String toString() {
            return "  - Type: " + type + ", Price: $" + pricePerDay + "/day, Features: " + features;
        }
    }

    public Car generateFakeCar(String pickupCountry, String pickupCity, String carType, String passengers, String transmission) {
        String providerName = faker.options().option("Hertz", "Avis", "Enterprise", "Budget");
        List<CarType> carTypes = generateFakeCarTypes(5, carType, passengers, transmission); // Generate 5, filter down
        return new Car(providerName, pickupCountry, pickupCity, carTypes);
    }

    private List<CarType> generateFakeCarTypes(int numCarTypes, String filterCarType, String passengers, String transmission) {
        List<CarType> carTypes = new ArrayList<>();
        String[] carTypeOptions = {"Economy", "Compact", "SUV", "Luxury"};
        String[] featureOptions = {
                "GPS Navigation", "Automatic Transmission", "Child Seat", "Unlimited Mileage",
                "Bluetooth", "Air Conditioning", "Hybrid Engine", "Leather Seats"
        };

        for (int i = 0; i < numCarTypes; i++) {
            String type = faker.options().option(carTypeOptions);
            int pricePerDay = switch (type) {
                case "Economy" -> faker.number().numberBetween(30, 61); // $30-60/day
                case "Compact" -> faker.number().numberBetween(40, 81); // $40-80/day
                case "SUV" -> faker.number().numberBetween(60, 121); // $60-120/day
                case "Luxury" -> faker.number().numberBetween(100, 201); // $100-200/day
                default -> 50;
            };
            List<String> features = generateRandomFeatures(featureOptions, transmission);
            carTypes.add(new CarType(type, pricePerDay, features));
        }

        // Filter by carType
        if (filterCarType != null && !filterCarType.trim().isEmpty()) {
            carTypes = carTypes.stream()
                    .filter(ct -> ct.getType().equalsIgnoreCase(filterCarType))
                    .collect(Collectors.toList());
        }

        // Filter by passengers
        if (passengers != null && !passengers.trim().isEmpty()) {
            carTypes = carTypes.stream()
                    .filter(ct -> isCarTypeSuitableForPassengers(ct.getType(), passengers))
                    .collect(Collectors.toList());
        }

        // Filter by transmission
        if (transmission != null && !transmission.trim().isEmpty() && transmission.equalsIgnoreCase("Automatic")) {
            carTypes = carTypes.stream()
                    .filter(ct -> ct.getFeatures().contains("Automatic Transmission"))
                    .collect(Collectors.toList());
        }

        // Ensure at least one car type if filters are too restrictive
        if (carTypes.isEmpty()) {
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
            carTypes.add(new CarType(type, pricePerDay, features));
        }

        return carTypes;
    }

    private List<String> generateRandomFeatures(String[] featureOptions, String transmission) {
        int numFeatures = random.nextInt(3) + 2; // 2-4 features
        List<String> features = new ArrayList<>();
        List<String> availableFeatures = new ArrayList<>(Arrays.asList(featureOptions));

        // Ensure Automatic Transmission if requested
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
}