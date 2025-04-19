package com.tenant.form.chatroom;

import com.tenant.validation.ChatroomKind;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
@Data
public class CreateChatRoomForm {
    @NotBlank(message = "name cannot be blank")
    @ApiModelProperty(required = true)
    private String name;
    private String avatar;
    @NotEmpty(message="memberIds can not be empty")
    @ApiModelProperty(required = true)
    @Size(min = 2)
    private List<Long> memberIds = new ArrayList<>();
    @NotBlank(message="settings can not be empty")
    @ApiModelProperty(required = true)
    private String settings;

}