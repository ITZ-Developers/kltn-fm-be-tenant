package com.tenant.controller;

import com.tenant.constant.FinanceConstant;
import com.tenant.service.KeyService;
import com.tenant.service.MessageService;
import com.tenant.service.chat.ChatService;
import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.dto.ResponseListDto;
import com.tenant.dto.chatroomMember.ChatRoomMemberDto;
import com.tenant.exception.BadRequestException;
import com.tenant.form.chatroomMember.CreateChatRoomMemberForm;
import com.tenant.mapper.ChatRoomMemberMapper;
import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.model.ChatRoomMember;
import com.tenant.storage.tenant.model.criteria.ChatRoomMemberCriteria;
import com.tenant.storage.tenant.repository.*;
import com.tenant.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private MessageService messageService;
    @Autowired
    private KeyService keyService;
    @Autowired
    private ChatService chatService;

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ChatRoomMemberDto>>> list(ChatRoomMemberCriteria chatRoomMemberCriteria, Pageable pageable) {
        if (chatRoomMemberCriteria.getIsPaged().equals(FinanceConstant.IS_PAGED_FALSE)) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Page<ChatRoomMember> listChatRoomMember = chatRoomMemberRepository.findAll(chatRoomMemberCriteria.getCriteria(), pageable);
        ResponseListDto<List<ChatRoomMemberDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(chatRoomMemberMapper.fromEntityListToChatRoomMemberDtoList(listChatRoomMember.getContent(), keyService.getFinanceKeyWrapper()));
        responseListObj.setTotalPages(listChatRoomMember.getTotalPages());
        responseListObj.setTotalElements(listChatRoomMember.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list chat room member success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ChatRoomMemberDto>>> autoComplete(ChatRoomMemberCriteria chatRoomMemberCriteria, @PageableDefault Pageable pageable) {
        chatRoomMemberCriteria.setStatus(FinanceConstant.STATUS_ACTIVE);
        if (chatRoomMemberCriteria.getIsPaged().equals(FinanceConstant.IS_PAGED_FALSE)) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Page<ChatRoomMember> listChatRoomMember = chatRoomMemberRepository.findAll(chatRoomMemberCriteria.getCriteria(), pageable);
        ResponseListDto<List<ChatRoomMemberDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(chatRoomMemberMapper.fromEntityListToChatRoomMemberDtoListAutoComplete(listChatRoomMember.getContent()));
        responseListObj.setTotalPages(listChatRoomMember.getTotalPages());
        responseListObj.setTotalElements(listChatRoomMember.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list auto-complete chatroom success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> create(@Valid @RequestBody CreateChatRoomMemberForm form, BindingResult bindingResult) {
        Account currentUser = accountRepository.findById(getCurrentUser()).orElse(null);
        if (currentUser == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found current user");
        }
        List<ChatRoomMember> chatRoomMembers = new ArrayList<>();
        ChatRoom chatroom = chatroomRepository.findById(form.getRoomId()).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_FOUND, "Not found room");
        }
        if (!Objects.equals(chatroom.getKind(), FinanceConstant.CHATROOM_KIND_GROUP)) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_KIND_GROUP, "Chat room kind is not kind group");
        }
        boolean isOwnerOfChatRoom = checkOwnerChatRoom(getCurrentUser(), chatroom.getId());
        boolean allowNotOwnerCanInvite = messageService.getSettingOfChatRoom(chatroom.getSettings()).getMember_permissions().getAllow_invite_members();
        boolean isMemberOfChatRoom = checkIsMemberOfChatRoom(getCurrentUser(), chatroom.getId());
        if (!isMemberOfChatRoom) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Current user is not member of group");
        }
        if (!allowNotOwnerCanInvite && !isOwnerOfChatRoom) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_UNABLE_INVITE_NEW_MEMBERS, "Owner allow add new member");
        }
        List<Account> accounts = accountRepository.findAllByIdInAndStatusAndIdNot(form.getMemberIds(), FinanceConstant.STATUS_ACTIVE, currentUser.getId());
        if (accounts.isEmpty()) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NEW_CHAT_ROOM_MEMBERS_EMPTY, "New members is empty");
        }
        List<Long> accountIds = accounts.stream().map(Account::getId).collect(Collectors.toList());
        if (chatRoomMemberRepository.checkExistChatRoomMemberInAccountIds(accountIds, chatroom.getId())) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_EXISTED_IN_CHAT_ROOM, "Exists account in chat room");
        }
        for (Account account : accounts) {
            ChatRoomMember chatRoomMember = new ChatRoomMember();
            chatRoomMember.setMember(account);
            chatRoomMember.setChatRoom(chatroom);
            chatRoomMembers.add(chatRoomMember);
        }
        chatRoomMemberRepository.saveAll(chatRoomMembers);
        chatService.sendMsgChatRoomCreated(chatroom.getId(), accountIds);
        chatService.sendMsgChatRoomUpdated(chatroom.getId(), chatRoomMemberRepository.findAllMemberIdsByChatRoomIdAndNotIn(chatroom.getId(), accountIds));
        return makeSuccessResponse(null, "Create chat room member success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> delete(@PathVariable("id") Long id, BindingResult bindingResult) {
        Long currentUserId = getCurrentUser();
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findById(id).orElse(null);
        if (chatRoomMember == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NOT_FOUND, "Not found chat room member");
        }
        ChatRoom chatRoom = chatRoomMember.getChatRoom();
        if (!Objects.equals(chatRoom.getKind(), FinanceConstant.CHATROOM_KIND_GROUP)) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_KIND_GROUP, "Chat room kind is not kind group");
        }
        if (!Objects.equals(chatRoomMember.getMember().getId(),chatRoom.getOwner().getId())) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_IS_OWNER, "Not found chat room member");
        }
        boolean isOwnerOfChatRoom = checkOwnerChatRoom(currentUserId, chatRoomMember.getChatRoom().getId());
        if (!isOwnerOfChatRoom) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NO_OWNER, "Can not delete member if not owner");
        }
        if (chatRoom.getChatRoomMembers().size() == 3) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_KIND_GROUP_HAS_MORE_THAN_3, "number of list members can not less than 2");
        }
        messageService.deleteDataOfMemberOfChatRoom(chatRoom.getId(), chatRoomMember.getMember().getId());
        chatService.sendMsgChatRoomDeleted(chatRoom.getId(), List.of(chatRoomMember.getMember().getId()));
        chatService.sendMsgChatRoomUpdated(chatRoom.getId(), chatRoomMemberRepository.findAllMemberIdsByChatRoomIdAndNotIn(chatRoom.getId(), List.of(chatRoomMember.getMember().getId())));
        return makeSuccessResponse(null, "Delete chat room member success");
    }

    @DeleteMapping(value = "/leave/{chatroomId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> leave(@PathVariable("chatroomId") Long chatroomId) {
        Account currentUser = accountRepository.findById(getCurrentUser()).orElse(null);
        if (currentUser == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found current user");
        }
        ChatRoom chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_FOUND, "Not found room");
        }
        Long currentUserId = currentUser.getId();
        boolean isOwnerOfChatRoom = checkOwnerChatRoom(currentUserId, chatroom.getId());
        boolean isMember = checkIsMemberOfChatRoom(currentUserId, chatroomId);
        if (!isMember) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_NO_JOIN, "Current user is not member of chatroom");
        }
        if (chatroom.getChatRoomMembers().size() == 3 && !isOwnerOfChatRoom) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_KIND_GROUP_HAS_MORE_THAN_3, "number of list members can not less than 2");
        }
        if (!FinanceConstant.CHATROOM_KIND_GROUP.equals(chatroom.getKind())) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_KIND_GROUP, "Chat room is not kind group");
        }
        if (isOwnerOfChatRoom) {
            messageService.deleteDataOfChatRoom(chatroomId);
            chatService.sendMsgChatRoomDeleted(chatroom.getId());
        } else {
            messageService.deleteDataOfMemberOfChatRoom(chatroomId, currentUserId);
            chatService.sendMsgChatRoomDeleted(chatroomId, List.of(currentUserId));
            chatService.sendMsgChatRoomUpdated(chatroomId, chatRoomMemberRepository.findAllMemberIdsByChatRoomIdAndNotIn(chatroomId, List.of(currentUserId)));
        }
        return makeSuccessResponse(null, "Create chat room member success");
    }
}