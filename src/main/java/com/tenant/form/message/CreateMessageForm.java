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
    @NotBlank(message = "content cannot be blank")
    @ApiModelProperty(required = true)
    private String content;
    @NotBlank(message = "document cannot be blank")
    @ApiModelProperty(required = true)
    private String document;
    @NotBlank(message = "parent cannot be blank")
    @ApiModelProperty(required = true)
    private String parent;
}