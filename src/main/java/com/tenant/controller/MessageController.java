package com.tenant.controller;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.dto.ResponseListDto;
import com.tenant.dto.message.MessageDto;
import com.tenant.exception.BadRequestException;
import com.tenant.exception.NotFoundException;
import com.tenant.form.message.CreateMessageForm;
import com.tenant.form.message.UpdateMessageForm;
import com.tenant.mapper.MessageMapper;
import com.tenant.service.KeyService;
import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.model.Message;
import com.tenant.storage.tenant.model.criteria.MessageCriteria;
import com.tenant.storage.tenant.repository.*;
import com.tenant.utils.AESUtils;
import com.tenant.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private ChatRoomMemberRepository chatRoomMemberRepository;
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
    @Autowired
    private KeyService keyService;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<MessageDto> get(@PathVariable("id") Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message == null) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Not found message");
        }
        return makeSuccessResponse(messageMapper.fromEntityToMessageDto(message), "Get message success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<MessageDto>>> list(MessageCriteria messageCriteria, Pageable pageable) {
        if (messageCriteria.getIsPaged().equals(FinanceConstant.IS_PAGED_FALSE)) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Page<Message> listMessage = messageRepository.findAll(messageCriteria.getCriteria(), pageable);
        ResponseListDto<List<MessageDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(messageMapper.fromEntityListToMessageDtoList(listMessage.getContent()));
        responseListObj.setTotalPages(listMessage.getTotalPages());
        responseListObj.setTotalElements(listMessage.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list message success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> create(@Valid @RequestBody CreateMessageForm form, BindingResult bindingResult) {
        if (!StringUtils.isNotBlank(form.getContent()) && !StringUtils.isNotBlank(form.getDocument())) {
            throw new BadRequestException("Required content or document");
        }
        Message message = new Message();
        String userSecretKey = keyService.getUserSecretKey();
        if (StringUtils.isNotBlank(form.getDocument())) {
            String messageDocument = AESUtils.decrypt(userSecretKey, form.getDocument(), FinanceConstant.AES_ZIP_ENABLE);
            message.setDocument(AESUtils.encrypt(keyService.getFinanceSecretKey(), messageDocument, FinanceConstant.AES_ZIP_ENABLE));
        }
        if (StringUtils.isNotBlank(form.getContent())) {
            String messageContent = AESUtils.decrypt(userSecretKey, form.getContent(), FinanceConstant.AES_ZIP_ENABLE);
            message.setContent(AESUtils.encrypt(keyService.getFinanceSecretKey(), messageContent, FinanceConstant.AES_ZIP_ENABLE));
        }
        Long currentId = getCurrentUser();
        Account sender = accountRepository.findById(currentId).orElse(null);
        if (sender == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found sender");
        }
        ChatRoom chatRoom = chatroomRepository.findById(form.getChatroomId()).orElse(null);
        if (chatRoom == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_FOUND, "Not found chatRoom");
        }
        boolean isMemberOfChatRoom = checkIsMemberOfChatRoom(currentId, chatRoom.getId());
        boolean isOwnerOfChatRoom = checkOwnerChatRoom(currentId, chatRoom.getId());
        boolean allowSendMessages = Boolean.valueOf(JSONUtils.getDataByKey(chatRoom.getSettings(), FinanceConstant.CHAT_ROOM_SETTING_ALLOW_SEND_MESSAGES));
        if (!isMemberOfChatRoom) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        if (!isOwnerOfChatRoom && !allowSendMessages) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_IS_NOT_SEND_MESSAGES, "Members unable to send message");
        }
        if (form.getParentMessageId() != null) {
            Message parent = messageRepository.findFirstByIdAndChatRoomId(form.getParentMessageId(), chatRoom.getId()).orElse(null);
            if (parent == null) {
                throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Message parent can not found");
            }
            message.setParent(parent);
        }
        message.setSender(sender);
        message.setChatRoom(chatRoom);
        messageRepository.save(message);
        return makeSuccessResponse(null, "Create message success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateMessageForm form, BindingResult bindingResult) {
        if (!StringUtils.isNotBlank(form.getContent()) && !StringUtils.isNotBlank(form.getDocument())) {
            throw new BadRequestException("Required content or document");
        }
        Message message = messageRepository.findById(form.getId()).orElse(null);
        if (message == null) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Not found message");
        }
        String userSecretKey = keyService.getUserSecretKey();
        if (StringUtils.isNotBlank(form.getDocument())) {
            String messageDocument = AESUtils.decrypt(userSecretKey, form.getDocument(), FinanceConstant.AES_ZIP_ENABLE);
            message.setDocument(AESUtils.encrypt(keyService.getFinanceSecretKey(), messageDocument, FinanceConstant.AES_ZIP_ENABLE));
        }
        if (StringUtils.isNotBlank(form.getContent())) {
            String messageContent = AESUtils.decrypt(userSecretKey, form.getContent(), FinanceConstant.AES_ZIP_ENABLE);
            message.setContent(AESUtils.encrypt(keyService.getFinanceSecretKey(), messageContent, FinanceConstant.AES_ZIP_ENABLE));
        }
        Long currentId = getCurrentUser();
        ChatRoom chatroom = new ChatRoom();
        boolean isMemberOfChatRoom = checkIsMemberOfChatRoom(currentId, message.getChatRoom().getId());
        boolean isOwnerOfChatRoom = checkOwnerChatRoom(currentId, chatroom.getId());
        boolean allowSendMessages = Boolean.valueOf(JSONUtils.getDataByKey(chatroom.getSettings(), FinanceConstant.CHAT_ROOM_SETTING_ALLOW_SEND_MESSAGES));
        if (!isMemberOfChatRoom) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        if (!Objects.equals(message.getSender().getId(), getCurrentUser())) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NO_OWNER, "Not found message");
        }
        if (!isOwnerOfChatRoom && !allowSendMessages) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_IS_NOT_SEND_MESSAGES, "Members unable to update message");
        }
        if (!checkIsMemberOfChatRoom(getCurrentUser(), message.getChatRoom().getId())) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        message.setDocument(form.getDocument());
        message.setContent(form.getContent());
        messageRepository.save(message);
        return makeSuccessResponse(null, "Update message success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message == null) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NOT_FOUND, "Not found message");
        }
        Long currentId = getCurrentUser();
        ChatRoom chatroom = new ChatRoom();
        boolean isMemberOfChatRoom = checkIsMemberOfChatRoom(currentId, message.getChatRoom().getId());
        boolean isOwnerOfChatRoom = checkOwnerChatRoom(currentId, chatroom.getId());
        if (!isMemberOfChatRoom) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Account no in this room");
        }
        if (!isOwnerOfChatRoom && !Objects.equals(message.getSender().getId(), getCurrentUser())) {
            throw new BadRequestException(ErrorCode.MESSAGE_ERROR_NO_OWNER, "Not found message");
        }
        messageReactionRepository.deleteAllByMessageId(id);
        chatRoomMemberRepository.updateLastMessageNullByMessageId(id);
        messageRepository.deleteById(id);
        return makeSuccessResponse(null, "Delete message success");
    }

}
