package com.tenant.mapper;
import com.tenant.dto.message.MessageDto;
import com.tenant.form.message.CreateMessageForm;
import com.tenant.form.message.UpdateMessageForm;
import com.tenant.storage.tenant.model.Message;
import org.mapstruct.*;
import java.util.List;
@Mapper(componentModel = "spring", uses = {AccountMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageMapper {
    @Mapping(source = "content", target = "content")
    @Mapping(source = "document", target = "document")
    @Mapping(source = "parent", target = "parent")
    @BeanMapping(ignoreByDefault = true)
    Message fromCreateMessageFormToEntity(CreateMessageForm createMessageForm);

    @Mapping(source = "content", target = "content")
    @Mapping(source = "document", target = "document")
    @Mapping(source = "parent", target = "parent")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateMessageFormToEntity(UpdateMessageForm updateMessageForm, @MappingTarget Message message);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "sender", target = "sender", qualifiedByName = "fromEntityToAccountDtoForNotificationGroup")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "document", target = "document")
    @Mapping(source = "parent", target = "parent")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMessageDto")
    MessageDto fromEntityToMessageDto(Message message);

    @IterableMapping(elementTargetType = MessageDto.class, qualifiedByName = "fromEntityToMessageDto")
    List<MessageDto> fromEntityListToMessageDtoList(List<Message> messageList);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMessageDtoAutoComplete")
    MessageDto fromEntityToMessageDtoAutoComplete(Message message);

    @IterableMapping(elementTargetType = MessageDto.class, qualifiedByName = "fromEntityToMessageDtoAutoComplete")
    List<MessageDto> fromEntityListToMessageDtoListAutoComplete(List<Message> messageList);
}