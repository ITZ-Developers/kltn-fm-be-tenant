package com.tenant.dto.message;

import com.tenant.dto.ABasicAdminDto;
import com.tenant.dto.account.AccountDto;
import lombok.Data;

@Data
public class MessageDto extends ABasicAdminDto {
    private AccountDto sender;
    private String content;
    private String document;
    private MessageDto parent;
}