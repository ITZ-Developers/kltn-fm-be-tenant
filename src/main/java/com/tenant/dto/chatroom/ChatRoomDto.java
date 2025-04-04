package com.tenant.dto.chatroom;

import com.tenant.dto.ABasicAdminDto;
import com.tenant.dto.account.AccountDto;
import lombok.Data;

@Data
public class ChatRoomDto extends ABasicAdminDto {
    private String name;
    private String avatar;
    private Integer kind;
    private AccountDto owner;
}