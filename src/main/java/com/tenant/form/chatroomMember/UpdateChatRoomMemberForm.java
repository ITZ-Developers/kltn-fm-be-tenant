package com.tenant.form.chatroomMember;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.*;

@Data
public class UpdateChatRoomMemberForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;
    @NotBlank(message = "nickName cannot be blank")
    @ApiModelProperty(required = true)
    private String nickName;
    @NotBlank(message = "lastReadMessage cannot be blank")
    @ApiModelProperty(required = true)
    private String lastReadMessage;
}