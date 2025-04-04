package com.tenant.controller;

import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.dto.ResponseListDto;
import com.tenant.dto.message.MessageDto;
import com.tenant.exception.BadRequestException;
import com.tenant.form.message.CreateMessageForm;
import com.tenant.form.message.UpdateMessageForm;
import com.tenant.mapper.MessageMapper;
import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.model.Message;
import com.tenant.storage.tenant.model.criteria.MessageCriteria;
import com.tenant.storage.tenant.repository.AccountRepository;
import com.tenant.storage.tenant.repository.ChatRoomRepository;
import com.tenant.storage.tenant.repository.MessageReactionRepository;
import com.tenant.storage.tenant.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v1/message")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MessageController extends ABasicController {
    @Autowired
    private MessageReactionRepository messageReactionRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ChatRoomRepository chatroomRepository;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MES_V')")
    public ApiMessageDto<MessageDto> get(@PathVariable("id") Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message == null) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Not found message");
        }
        return makeSuccessResponse(messageMapper.fromEntityToMessageDto(message), "Get message success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MES_L')")
    public ApiMessageDto<ResponseListDto<List<MessageDto>>> list(MessageCriteria messageCriteria, Pageable pageable) {
        Page<Message> listMessage = messageRepository.findAll(messageCriteria.getCriteria(), pageable);
        ResponseListDto<List<MessageDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(messageMapper.fromEntityListToMessageDtoList(listMessage.getContent()));
        responseListObj.setTotalPages(listMessage.getTotalPages());
        responseListObj.setTotalElements(listMessage.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list message success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MES_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateMessageForm form, BindingResult bindingResult) {
        Message message = messageMapper.fromCreateMessageFormToEntity(form);
        Account sender = accountRepository.findById(getCurrentUser()).orElse(null);
        if (sender == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found sender");
        }
        message.setSender(sender);
        ChatRoom chatRoom = chatroomRepository.findById(form.getChatroomId()).orElse(null);
        if (chatRoom == null) {
            throw new BadRequestException(ErrorCode.CHATROOM_ERROR_NOT_FOUND, "Not found chatRoom");
        }
        message.setChatRoom(chatRoom);
        if(!checkIsMemberOfChatRoom(getCurrentUser(),message.getChatRoom().getId())){
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        messageRepository.save(message);
        return makeSuccessResponse(null, "Create message success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MES_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateMessageForm form, BindingResult bindingResult) {
        Message message = messageRepository.findById(form.getId()).orElse(null);
        if (message == null) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Not found message");
        }
        if(!checkIsMemberOfChatRoom(getCurrentUser(),message.getChatRoom().getId())){
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        if (!Objects.equals(message.getSender().getId(), getCurrentUser())) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NO_OWNER, "Not found message");
        }
        if(!checkIsMemberOfChatRoom(getCurrentUser(),message.getChatRoom().getId())){
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        messageMapper.fromUpdateMessageFormToEntity(form, message);
        messageRepository.save(message);
        return makeSuccessResponse(null, "Update message success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MES_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message == null) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Not found message");
        }
        if(!checkIsMemberOfChatRoom(getCurrentUser(),message.getChatRoom().getId())){
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        if (!Objects.equals(message.getSender().getId(), getCurrentUser())) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NO_OWNER, "Not found message");
        }
        messageRepository.deleteById(id);
        messageReactionRepository.deleteAllByMessageId(id);
        return makeSuccessResponse(null, "Delete message success");
    }

}
