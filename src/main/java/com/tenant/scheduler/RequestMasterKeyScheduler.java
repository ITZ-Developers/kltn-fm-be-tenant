package com.tenant.scheduler;

import com.tenant.dto.ApiMessageDto;
import com.tenant.multitenancy.dto.MasterKeyDto;
import com.tenant.multitenancy.feign.FeignAccountAuthService;
import com.tenant.service.KeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestMasterKeyScheduler {
    @Autowired
    private FeignAccountAuthService accountAuthService;
    @Autowired
    private KeyService keyService;

    @Scheduled(cron = "0 * * * * *", zone = "UTC")
    public void requestKey() {
        ApiMessageDto<MasterKeyDto> masterKeyDto = accountAuthService.getKey();
        if (masterKeyDto == null || !masterKeyDto.getResult() || masterKeyDto.getData() == null) {
            keyService.clearConcurrentMap();
        } else {
            keyService.setMasterKey(masterKeyDto.getData().getPrivateKey());
        }
    }
}