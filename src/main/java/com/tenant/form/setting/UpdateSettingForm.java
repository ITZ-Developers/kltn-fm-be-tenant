package com.tenant.form.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateSettingForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;
    @NotBlank(message = "valueData cannot be null")
    @ApiModelProperty(name = "valueData", required = true)
    private String valueData;
    @NotNull(message = "status cannot be null")
    @ApiModelProperty(name = "status", required = true)
    private Integer status;
    @ApiModelProperty(name = "groupName")
    private String groupName;
    @ApiModelProperty(name = "keyName")
    private String keyName;
    @ApiModelProperty(name = "dataType")
    private String dataType;
    @ApiModelProperty(name = "isSystem")
    private Boolean isSystem;
}
