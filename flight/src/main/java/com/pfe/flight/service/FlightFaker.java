package com.pfe.flight.service;

import com.github.javafaker.Faker;
import com.github.javafaker.Aviation;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FlightFaker {

    private final Faker faker = new Faker();
    // Use the Aviation class from JavaFaker (see documentation)
    private final Aviation aviation = faker.aviation();
    private final Random random = new Random();

    public List<Map<String, Object>> generateFakeFlightOffers(
            String originLocationCode, String destinationLocationCode,
            String departureDate, String returnDate, int adults) {

        List<Map<String, Object>> flightOffers = new ArrayList<>();

        // Generate 3 fake flight offers
        for (int i = 1; i <= 3; i++) {
            flightOffers.add(generateFlightOffer(originLocationCode, destinationLocationCode, departureDate, returnDate, i));
        }

        return flightOffers;
    }

    private Map<String, Object> generateFlightOffer(
            String originLocationCode, String destinationLocationCode,
            String departureDate, String returnDate, int id) {

        Map<String, Object> flightOffer = new HashMap<>();
        flightOffer.put("type", "flight-offer");
        flightOffer.put("id", String.valueOf(id));
        flightOffer.put("source", "GDS");
        flightOffer.put("instantTicketingRequired", false);
        flightOffer.put("nonHomogeneous", false);
        flightOffer.put("oneWay", false);
        flightOffer.put("isUpsellOffer", false);
        flightOffer.put("lastTicketingDate", getRandomDate(departureDate));
        flightOffer.put("lastTicketingDateTime", getRandomDate(departureDate));
        flightOffer.put("numberOfBookableSeats", random.nextInt(10) + 1);

        // Itineraries: one for departure and one for return
        List<Map<String, Object>> itineraries = new ArrayList<>();
        itineraries.add(generateItinerary(originLocationCode, destinationLocationCode, departureDate));
        itineraries.add(generateItinerary(destinationLocationCode, originLocationCode, returnDate));
        flightOffer.put("itineraries", itineraries);

        // Price
        flightOffer.put("price", generatePrice());

        // Pricing Options
        flightOffer.put("pricingOptions", Map.of("fareType", List.of("PUBLISHED"), "includedCheckedBagsOnly", false));

        // Validating Airline (using a helper since Aviation doesn't provide one)
        String airlineCode = getAirlineCode();
        flightOffer.put("validatingAirlineCodes", List.of(airlineCode));

        // Traveler Pricing
        flightOffer.put("travelerPricings", List.of(generateTravelerPricing(id)));

        return flightOffer;
    }

    private Map<String, Object> generateItinerary(String departureCode, String arrivalCode, String date) {
        // For the carrier code, we use our helper method
        String carrierCode = getAirlineCode();
        // For the aircraft, use the Aviation method per the documentation
        String aircraftCode = aviation.aircraft(); // e.g. "An-2"
        // Simulate a flight number (3-digit number)
        String flightNumber = String.valueOf(random.nextInt(900) + 100);

        // Generate random departure and arrival times based on the provided date
        String departureTime = date + "T" + String.format("%02d:%02d:00", random.nextInt(24), random.nextInt(60));
        String arrivalTime = date + "T" + String.format("%02d:%02d:00", random.nextInt(24), random.nextInt(60));

        Map<String, Object> departure = Map.of(
                "iataCode", departureCode,
                "terminal", "1",
                "at", departureTime
        );

        Map<String, Object> arrival = Map.of(
                "iataCode", arrivalCode,
                "terminal", "2",
                "at", arrivalTime
        );

        Map<String, Object> segment = new HashMap<>();
        segment.put("departure", departure);
        segment.put("arrival", arrival);
        segment.put("carrierCode", carrierCode);
        segment.put("number", flightNumber);
        segment.put("aircraft", Map.of("code", aircraftCode));
        segment.put("operating", Map.of("carrierCode", carrierCode));
        segment.put("duration", "PT2H30M");
        segment.put("id", UUID.randomUUID().toString());
        segment.put("numberOfStops", 0);
        segment.put("blacklistedInEU", false);

        return Map.of("duration", "PT2H30M", "segments", List.of(segment));
    }

    private Map<String, Object> generatePrice() {
        double basePrice = faker.number().randomDouble(2, 50, 200);
        double extraFees = faker.number().randomDouble(2, 10, 50);
        double totalPrice = basePrice + extraFees;

        return Map.of(
                "currency", "EUR",
                "total", String.format("%.2f", totalPrice),
                "base", String.format("%.2f", basePrice),
                "fees", List.of(
                        Map.of("amount", "0.00", "type", "SUPPLIER"),
                        Map.of("amount", "0.00", "type", "TICKETING")
                ),
                "grandTotal", String.format("%.2f", totalPrice),
                "additionalServices", List.of(
                        Map.of("amount", "55.41", "type", "CHECKED_BAGS")
                )
        );
    }

    private Map<String, Object> generateTravelerPricing(int travelerId) {
        return Map.of(
                "travelerId", String.valueOf(travelerId),
                "fareOption", "STANDARD",
                "travelerType", "ADULT",
                "price", generatePrice(),
                "fareDetailsBySegment", List.of(
                        Map.of(
                                "segmentId", UUID.randomUUID().toString(),
                                "cabin", "ECONOMY",
                                "fareBasis", "TL10R0B",
                                "brandedFare", "LIGHT",
                                "brandedFareLabel", "ECONOMY LIGHT",
                                "class", "T",
                                "includedCheckedBags", Map.of("quantity", 0),
                                "includedCabinBags", Map.of("weight", 8, "weightUnit", "KG"),
                                "amenities", List.of(
                                        Map.of(
                                                "description", "CHECKED BAG 23KG 01PC 158CM",
                                                "isChargeable", true,
                                                "amenityType", "BAGGAGE",
                                                "amenityProvider", Map.of("name", "BrandedFare")
                                        ),
                                        Map.of(
                                                "description", "MEAL GRILL",
                                                "isChargeable", false,
                                                "amenityType", "MEAL",
                                                "amenityProvider", Map.of("name", "BrandedFare")
                                        ),
                                        Map.of(
                                                "description", "STANDARD SEAT RESERVATION",
                                                "isChargeable", true,
                                                "amenityType", "BRANDED_FARES",
                                                "amenityProvider", Map.of("name", "BrandedFare")
                                        ),
                                        Map.of(
                                                "description", "MILES ACCRUAL",
                                                "isChargeable", false,
                                                "amenityType", "BRANDED_FARES",
                                                "amenityProvider", Map.of("name", "BrandedFare")
                                        ),
                                        Map.of(
                                                "description", "CHANGEABLE TICKET",
                                                "isChargeable", true,
                                                "amenityType", "BRANDED_FARES",
                                                "amenityProvider", Map.of("name", "BrandedFare")
                                        )
                                )
                        )
                )
        );
    }

    // Returns a random date between 1 and 5 days after the provided reference date (format: YYYY-MM-DD)
    private String getRandomDate(String referenceDate) {
        LocalDate date = LocalDate.parse(referenceDate, DateTimeFormatter.ISO_DATE);
        int daysToAdd = random.nextInt(5) + 1;
        return date.plusDays(daysToAdd).toString();
    }

    // Helper to simulate an airline code (since Aviation doesn't provide one)
    private String getAirlineCode() {
        String[] airlines = {"TU", "AF", "LH", "BA", "DL"};
        return airlines[random.nextInt(airlines.length)];
    }
}
