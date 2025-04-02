package com.pfe.flight.config;

import com.amadeus.Amadeus;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmadeusConfig {

    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure().directory("./flight/").load();

    }

    @Bean
    public Amadeus amadeus(Dotenv dotenv) {
        return Amadeus.builder(
                dotenv.get("AMADEUS_API_KEY"),
                dotenv.get("AMADEUS_API_SECRET")
        ).build();
    }
}
