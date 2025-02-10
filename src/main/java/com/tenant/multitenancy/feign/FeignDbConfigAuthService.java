package com.tenant.multitenancy.feign;

import com.tenant.dto.ApiMessageDto;
import com.tenant.multitenancy.config.CustomFeignConfig;
import com.tenant.multitenancy.dto.DbConfigDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "dbconfig-svr", url = "${auth.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignDbConfigAuthService {
    @GetMapping(value = "/v1/db-config/get-by-name")
    ApiMessageDto<DbConfigDto> getByName(@RequestParam(value = "name") String name);

    @GetMapping(value = "/v1/db-config/get-by-tenant-id")
    ApiMessageDto<DbConfigDto> getByTenantId(@RequestParam(value = "tenantId") String tenantId);
}
