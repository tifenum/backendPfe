package com.pfe.flight.config;

import com.amadeus.Amadeus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmadeusConfig {
    @Bean
    public Amadeus amadeus() {
        return Amadeus.builder("t52s39mJ2t8lj7MkNrJ1xaemdQQSAO3O", "GbbYQOlrnp3LXd1l").build();
    }
}
