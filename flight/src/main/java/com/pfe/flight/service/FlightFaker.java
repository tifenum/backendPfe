package com.pfe.flight.service;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FlightFaker {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    public List<Map<String, Object>> generateFakeFlightOffers(
            String originLocationCode, String destinationLocationCode,
            String departureDate, String returnDate, String flightType, String airlineCode) {

        List<Map<String, Object>> flightOffers = new ArrayList<>();
        // Default departure date to today + 7 days if not provided
        String effectiveDepartureDate = departureDate != null && !departureDate.isEmpty()
                ? departureDate
                : LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Calculate base duration for this route
        long baseDurationMinutes = estimateFlightDuration(originLocationCode, destinationLocationCode);

        for (int i = 1; i <= 3; i++) {
            // Vary duration by ±30 minutes
            long durationVariation = random.nextInt(61) - 30; // -30 to +30 minutes
            long variedDurationMinutes = Math.max(30, baseDurationMinutes + durationVariation); // Ensure at least 30 minutes
            flightOffers.add(generateFlightOffer(
                    originLocationCode, destinationLocationCode,
                    effectiveDepartureDate, returnDate, flightType, i, airlineCode, variedDurationMinutes
            ));
        }

        return flightOffers;
    }

    private long estimateFlightDuration(String origin, String destination) {
        // Simple heuristic: map some common city pairs to realistic durations (in minutes)
        Map<String, Integer> durationMap = new HashMap<>();
        durationMap.put("JFK-LHR", 420); // New York to London (~7h)
        durationMap.put("LHR-JFK", 450); // London to New York (~7.5h)
        durationMap.put("LAX-SYD", 900); // Los Angeles to Sydney (~15h)
        durationMap.put("SYD-LAX", 870); // Sydney to Los Angeles (~14.5h)
        durationMap.put("CDG-DXB", 390); // Paris to Dubai (~6.5h)
        durationMap.put("DXB-CDG", 420); // Dubai to Paris (~7h)
        durationMap.put("NRT-ORD", 720); // Tokyo to Chicago (~12h)
        durationMap.put("ORD-NRT", 750); // Chicago to Tokyo (~12.5h)

        String routeKey = origin + "-" + destination;
        String reverseRouteKey = destination + "-" + origin;

        // Check if we have a predefined duration for this route
        if (durationMap.containsKey(routeKey)) {
            return durationMap.get(routeKey);
        } else if (durationMap.containsKey(reverseRouteKey)) {
            return durationMap.get(reverseRouteKey);
        }

        // Fallback: generate random duration based on rough distance
        // Short-haul: 1-3h, Medium-haul: 3-6h, Long-haul: 6-18h
        int[] durationRanges = {60, 180, 360, 1080}; // 1h, 3h, 6h, 18h
        int rangeIndex = random.nextInt(3); // 0: short, 1: medium, 2: long
        return random.nextInt(durationRanges[rangeIndex + 1] - durationRanges[rangeIndex]) + durationRanges[rangeIndex];
    }

    private Map<String, Object> generateFlightOffer(
            String originLocationCode, String destinationLocationCode,
            String departureDate, String returnDate, String flightType, int id, String airlineCode, long flightDurationMinutes) {

        Map<String, Object> flightOffer = new HashMap<>();
        flightOffer.put("id", id);

        boolean isOneWay = "one-way".equalsIgnoreCase(flightType);
        flightOffer.put("oneWay", isOneWay);
        flightOffer.put("tripType", isOneWay ? "One Way" : "Round Trip");
        flightOffer.put("returnDate", isOneWay ? null : returnDate);

        List<Map<String, Object>> itineraries = new ArrayList<>();
        itineraries.add(generateItinerary(originLocationCode, destinationLocationCode, departureDate, flightDurationMinutes));
        if (!isOneWay) {
            // Only add return itinerary for round-trip
            String effectiveReturnDate = returnDate != null && !returnDate.isEmpty()
                    ? returnDate
                    : LocalDate.parse(departureDate).plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);
            // Use same duration variation for return flight
            itineraries.add(generateItinerary(destinationLocationCode, originLocationCode, effectiveReturnDate, flightDurationMinutes));
        }
        flightOffer.put("itineraries", itineraries);

        flightOffer.put("price", String.valueOf((int) faker.number().randomDouble(2, 50, 200)));
        String effectiveAirlineCode = airlineCode != null && !airlineCode.isEmpty() ? airlineCode : getAirlineCode();
        flightOffer.put("AirlineCodes", effectiveAirlineCode);

        List<Map<String, Object>> seatMap = generateSeatMap();
        flightOffer.put("seatMap", List.of(seatMap));

        return flightOffer;
    }

    private Map<String, Object> generateItinerary(String departureCode, String arrivalCode, String date, long flightDurationMinutes) {
        // Generate random departure time
        LocalDateTime departureDateTime = LocalDateTime.parse(
                date + "T" + String.format("%02d:%02d:00", random.nextInt(24), random.nextInt(60)),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );

        // Calculate arrival time
        LocalDateTime arrivalDateTime = departureDateTime.plusMinutes(flightDurationMinutes);

        // Format duration as PTxHyM
        long hours = flightDurationMinutes / 60;
        long minutes = flightDurationMinutes % 60;
        String duration = String.format("%dH and %dM", hours, minutes);

        Map<String, Object> departure = Map.of(
                "iataCode", departureCode,
                "terminal", "1",
                "at", departureDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        Map<String, Object> arrival = Map.of(
                "iataCode", arrivalCode,
                "terminal", "2",
                "at", arrivalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        Map<String, Object> segment = new HashMap<>();
        segment.put("departure", departure);
        segment.put("arrival", arrival);
        segment.put("duration", duration);

        return Map.of("duration", duration, "segments", List.of(segment));
    }

    private List<Map<String, Object>> generateSeatMap() {
        List<Map<String, Object>> seats = new ArrayList<>();
        String[] rows = {"1", "3", "7", "8", "10", "11", "12", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32"};
        String[] columns = {"A", "B", "C", "D", "E", "F"};
        String[] columns1 = {"A", "B", "E", "F"};
        String[] columns2 = {"B", "C", "D", "E"};
        for (String row : rows) {
            if (row.equals("1") || row.equals("3")) {
                for (String column : columns1) {
                    seats.add(Map.of(
                            "id", row + "-" + column,
                            "isReserved", random.nextBoolean(),
                            "class", "Business"
                    ));
                }
            } else if (row.equals("20")) {
                for (String column : columns2) {
                    seats.add(Map.of(
                            "id", row + "-" + column,
                            "isReserved", random.nextBoolean(),
                            "class", "Econom-Plus"
                    ));
                }
            } else {
                for (String column : columns) {
                    if (row.equals("7") || row.equals("8") || row.equals("10") || row.equals("11") || row.equals("12") || row.equals("21")) {
                        seats.add(Map.of(
                                "id", row + "-" + column,
                                "isReserved", random.nextBoolean(),
                                "class", "Econom-Plus"
                        ));
                    } else {
                        seats.add(Map.of(
                                "id", row + "-" + column,
                                "isReserved", random.nextBoolean(),
                                "class", "Economy"
                        ));
                    }
                }
            }
        }
        return seats;
    }

    private String getAirlineCode() {
        String[] airlines = {"TU", "AF", "LH", "BA", "DL"};
        return airlines[random.nextInt(airlines.length)];
    }
}