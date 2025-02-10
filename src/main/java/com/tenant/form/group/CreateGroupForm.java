package com.tenant.form.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class CreateGroupForm {
    @NotBlank(message = "name cannot be null")
    @ApiModelProperty(name = "name", required = true)
    private String name;
    @NotBlank(message = "description cannot be null")
    @ApiModelProperty(name = "description", required = true)
    private String description;
    @NotNull(message = "permissions cannot be null")
    @ApiModelProperty(name = "permissions", required = true)
    private Long[] permissions;
    @NotNull(message = "kind cannot be null")
    @ApiModelProperty(name = "kind", required = true)
    private Integer kind;
}
