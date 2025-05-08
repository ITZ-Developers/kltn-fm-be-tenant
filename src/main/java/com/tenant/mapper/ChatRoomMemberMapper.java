package com.tenant.mapper;
import com.tenant.dto.account.KeyWrapperDto;
import com.tenant.dto.chatroomMember.ChatRoomMemberDto;
import com.tenant.form.chatroomMember.CreateChatRoomMemberForm;
import com.tenant.form.chatroomMember.UpdateChatRoomMemberForm;
import com.tenant.storage.tenant.model.ChatRoomMember;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ChatRoomMapper.class, AccountMapper.class, MessageMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChatRoomMemberMapper extends EncryptDecryptMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "chatRoom", target = "room", qualifiedByName = "fromEntityToChatRoomDto")
    @Mapping(source = "member", target = "member", qualifiedByName = "fromEntityToAccountDtoAutoComplete")
    @Mapping(source = "lastReadMessage", target = "lastReadMessage", qualifiedByName = "fromEntityToMessageDto" )
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToChatRoomMemberDto")
    ChatRoomMemberDto fromEntityToChatRoomMemberDto(ChatRoomMember chatroommember, @Context KeyWrapperDto keyWrapper);

    @IterableMapping(elementTargetType = ChatRoomMemberDto.class, qualifiedByName = "fromEntityToChatRoomMemberDto")
    List<ChatRoomMemberDto> fromEntityListToChatRoomMemberDtoList(List<ChatRoomMember> chatroommemberList, @Context KeyWrapperDto keyWrapper);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToChatRoomMemberDtoAutoComplete")
    ChatRoomMemberDto fromEntityToChatRoomMemberDtoAutoComplete(ChatRoomMember chatroommember);

    @IterableMapping(elementTargetType = ChatRoomMemberDto.class, qualifiedByName = "fromEntityToChatRoomMemberDtoAutoComplete")
    List<ChatRoomMemberDto> fromEntityListToChatRoomMemberDtoListAutoComplete(List<ChatRoomMember> chatroommemberList);
}