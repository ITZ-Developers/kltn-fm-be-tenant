package com.tenant.form.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateMessageForm {
    @NotNull(message = "chatroomId cannot be null")
    @ApiModelProperty(required = true)
    private Long chatroomId;
    private String content;
    private String document;
    @NotNull(message = "parent cannot be blank")
    @ApiModelProperty(required = true)
    private Long parentMessageId;
}