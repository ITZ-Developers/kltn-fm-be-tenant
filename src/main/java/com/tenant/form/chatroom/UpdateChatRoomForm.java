package com.tenant.form.chatroom;
import com.tenant.validation.ChatroomKind;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data
public class UpdateChatRoomForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;
    @NotBlank(message = "name cannot be blank")
    @ApiModelProperty(required = true)
    private String name;
    @NotBlank(message = "avatar cannot be blank")
    @ApiModelProperty(required = true)
    private String avatar;
    @ChatroomKind
    @ApiModelProperty(required = true)
    private Integer kind;
    @NotNull(message = "ownerId cannot be null")
    @ApiModelProperty(required = true)
    private Long ownerId;
}