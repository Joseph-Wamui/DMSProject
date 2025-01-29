package com.emt.dms1.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email")
@Data
public class ProviderConfiguration {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean debug;
}
