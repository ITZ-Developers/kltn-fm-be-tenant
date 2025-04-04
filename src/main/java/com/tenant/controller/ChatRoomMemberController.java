package com.tenant.controller;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.dto.ResponseListDto;
import com.tenant.dto.chatroomMember.ChatRoomMemberDto;
import com.tenant.exception.BadRequestException;
import com.tenant.form.chatroomMember.CreateChatRoomMemberForm;
import com.tenant.form.chatroomMember.UpdateChatRoomMemberForm;
import com.tenant.mapper.ChatRoomMemberMapper;
import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.model.ChatRoomMember;
import com.tenant.storage.tenant.model.criteria.ChatRoomMemberCriteria;
import com.tenant.storage.tenant.repository.AccountRepository;
import com.tenant.storage.tenant.repository.ChatRoomMemberRepository;
import com.tenant.storage.tenant.repository.ChatRoomRepository;
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
@RequestMapping("/v1/chat-room-member")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatRoomMemberController extends ABasicController {
    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;
    @Autowired
    private ChatRoomRepository chatroomRepository;
    @Autowired
    private AccountRepository accountRepository;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_R_M_V')")
    public ApiMessageDto<ChatRoomMemberDto> get(@PathVariable("id") Long id) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findById(id).orElse(null);
        if (chatRoomMember == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NOT_FOUND, "Not found chat room member");
        }
        return makeSuccessResponse(chatRoomMemberMapper.fromEntityToChatRoomMemberDto(chatRoomMember), "Get chat room member success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_R_M_L')")
    public ApiMessageDto<ResponseListDto<List<ChatRoomMemberDto>>> list(ChatRoomMemberCriteria chatRoomMemberCriteria, Pageable pageable) {
        Page<ChatRoomMember> listChatRoomMember = chatRoomMemberRepository.findAll(chatRoomMemberCriteria.getCriteria(), pageable);
        ResponseListDto<List<ChatRoomMemberDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(chatRoomMemberMapper.fromEntityListToChatRoomMemberDtoList(listChatRoomMember.getContent()));
        responseListObj.setTotalPages(listChatRoomMember.getTotalPages());
        responseListObj.setTotalElements(listChatRoomMember.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list chat room member success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_R_M_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateChatRoomMemberForm form, BindingResult bindingResult) {
        ChatRoomMember chatRoomMember = chatRoomMemberMapper.fromCreateChatRoomMemberFormToEntity(form);
        ChatRoom room = chatroomRepository.findById(form.getRoomId()).orElse(null);
        if (room == null) {
            throw new BadRequestException(ErrorCode.CHATROOM_ERROR_NOT_FOUND, "Not found room");
        }
        chatRoomMember.setChatRoom(room);
        if (Objects.equals(room.getKind(), FinanceConstant.CHATROOM_KIND_GROUP) && checkOwnerChatRoom(getCurrentUser(), room.getId())) {
            throw new BadRequestException(ErrorCode.CHATROOM_ERROR_NO_OWNER, "Owner allow add new member");
        }
        Account member = accountRepository.findById(form.getMemberId()).orElse(null);
        if (member == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found member");
        }
        chatRoomMember.setMember(member);
        chatRoomMemberRepository.save(chatRoomMember);
        return makeSuccessResponse(null, "Create chat room member success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_R_M_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateChatRoomMemberForm form, BindingResult bindingResult) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findById(form.getId()).orElse(null);
        if (chatRoomMember == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NOT_FOUND, "Not found chat room member");
        }
        chatRoomMemberMapper.fromUpdateChatRoomMemberFormToEntity(form, chatRoomMember);
        chatRoomMemberRepository.save(chatRoomMember);
        return makeSuccessResponse(null, "Update chat room member success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_R_M_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findById(id).orElse(null);
        if (chatRoomMember == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NOT_FOUND, "Not found chat room member");
        }
        chatRoomMemberRepository.deleteById(id);
        return makeSuccessResponse(null, "Delete chat room member success");
    }
}