package com.tenant.multitenancy.component;

import com.tenant.multitenancy.feign.FeignAccountAuthService;
import com.tenant.multitenancy.constant.FeignConstant;
import com.tenant.service.impl.UserServiceImpl;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Data
@Slf4j
@Component
public class OAuth2FeignRequestInterceptor implements RequestInterceptor { // cấu hình cho các cuộc gọi đến các service khác
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private static final String BASIC_AUTH_TYPE = "Basic";
    @Value("${auth.internal.basic.username}")
    private String internalAuthUsername;
    @Value("${auth.internal.basic.password}")
    private String internalAuthPassword;
    @Autowired
    private UserServiceImpl userService;

    @Override
    public void apply(RequestTemplate template) {
        if (template.headers().containsKey(FeignAccountAuthService.TOKEN_TYPE)){
            if(Objects.equals(template.headers().get(FeignAccountAuthService.TOKEN_TYPE).toArray()[0], FeignConstant.TOKEN_TYPE_PROJECT)){
                template.header(AUTHORIZATION_HEADER, String.format("%s %s",BEARER_TOKEN_TYPE , userService.getCurrentToken()));
            }else{
                log.error("-----------> not found type = "+template.headers().get(FeignAccountAuthService.TOKEN_TYPE).toArray()[0]);
            }
        }
        else if(template.headers().containsKey(FeignAccountAuthService.LOGIN_TYPE)){
            if(Objects.equals(template.headers().get(FeignAccountAuthService.LOGIN_TYPE).toArray()[0], FeignConstant.LOGIN_TYPE_INTERNAL)){
                String auth = internalAuthUsername + ":" + internalAuthPassword;
                log.error("-----------> internal = "+auth);
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                template.header(AUTHORIZATION_HEADER, String.format("%s %s", BASIC_AUTH_TYPE, new String(encodedAuth)));
            }else{
                log.error("-----------> not found type = "+template.headers().get(FeignAccountAuthService.LOGIN_TYPE).toArray()[0]);
            }
            template.removeHeader(FeignAccountAuthService.LOGIN_TYPE);
        }else{
            log.error("-----------> Constructing Header {} for Token {}, token {}", AUTHORIZATION_HEADER, BEARER_TOKEN_TYPE, String.format("%s %s", BEARER_TOKEN_TYPE, userService.AUTH_SERVER_TOKEN));
            template.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, userService.AUTH_SERVER_TOKEN));
        }
    }
}
