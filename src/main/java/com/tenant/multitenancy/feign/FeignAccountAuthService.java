package com.tenant.multitenancy.feign;

import com.tenant.dto.account.LoginAuthDto;
import com.tenant.multitenancy.config.CustomFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-svr", url = "${master.url}", configuration = CustomFeignConfig.class)
public interface FeignAccountAuthService {
    String LOGIN_TYPE = "BASIC_LOGIN_AUTH";
    @PostMapping(value = "/api/token")
    LoginAuthDto authLogin(@RequestHeader(LOGIN_TYPE) String type, @RequestParam MultiValueMap<String,String> request);
}
