package com.tenant.dto.setting;

import lombok.Data;

@Data
public class SettingDto {
    private Long id;
    private String groupName;
    private String keyName;
    private String valueData;
    private String dataType;
    private Boolean isSystem;
}
