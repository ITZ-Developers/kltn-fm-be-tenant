package com.tenant.dto.message;

import com.tenant.dto.ABasicAdminDto;
import com.tenant.dto.account.AccountDto;
import com.tenant.dto.message.reation.MessageReactionDto;
import lombok.Data;
import java.util.*;
@Data
public class MessageDto extends ABasicAdminDto {
    private AccountDto sender;
    private String content;
    private String document;
    private MessageDto parent;
    List<MessageReactionDto> messageReactions;
}