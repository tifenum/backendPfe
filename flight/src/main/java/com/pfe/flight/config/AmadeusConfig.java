package com.pfe.flight.config;

import com.amadeus.Amadeus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmadeusConfig {

    @Bean
    public Amadeus amadeus(AmadeusProperties properties) {
        String apiKey = properties.getKey();
        String apiSecret = properties.getSecret();
        System.out.println("apikey :"+apiKey);
        System.out.println("apisecret :"+apiSecret);
        return Amadeus.builder(apiKey, apiSecret).build();
    }
}
