package com.tenant.form.chatroomMember;

import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;

@Data
public class CreateChatRoomMemberForm {
    @NotBlank(message = "nickName cannot be blank")
    @ApiModelProperty(required = true)
    private String nickName;
    @NotNull(message = "roomId cannot be null")
    @ApiModelProperty(required = true)
    private Long roomId;
    @NotNull(message = "memberId cannot be null")
    @ApiModelProperty(required = true)
    private Long memberId;
}