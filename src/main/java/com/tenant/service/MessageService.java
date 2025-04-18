package com.tenant.service;

import com.tenant.mapper.ChatRoomMemberMapper;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageService {
    @Autowired
    private MessageReactionRepository messageReactionRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;
    @Autowired
    private ChatRoomRepository chatroomRepository;
    @Autowired
    private AccountRepository accountRepository;
    public void deleteDataOfChatRoom(Long chatroomId){
        messageReactionRepository.deleteAllByChatRoomId(chatroomId);
        messageRepository.updateParentNullByChatRoomId(chatroomId);
        chatRoomMemberRepository.deleteAllByChatRoomId(chatroomId);
        messageRepository.deleteAllByChatRoomId(chatroomId);
        chatroomRepository.deleteById(chatroomId);
    }
    public void deleteDataOfMemberOfChatRoom(Long chatroomId, Long memberId){
        messageReactionRepository.deleteAllByChatRoomIdAndMemberId(chatroomId, memberId);
        messageRepository.updateParentNullByChatRoomIdAndMemberId(chatroomId,memberId);
        messageRepository.deleteAllByChatRoomIdAndMemberId(chatroomId, memberId);
        chatRoomMemberRepository.deleteByChatRoomIdAndMemberId(chatroomId,memberId);
    }
}
