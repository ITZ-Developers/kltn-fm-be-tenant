package com.tenant.scheduler;

import com.tenant.model.Setting;
import com.tenant.repository.NotificationRepository;
import com.tenant.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class NotificationScheduler {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Value("${notification.timeout}")
    private Integer notificationTimeoutConfig;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC") // cron format: second | minute | hour | day of month | month | day of week
    public void checkNotificationExpired(){
        Integer notificationTimeout = notificationTimeoutConfig;
        Setting notificationTimeoutSetting = settingRepository.findFirstByKeyName("notification_timeout").orElse(null);
        if (notificationTimeoutSetting != null && StringUtils.isNoneBlank(notificationTimeoutSetting.getValueData()) && Integer.parseInt(notificationTimeoutSetting.getValueData()) > 0){
            notificationTimeout = Integer.parseInt(notificationTimeoutSetting.getValueData());
        }
        LocalDateTime dateTimeBeforeDays = LocalDateTime.now().minusDays(notificationTimeout);
        Date dateBeforeDays = Date.from(dateTimeBeforeDays.atZone(ZoneId.systemDefault()).toInstant());
        notificationRepository.deleteAllNotificationExpired(dateBeforeDays);
    }
}
