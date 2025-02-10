package com.tenant.dto.setting;

import com.tenant.dto.ABasicAdminDto;
import lombok.Data;

@Data
public class SettingAdminDto extends ABasicAdminDto {
    private String groupName;
    private String keyName;
    private String valueData;
    private String dataType;
    private Boolean isSystem;
}
