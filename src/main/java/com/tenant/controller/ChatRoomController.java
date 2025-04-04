package com.tenant.controller;
import com.tenant.constant.FinanceConstant;
import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.dto.ResponseListDto;
import com.tenant.dto.chatroom.ChatRoomDto;
import com.tenant.exception.BadRequestException;
import com.tenant.form.chatroom.CreateChatRoomForm;
import com.tenant.form.chatroom.UpdateChatRoomForm;
import com.tenant.mapper.ChatRoomMapper;
import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.model.criteria.ChatRoomCriteria;
import com.tenant.storage.tenant.repository.*;
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
@RequestMapping("/v1/chatroom")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatRoomController extends ABasicController {
    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired
    private ChatRoomRepository chatroomRepository;
    @Autowired
    private ChatRoomMapper chatroomMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageReactionRepository messageReactionRepository ;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_V')")
    public ApiMessageDto<ChatRoomDto> get(@PathVariable("id") Long id) {
        ChatRoom chatroom = chatroomRepository.findById(id).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHATROOM_ERROR_NOT_FOUND, "Not found chatroom");
        }
        return makeSuccessResponse(chatroomMapper.fromEntityToChatRoomDto(chatroom), "Get chatroom success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_L')")
    public ApiMessageDto<ResponseListDto<List<ChatRoomDto>>> list(ChatRoomCriteria chatroomCriteria, Pageable pageable) {
        Page<ChatRoom> listChatRoom = chatroomRepository.findAll(chatroomCriteria.getCriteria(), pageable);
        ResponseListDto<List<ChatRoomDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(chatroomMapper.fromEntityListToChatRoomDtoList(listChatRoom.getContent()));
        responseListObj.setTotalPages(listChatRoom.getTotalPages());
        responseListObj.setTotalElements(listChatRoom.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list chatroom success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ChatRoomDto>>> autoComplete(ChatRoomCriteria chatroomCriteria, @PageableDefault Pageable pageable) {
        chatroomCriteria.setStatus(FinanceConstant.STATUS_ACTIVE);
        Page<ChatRoom> listChatRoom = chatroomRepository.findAll(chatroomCriteria.getCriteria(), pageable);
        ResponseListDto<List<ChatRoomDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(chatroomMapper.fromEntityListToChatRoomDtoListAutoComplete(listChatRoom.getContent()));
        responseListObj.setTotalPages(listChatRoom.getTotalPages());
        responseListObj.setTotalElements(listChatRoom.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list auto-complete chatroom success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateChatRoomForm form, BindingResult bindingResult) {
        ChatRoom chatroom = chatroomMapper.fromCreateChatRoomFormToEntity(form);
        Account owner = accountRepository.findById(form.getOwnerId()).orElse(null);
        if (owner == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found owner");
        }
        chatroom.setOwner(owner);
        chatroomRepository.save(chatroom);
        return makeSuccessResponse(null, "Create chatroom success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateChatRoomForm form, BindingResult bindingResult) {
        ChatRoom chatroom = chatroomRepository.findById(form.getId()).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHATROOM_ERROR_NOT_FOUND, "Not found chatroom");
        }
        chatroomMapper.fromUpdateChatRoomFormToEntity(form, chatroom);
        Account owner = accountRepository.findById(form.getOwnerId()).orElse(null);
        if (owner == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found owner");
        }
        chatroom.setOwner(owner);
        chatroomRepository.save(chatroom);
        return makeSuccessResponse(null, "Update chatroom success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        ChatRoom chatroom = chatroomRepository.findById(id).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHATROOM_ERROR_NOT_FOUND, "Not found chatroom");
        }
        messageReactionRepository.deleteAllByChatRoomId(id);
        messageRepository.deleteAllByChatRoomId(id);
        chatRoomMemberRepository.deleteAllByChatRoomId(id);
        chatroomRepository.deleteById(id);
        return makeSuccessResponse(null, "Delete chatroom success");
    }
}