package com.tenant.dto.chatroom;

import com.tenant.dto.ABasicAdminDto;
import com.tenant.dto.account.AccountDto;
import com.tenant.dto.message.MessageDto;
import lombok.Data;

@Data
public class ChatRoomDto extends ABasicAdminDto {
    private String name;
    private String avatar;
    private Integer kind;
    private AccountDto owner;
    private String settings;

    private long totalMembers;
    private MessageDto lastMessage;
    private Long totalUnreadMessages;

    private String otherFullName;
    private String otherAvatar;
}