package com.pfe.flight.service;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FlightFaker {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    public List<Map<String, Object>> generateFakeFlightOffers(
            String originLocationCode, String destinationLocationCode,
            String departureDate, String returnDate, int adults) {

        List<Map<String, Object>> flightOffers = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            flightOffers.add(generateFlightOffer(originLocationCode, destinationLocationCode, departureDate, returnDate, i));
        }

        return flightOffers;
    }

    private Map<String, Object> generateFlightOffer(
            String originLocationCode, String destinationLocationCode,
            String departureDate, String returnDate, int id) {

        Map<String, Object> flightOffer = new HashMap<>();
        flightOffer.put("id", id);

        flightOffer.put("oneWay", false);
        List<Map<String, Object>> itineraries = new ArrayList<>();
        itineraries.add(generateItinerary(originLocationCode, destinationLocationCode, departureDate));
        itineraries.add(generateItinerary(destinationLocationCode, originLocationCode, returnDate));
        flightOffer.put("itineraries", itineraries);

        flightOffer.put("price", String.format("%.2f", faker.number().randomDouble(2, 50, 200)));
        String airlineCode = getAirlineCode();
        flightOffer.put("AirlineCodes", airlineCode);

        List<Map<String, Object>> seatMap = generateSeatMap();
        flightOffer.put("seatMap", List.of(seatMap));

        return flightOffer;
    }

    private Map<String, Object> generateItinerary(String departureCode, String arrivalCode, String date) {
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
        segment.put("duration", "PT2H30M");

        return Map.of("duration", "PT2H30M", "segments", List.of(segment));
    }

    private List<Map<String, Object>> generateSeatMap() {
        List<Map<String, Object>> seats = new ArrayList<>();
        String[] rows = {"1", "3", "7", "8", "10", "11", "12", "20", "21", "22","23","24","25","26","27","28","29","30","31","32"};
        String[] columns = {"A", "B", "C", "D", "E", "F"};
        String[] columns1 = {"A", "B","E", "F"};
        String[] columns2 = {"B", "C", "D", "E"};
        for (String row : rows) {
            if(row.equals("1") || row.equals("3")) {
                for (String column : columns1) {
                    seats.add(Map.of(
                            "id", row + "-" + column,
                            "isReserved", random.nextBoolean(),
                            "class","Business"
                    ));
                }
            }
            else if(row.equals("20")){
                for (String column : columns2) {
                    seats.add(Map.of(
                            "id", row + "-" + column,
                            "isReserved", random.nextBoolean(),
                            "class","Econom-Plus"

                    ));
                }
            }
            else {
                for (String column : columns) {
                    if(row.equals("7") || row.equals("8") || row.equals("10") || row.equals("11") || row.equals("12") || row.equals("21")) {
                        seats.add(Map.of(
                                "id", row + "-" + column,
                                "isReserved", random.nextBoolean(),
                                "class","Econom-Plus"
                        ));
                    }
                    else{
                        seats.add(Map.of(
                                "id", row + "-" + column,
                                "isReserved", random.nextBoolean(),
                                "class","Economy"
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
