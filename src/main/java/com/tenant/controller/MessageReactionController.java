package com.tenant.controller;

import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.exception.BadRequestException;
import com.tenant.storage.tenant.model.Message;
import com.tenant.storage.tenant.repository.MessageReactionRepository;
import com.tenant.storage.tenant.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/message-reaction")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MessageReactionController extends ABasicController {
    @Autowired
    private MessageReactionRepository messageReactionRepository;
    @Autowired
    private MessageRepository messageRepository;

    @DeleteMapping(value = "/delete/{messageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MES_D')")
    public ApiMessageDto<String> delete(@PathVariable("messageId") Long messageId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Not found message");
        }
        if(!checkIsMemberOfChatRoom(getCurrentUser(),message.getChatRoom().getId())){
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        Long accountId = getCurrentUser();
        messageReactionRepository.deleteAllByAccountIdAndMessageId(accountId,message.getId());
        return makeSuccessResponse(null, "Delete message success");
    }
}
