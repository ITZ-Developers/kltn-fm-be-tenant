package com.tenant.form.account;

import com.tenant.validation.PasswordConstraint;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RequestKeyForm {
    @NotBlank(message = "password cannot be null.")
    @PasswordConstraint
    @ApiModelProperty(name = "password", required = true)
    private String password;
}
