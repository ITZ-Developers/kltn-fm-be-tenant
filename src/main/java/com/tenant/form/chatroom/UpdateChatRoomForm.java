package com.tenant.form.chatroom;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.chatroom.settings.SettingJsonFormat;
import com.tenant.validation.ChatroomKind;
import com.tenant.validation.ValidJsonField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateChatRoomForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;
    private String name;
    private String avatar;

    @ValidJsonField(classType = SettingJsonFormat.class, allowNull = true)
    @ApiModelProperty(name = "settings", required = false)
    private String settings = FinanceConstant.CHAT_ROOM_SETTING_SAMPLE_DATA;

}