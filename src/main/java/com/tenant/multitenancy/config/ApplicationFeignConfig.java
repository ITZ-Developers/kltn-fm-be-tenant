package com.tenant.multitenancy.config;

import com.tenant.dto.account.LoginAuthDto;
import com.tenant.multitenancy.feign.FeignAccountAuthService;
import com.tenant.multitenancy.constant.FeignConstant;
import com.tenant.service.impl.UserServiceImpl;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Slf4j
@Configuration
@EnableConfigurationProperties({LiquibaseProperties.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationFeignConfig {
    @Autowired
    FeignAccountAuthService accountAuthService;
    @Autowired
    UserServiceImpl userService;
    @Value("${auth.internal.username}")
    String username;
    @Value("${auth.internal.password}")
    String password;

    @PostConstruct
    public void initialize() {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "password");
        request.add("username", username);
        request.add("password", password);
        LoginAuthDto result = accountAuthService.authLogin(FeignConstant.LOGIN_TYPE_INTERNAL, request);
        if (result == null || result.getAccessToken() == null) {
            log.error("Failed to retrieve access token");
            throw new IllegalStateException("Application failed to start: Cannot retrieve access token");
        }
        userService.AUTH_SERVER_TOKEN = result.getAccessToken();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
