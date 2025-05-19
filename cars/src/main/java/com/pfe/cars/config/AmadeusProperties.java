package com.pfe.cars.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "amadeus.api")
public class AmadeusProperties {
    private String key;
    private String secret;

}
