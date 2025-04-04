package com.tenant.mapper;
import com.tenant.dto.chatroomMember.ChatRoomMemberDto;
import com.tenant.form.chatroomMember.CreateChatRoomMemberForm;
import com.tenant.form.chatroomMember.UpdateChatRoomMemberForm;
import com.tenant.storage.tenant.model.ChatRoomMember;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ChatRoomMapper.class, AccountMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChatRoomMemberMapper {
    @Mapping(source = "nickName", target = "nickName")
    @BeanMapping(ignoreByDefault = true)
    ChatRoomMember fromCreateChatRoomMemberFormToEntity(CreateChatRoomMemberForm createChatRoomMemberForm);

    @Mapping(source = "nickName", target = "nickName")
    @Mapping(source = "lastReadMessage", target = "lastReadMessage")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateChatRoomMemberFormToEntity(UpdateChatRoomMemberForm updateChatRoomMemberForm, @MappingTarget ChatRoomMember chatroommember);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nickName", target = "nickName")
    @Mapping(source = "room", target = "room", qualifiedByName = "fromEntityToChatRoomDto")
    @Mapping(source = "member", target = "member", qualifiedByName = "fromEntityToAccountDtoForNotificationGroup")
    @Mapping(source = "lastReadMessage", target = "lastReadMessage")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToChatRoomMemberDto")
    ChatRoomMemberDto fromEntityToChatRoomMemberDto(ChatRoomMember chatroommember);

    @IterableMapping(elementTargetType = ChatRoomMemberDto.class, qualifiedByName = "fromEntityToChatRoomMemberDto")
    List<ChatRoomMemberDto> fromEntityListToChatRoomMemberDtoList(List<ChatRoomMember> chatroommemberList);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nickName", target = "nickName")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToChatRoomMemberDtoAutoComplete")
    ChatRoomMemberDto fromEntityToChatRoomMemberDtoAutoComplete(ChatRoomMember chatroommember);

    @IterableMapping(elementTargetType = ChatRoomMemberDto.class, qualifiedByName = "fromEntityToChatRoomMemberDtoAutoComplete")
    List<ChatRoomMemberDto> fromEntityListToChatRoomMemberDtoListAutoComplete(List<ChatRoomMember> chatroommemberList);
}