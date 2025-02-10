package com.tenant.scheduler;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.service.ServiceReminderDto;
import com.tenant.model.*;
import com.tenant.repository.*;
import com.tenant.service.KeyService;
import com.tenant.service.TransactionService;
import com.tenant.utils.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ServiceScheduler {
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private KeyService keyService;
    @Autowired
    private TransactionService transactionService;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC") // cron format: second | minute | hour | day of month | month | day of week
    public void checkServiceExpired() {
        List<ServiceReminderDto> serviceReminderDtoList = serviceRepository.getListServiceReminderDto();
        List<Notification> notifications = new ArrayList<>();
        for (ServiceReminderDto dto : serviceReminderDtoList) {
            if (dto.getAccountId() != null) {
                String serviceName = AESUtils.decrypt(keyService.getFinanceSecretKey(), dto.getServiceName(), FinanceConstant.AES_ZIP_ENABLE);
                String dayOrDays = dto.getNumberOfDueDays() != 1 ? " days" : " day";
                String message = "Your " + serviceName + " service is due in " + dto.getNumberOfDueDays() + dayOrDays;
                notifications.add(createNotification(dto.getServiceId(), dto.getAccountId(), message));
            }
            if (dto.getNumberOfDueDays() <= 0) {
                serviceRepository.findById(dto.getServiceId()).ifPresent(service -> {
                    transactionService.updateExpirationDate(service);
                });
            }
        }
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }
    private Notification createNotification(Long serviceId, Long accountId, String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setState(FinanceConstant.NOTIFICATION_STATE_SENT);
        notification.setServiceId(serviceId);
        notification.setAccountId(accountId);
        return notification;
    }
}
