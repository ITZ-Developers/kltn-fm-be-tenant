package com.tenant.form.organizationPermission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateOrganizationPermissionForm {
    @NotNull(message = "accountId cannot be null")
    @ApiModelProperty(name = "accountId", required = true)
    private Long accountId;
    @NotNull(message = "organizationId cannot be null")
    @ApiModelProperty(name = "organizationId", required = true)
    private Long organizationId;
}
