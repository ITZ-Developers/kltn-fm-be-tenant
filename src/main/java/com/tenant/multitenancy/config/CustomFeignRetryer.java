package com.tenant.multitenancy.config;

import com.tenant.dto.account.LoginAuthDto;
import com.tenant.multitenancy.component.ApplicationContextProvider;
import com.tenant.multitenancy.feign.FeignAccountAuthService;
import com.tenant.multitenancy.constant.FeignConstant;
import com.tenant.service.impl.UserServiceImpl;
import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
public class CustomFeignRetryer implements Retryer {
    private final int maxAttempts;
    private final long backoff;
    private int attempt;

    @Autowired
    public CustomFeignRetryer(@Value("${feign.client.retryer.config.maxAttempt}") int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.backoff = 1000;
    }

    public CustomFeignRetryer(int maxAttempts, long backoff) {
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
        this.attempt = 0;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            throw e;
        }
        if (e.status() == 401 || e.status() == 403) {
            log.error("Feign retry attempt {} due to {} ", attempt, e.getMessage());

            e.request().requestTemplate().removeHeader("Authorization");

            ApplicationContext applicationContext = ApplicationContextProvider.getContext();
            FeignAccountAuthService feignAccountAuthService = applicationContext.getBean(FeignAccountAuthService.class);

            Environment environment = applicationContext.getEnvironment();
            String username = environment.getProperty("auth.internal.username");
            String password = environment.getProperty("auth.internal.password");

            UserServiceImpl userService = applicationContext.getBean(UserServiceImpl.class);
            userService.AUTH_SERVER_TOKEN = getNewAccessToken(feignAccountAuthService, username, password);

            try {
                Thread.sleep(backoff);
            } catch (InterruptedException exi) {
                Thread.currentThread().interrupt();
            }
        } else {
            throw e;
        }
    }

    public String getNewAccessToken(FeignAccountAuthService feignAccountAuthService, String username, String password){
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "password");
        request.add("username", username);
        request.add("password", password);
        LoginAuthDto response = feignAccountAuthService.authLogin(FeignConstant.LOGIN_TYPE_INTERNAL, request);
        if (response == null || response.getAccessToken() == null){
            throw new RuntimeException("CAN NOT GET NEW KEY");
        }
        return response.getAccessToken();
    }

    @Override
    public Retryer clone() {
        return new CustomFeignRetryer(maxAttempts, backoff);
    }
}
