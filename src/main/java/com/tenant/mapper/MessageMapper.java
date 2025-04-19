package com.tenant.mapper;

import com.tenant.dto.account.KeyWrapperDto;
import com.tenant.dto.message.MessageDto;
import com.tenant.form.message.CreateMessageForm;
import com.tenant.form.message.UpdateMessageForm;
import com.tenant.storage.tenant.model.Message;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AccountMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageMapper extends EncryptDecryptMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "sender", target = "sender", qualifiedByName = "fromEntityToAccountDtoAutoComplete")
    @Mapping(target = "content", expression = "java(decryptAndEncrypt(keyWrapper, message.getContent()))")
    @Mapping(target = "document", expression = "java(decryptAndEncrypt(keyWrapper, message.getDocument()))")
    @Mapping(source = "parent", target = "parent", qualifiedByName = "fromEntityToMessageShortDto")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMessageDto")
    MessageDto fromEntityToMessageDto(Message message, @Context KeyWrapperDto keyWrapper);

    @IterableMapping(elementTargetType = MessageDto.class, qualifiedByName = "fromEntityToMessageDto")
    List<MessageDto> fromEntityListToMessageDtoList(List<Message> messageList, @Context KeyWrapperDto keyWrapper);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "content", expression = "java(decryptAndEncrypt(keyWrapper, message.getContent()))")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMessageDtoAutoComplete")
    MessageDto fromEntityToMessageDtoAutoComplete(Message message, @Context KeyWrapperDto keyWrapper);

    @IterableMapping(elementTargetType = MessageDto.class, qualifiedByName = "fromEntityToMessageDtoAutoComplete")
    List<MessageDto> fromEntityListToMessageDtoListAutoComplete(List<Message> messageList, @Context KeyWrapperDto keyWrapper);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "content", expression = "java(decryptAndEncrypt(keyWrapper, message.getContent()))")
    @Mapping(target = "document", expression = "java(decryptAndEncrypt(keyWrapper, message.getDocument()))")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMessageShortDto")
    MessageDto fromEntityToMessageShortDto(Message message, @Context KeyWrapperDto keyWrapper);

    @IterableMapping(elementTargetType = MessageDto.class, qualifiedByName = "fromEntityToMessageShortDto")
    @Named("fromEntityToMessageShortDtoList")
    List<MessageDto> fromEntityToMessageShortDtoList(List<Message> messageList, @Context KeyWrapperDto keyWrapper);
}