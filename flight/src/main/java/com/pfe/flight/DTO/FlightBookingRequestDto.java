package com.pfe.flight.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightBookingRequestDto {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("flightDetails")
    private FlightDetails flightDetails;

    @JsonProperty("bookingStatus")
    private String bookingStatus;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightDetails {
        @JsonProperty("isUpsellOffer")
        private Boolean upsellOffer; // Use Boolean wrapper instead of primitive

        @JsonProperty("numberOfBookableSeats")
        private Integer numberOfBookableSeats;

        @JsonProperty("travelerPricings")
        private List<TravelerPricing> travelerPricings;

        @JsonProperty("instantTicketingRequired")
        private Boolean instantTicketingRequired;

        @JsonProperty("validatingAirlineCodes")
        private List<String> validatingAirlineCodes;

        @JsonProperty("source")
        private String source;

        @JsonProperty("type")
        private String type;

        @JsonProperty("oneWay")
        private Boolean oneWay;

        @JsonProperty("price")
        private Price price;

        @JsonProperty("pricingOptions")
        private PricingOptions pricingOptions;

        @JsonProperty("nonHomogeneous")
        private Boolean nonHomogeneous;

        @JsonProperty("itineraries")
        private List<Itinerary> itineraries;

        @JsonProperty("lastTicketingDate")
        private String lastTicketingDate;

        @JsonProperty("id")
        private String id;

        @JsonProperty("lastTicketingDateTime")
        private String lastTicketingDateTime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TravelerPricing {
        @JsonProperty("fareOption")
        private String fareOption;

        @JsonProperty("fareDetailsBySegment")
        private List<FareDetailsBySegment> fareDetailsBySegment;

        @JsonProperty("travelerId")
        private Integer travelerId;

        @JsonProperty("travelerType")
        private String travelerType;

        @JsonProperty("price")
        private Price price;

        @JsonProperty("seatMap")
        private List<Seat> seatMap;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FareDetailsBySegment {
        @JsonProperty("segmentId")
        private String segmentId;

        @JsonProperty("cabin")
        private String cabin;

        @JsonProperty("includedCheckedBags")
        private Baggage includedCheckedBags;

        @JsonProperty("class")
        private String fareClass; // Maps JSON's "class"

        @JsonProperty("fareBasis")
        private String fareBasis;

        @JsonProperty("brandedFare")
        private String brandedFare;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Baggage {
        @JsonProperty("quantity")
        private Integer quantity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        @JsonProperty("fees")
        private List<Fee> fees;

        @JsonProperty("grandTotal")
        private String grandTotal;

        @JsonProperty("additionalServices")
        private List<Service> additionalServices;

        @JsonProperty("base")
        private String base;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("total")
        private String total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fee {
        @JsonProperty("amount")
        private String amount;

        @JsonProperty("type")
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Service {
        @JsonProperty("amount")
        private String amount;

        @JsonProperty("type")
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Seat {
        @JsonProperty("isReserved")
        private Boolean reserved;

        @JsonProperty("id")
        private String id;

        @JsonProperty("class")
        private String seatClass; // Maps JSON's "class"
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PricingOptions {
        @JsonProperty("fareType")
        private List<String> fareType;

        @JsonProperty("includedCheckedBagsOnly")
        private Boolean includedCheckedBagsOnly;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Itinerary {
        @JsonProperty("duration")
        private String duration;

        @JsonProperty("segments")
        private List<Segment> segments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Segment {
        @JsonProperty("duration")
        private String duration;

        @JsonProperty("number")
        private String number;

        @JsonProperty("numberOfStops")
        private Integer numberOfStops;

        @JsonProperty("blacklistedInEU")
        private Boolean blacklistedInEU;

        @JsonProperty("arrival")
        private Arrival arrival;

        @JsonProperty("carrierCode")
        private String carrierCode;

        @JsonProperty("aircraft")
        private Aircraft aircraft;

        @JsonProperty("departure")
        private Departure departure;

        @JsonProperty("operating")
        private Operating operating;

        @JsonProperty("id")
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Arrival {
        @JsonProperty("terminal")
        private String terminal;

        @JsonProperty("iataCode")
        private String iataCode;

        @JsonProperty("at")
        private String at;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Departure {
        @JsonProperty("terminal")
        private String terminal;

        @JsonProperty("iataCode")
        private String iataCode;

        @JsonProperty("at")
        private String at;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Aircraft {
        @JsonProperty("code")
        private String code;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Operating {
        @JsonProperty("carrierCode")
        private String carrierCode;
    }
}