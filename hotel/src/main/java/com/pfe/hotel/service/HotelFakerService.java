package com.pfe.hotel.service;

import com.github.javafaker.Faker;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class HotelFakerService {
    private final Faker faker;
    private final Random random;

    public HotelFakerService() {
        this.faker = new Faker();
        this.random = new Random();
    }

    @Getter
    public static class Hotel {
        private final String name;
        private final String address;
        private final double latitude;
        private final double longitude;
        private final List<Room> rooms;

        public Hotel(String name, String address, double latitude, double longitude, List<Room> rooms) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.rooms = rooms;
        }

        @Override
        public String toString() {
            return "Hotel: " + name +
                    "\nAddress: " + address +
                    "\nCoordinates: (" + latitude + ", " + longitude + ")" +
                    "\nRooms:\n" + rooms;
        }
    }

    @Getter
    public static class Room {
        private final String type;
        private final double price;
        private final List<String> features;

        public Room(String type, double price, List<String> features) {
            this.type = type;
            this.price = price;
            this.features = features;
        }

        @Override
        public String toString() {
            return "  - Type: " + type + ", Price: $" + String.format("%.2f", price) + ", Features: " + features;
        }
    }

    public Hotel generateFakeHotel(String hotelName, String countryName, String stateName, double latitude, double longitude) {
        String address = faker.address().streetAddress() + ", " + stateName + ", " + countryName;
        List<Room> rooms = generateFakeRooms(random.nextInt(5) + 2); // 2-6 rooms
        return new Hotel(hotelName, address, latitude, longitude, rooms);
    }

    private List<Room> generateFakeRooms(int numRooms) {
        List<Room> rooms = new ArrayList<>();
        String[] roomTypes = {"Single", "Double", "Suite", "Deluxe"};
        String[] featureOptions = {
                "Free Wi-Fi", "Ocean View", "Mini Bar", "Flat-screen TV",
                "Balcony", "Air Conditioning", "Room Service", "Hot Tub"
        };

        for (int i = 0; i < numRooms; i++) {
            String type = faker.options().option(roomTypes);
            double price = switch (type) {
                case "Single" -> faker.number().randomDouble(2, 50, 100);
                case "Double" -> faker.number().randomDouble(2, 80, 150);
                case "Suite" -> faker.number().randomDouble(2, 150, 300);
                case "Deluxe" -> faker.number().randomDouble(2, 200, 400);
                default -> 100.0;
            };
            List<String> features = generateRandomFeatures(featureOptions);
            rooms.add(new Room(type, price, features));
        }
        return rooms;
    }

    private List<String> generateRandomFeatures(String[] featureOptions) {
        int numFeatures = random.nextInt(3) + 2; // 2-4 features
        List<String> features = new ArrayList<>();
        for (int i = 0; i < numFeatures; i++) {
            features.add(faker.options().option(featureOptions));
        }
        return features;
    }
}