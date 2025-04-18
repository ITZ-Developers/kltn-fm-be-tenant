package com.tenant.controller;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.dto.ResponseListDto;
import com.tenant.dto.chatroom.*;
import com.tenant.exception.BadRequestException;
import com.tenant.form.chatroom.CreateChatRoomDirectForm;
import com.tenant.form.chatroom.CreateChatRoomForm;
import com.tenant.form.chatroom.UpdateChatRoomForm;
import com.tenant.mapper.ChatRoomMapper;
import com.tenant.mapper.MessageMapper;
import com.tenant.storage.tenant.model.*;
import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.model.criteria.ChatRoomCriteria;
import com.tenant.storage.tenant.repository.*;
import com.tenant.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/chat-room")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatRoomController extends ABasicController {
    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired
    private ChatRoomRepository chatroomRepository;
    @Autowired
    private ChatRoomMapper chatRoomMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageReactionRepository messageReactionRepository;
    @Autowired
    private MessageMapper messageMapper;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ChatRoomDto> get(@PathVariable("id") Long id) {
        ChatRoom chatroom = chatroomRepository.findById(id).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_FOUND, "Not found chatroom");
        }
        return makeSuccessResponse(chatRoomMapper.fromEntityToChatRoomDto(chatroom), "Get chatroom success");
    }

    /**
     * @return rooms for getCurrentUser
     */
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ChatRoomDto>>> list(ChatRoomCriteria chatroomCriteria, Pageable pageable) {
        ResponseListDto<List<ChatRoomDto>> responseListObj = new ResponseListDto<>();
        if (chatroomCriteria.getIsPaged().equals(FinanceConstant.IS_PAGED_FALSE)) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Long currentUserId = getCurrentUser();
        chatroomCriteria.setMemberId(currentUserId);
        chatroomCriteria.setShouldSortByLastMessage(true);
        Page<ChatRoom> chatRoomPage = chatroomRepository.findAll(chatroomCriteria.getCriteria(), pageable);
        List<ChatRoom> chatRooms = chatRoomPage.getContent();
        List<Long> chatRoomIds = chatRooms.stream().map(ChatRoom::getId).collect(Collectors.toList());

        Map<Long, Long> memberCountMap = chatroomRepository.countMembersByChatRoomIds(chatRoomIds)
                .stream()
                .collect(Collectors.toMap(ChatRoomMemberCountInterface::getChatRoomId, ChatRoomMemberCountInterface::getMemberCount));
        Map<Long, Message> lastMessageMap = chatroomRepository.findLastMessagesByChatRoomIds(chatRoomIds)
                .stream()
                .collect(Collectors.toMap(ChatRoomLastMessageInterface::getChatRoomId, ChatRoomLastMessageInterface::getLastMessage));
        Map<Long, Long> unreadCountMap = chatroomRepository.countUnreadMessages(chatRoomIds, currentUserId)
                .stream()
                .collect(Collectors.toMap(ChatRoomUnreadCountInterface::getChatRoomId, ChatRoomUnreadCountInterface::getUnreadCount));
        Map<Long, OtherMemberInfoInterface> otherMemberMap = chatroomRepository.findOtherMembersInDirectMessages(chatRoomIds, currentUserId, FinanceConstant.CHATROOM_KIND_DIRECT_MESSAGE)
                .stream()
                .collect(Collectors.toMap(
                        OtherMemberInfoInterface::getChatRoomId,
                        Function.identity()));

        List<ChatRoomDto> dtos = chatRooms.stream().map(chatRoom -> {
            ChatRoomDto dto = chatRoomMapper.fromEntityToChatRoomDto(chatRoom);
            dto.setTotalMembers(memberCountMap.getOrDefault(chatRoom.getId(), 0L).intValue());
            if (Objects.equals(chatRoom.getKind(), FinanceConstant.CHATROOM_KIND_DIRECT_MESSAGE)) {
                OtherMemberInfoInterface otherMember = otherMemberMap.get(chatRoom.getId());
                if (otherMember != null) {
                    dto.setOtherFullName(otherMember.getFullName());
                    dto.setOtherAvatar(otherMember.getAvatar());
                }
            }
            Message lastMessage = lastMessageMap.get(chatRoom.getId());
            if (lastMessage != null) {
                dto.setLastMessage(messageMapper.fromEntityToMessageDto(lastMessage));
            }
            dto.setTotalUnreadMessages(unreadCountMap.getOrDefault(chatRoom.getId(), 0L));
            return dto;
        }).collect(Collectors.toList());

        responseListObj.setContent(dtos);
        responseListObj.setTotalPages(chatRoomPage.getTotalPages());
        responseListObj.setTotalElements(chatRoomPage.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list chatroom success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ChatRoomDto>>> autoComplete(ChatRoomCriteria chatroomCriteria, @PageableDefault Pageable pageable) {
        chatroomCriteria.setStatus(FinanceConstant.STATUS_ACTIVE);
        if (chatroomCriteria.getIsPaged().equals(FinanceConstant.IS_PAGED_FALSE)) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Page<ChatRoom> listChatRoom = chatroomRepository.findAll(chatroomCriteria.getCriteria(), pageable);
        ResponseListDto<List<ChatRoomDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(chatRoomMapper.fromEntityListToChatRoomDtoListAutoComplete(listChatRoom.getContent()));
        responseListObj.setTotalPages(listChatRoom.getTotalPages());
        responseListObj.setTotalElements(listChatRoom.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list auto-complete chatroom success");
    }

    @PostMapping(value = "/create-group", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_I')")
    public ApiMessageDto<String> createGroup(@Valid @RequestBody CreateChatRoomForm form, BindingResult bindingResult) {
        ChatRoom chatroom = chatRoomMapper.fromCreateChatRoomFormToEntity(form);
        Account owner = accountRepository.findById(getCurrentUser()).orElse(null);
        if (owner == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found owner");
        }
        chatroom.setOwner(owner);
        chatroom.setSettings(FinanceConstant.CHAT_ROOM_SETTING_SAMPLE_DATA);
        chatroom.setKind(FinanceConstant.CHATROOM_KIND_GROUP);

        List<ChatRoomMember> chatRoomMembers = new ArrayList<>();
        ChatRoomMember chatRoomMemberOwner = new ChatRoomMember();
        chatRoomMemberOwner.setMember(owner);
        chatRoomMemberOwner.setChatRoom(chatroom);
        chatRoomMembers.add(chatRoomMemberOwner);

        List<Account> accounts = accountRepository.findAllByIdInAndStatus(form.getMemberIds(), FinanceConstant.STATUS_ACTIVE);
        if (accounts.size() < 2) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_KIND_GROUP_HAS_MORE_THAN_3, "number of list members can not less than 2");
        }
        for (Account account : accounts) {
            ChatRoomMember newChatRoomMember = new ChatRoomMember();
            newChatRoomMember.setMember(account);
            newChatRoomMember.setChatRoom(chatroom);
            chatRoomMembers.add(newChatRoomMember);
        }
        chatroomRepository.save(chatroom);
        chatRoomMemberRepository.saveAll(chatRoomMembers);
        return makeSuccessResponse(null, "Create chatroom group success");
    }

    @PostMapping(value = "/create-direct-message", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_I')")
    public ApiMessageDto<ChatRoomDto> createDirectMessage(@Valid @RequestBody CreateChatRoomDirectForm form, BindingResult bindingResult) {
        ChatRoom chatroom = new ChatRoom();
        Account owner = accountRepository.findById(getCurrentUser()).orElse(null);
        if (owner == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found owner");
        }
        chatroom.setOwner(owner);
        chatroom.setKind(FinanceConstant.CHATROOM_KIND_DIRECT_MESSAGE);
        Account other = accountRepository.findById(form.getAccountId()).orElse(null);
        if (other == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found owner");
        }

        List<ChatRoomMember> chatRoomMembers = new ArrayList<>();
        ChatRoomMember chatRoomMemberOwner = new ChatRoomMember();
        chatRoomMemberOwner.setMember(owner);
        chatRoomMemberOwner.setChatRoom(chatroom);
        chatRoomMembers.add(chatRoomMemberOwner);

        ChatRoomMember chatRoomMemberOther = new ChatRoomMember();
        chatRoomMemberOther.setMember(other);
        chatRoomMemberOther.setChatRoom(chatroom);
        chatRoomMembers.add(chatRoomMemberOther);

        chatroomRepository.save(chatroom);
        chatRoomMemberRepository.saveAll(chatRoomMembers);

        ChatRoomDto newChatRoomDto = chatRoomMapper.fromEntityToChatRoomDto(chatroom);
        return makeSuccessResponse(newChatRoomDto, "Create chatroom direct message success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_I')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateChatRoomForm form, BindingResult bindingResult) {
        ChatRoom chatroom = chatroomRepository.findById(form.getId()).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_FOUND, "Not found chatroom");
        }
        if (FinanceConstant.CHATROOM_KIND_DIRECT_MESSAGE.equals(chatroom.getKind())) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_DIRECT_MESSAGE_NOT_UPDATE, "chat room direct can not update");
        }
        Boolean allowNotOwnerCanUpdate = Boolean.valueOf(JSONUtils.getDataByKey(chatroom.getSettings(), FinanceConstant.CHAT_ROOM_SETTING_ALLOW_UPDATE));
        Boolean checkIsOwner = checkOwnerChatRoom(getCurrentUser(), chatroom.getId());
        if (!checkIsOwner && !allowNotOwnerCanUpdate) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_MEMBER_ERROR_IS_NOT_OWNER_AND_NOT_ALLOW_UPDATE, "Chat room updated by owner or not owner if allow to update");
        }

        chatRoomMapper.fromUpdateChatRoomFormToEntity(form, chatroom);
        if(checkIsOwner){
            chatroom.setSettings(form.getSettings());
        }

        chatroomRepository.save(chatroom);
        return makeSuccessResponse(null, "Update chatroom success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CHA_I')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        ChatRoom chatroom = chatroomRepository.findById(id).orElse(null);
        if (chatroom == null) {
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NOT_FOUND, "Not found chatroom");
        }
        Boolean checkIsOwner = checkOwnerChatRoom(getCurrentUser(), chatroom.getId());
        Boolean checkIsMember = checkIsMemberOfChatRoom(getCurrentUser(),chatroom.getId());
        if(FinanceConstant.CHATROOM_KIND_GROUP.equals(chatroom.getKind()) && !checkIsOwner){
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NO_OWNER,"Can not delete if not owner");
        }
        if(FinanceConstant.CHATROOM_KIND_DIRECT_MESSAGE.equals(chatroom.getKind()) && !checkIsMember){
            throw new BadRequestException(ErrorCode.CHAT_ROOM_ERROR_NO_OWNER, "Can not delete if not owner");
        }
        messageReactionRepository.deleteAllByChatRoomId(id);
        messageRepository.updateParentNullByChatRoomId(id);
        chatRoomMemberRepository.updateLastMessageNullByChatRoomId(id);
        messageRepository.deleteAllByChatRoomId(id);
        chatRoomMemberRepository.deleteAllByChatRoomId(id);
        chatroomRepository.deleteById(id);
        return makeSuccessResponse(null, "Delete chatroom success");
    }
}