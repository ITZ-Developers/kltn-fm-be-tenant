package com.tenant.form.keyInformationGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateKeyInformationGroupForm {
    @NotBlank(message = "name cannot be null")
    @ApiModelProperty(name = "name", required = true)
    private String name;
    @NotBlank(message = "description cannot be null")
    @ApiModelProperty(name = "description", required = true)
    private String description;
}
