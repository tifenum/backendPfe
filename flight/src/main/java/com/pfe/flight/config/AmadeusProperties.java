package com.pfe.flight.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "amadeus.api")
@Setter
@Getter
public class AmadeusProperties {
    private String key;
    private String secret;
}
