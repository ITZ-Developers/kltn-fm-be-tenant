package com.tenant.dto.chatroomMember;
import com.tenant.dto.ABasicAdminDto;
import com.tenant.dto.account.AccountDto;
import com.tenant.dto.chatroom.ChatRoomDto;
import com.tenant.dto.message.MessageDto;
import lombok.Data;
@Data
public class ChatRoomMemberDto extends ABasicAdminDto {
    private String nickName;
    private ChatRoomDto room;
    private AccountDto member;
    private MessageDto lastReadMessage;
}