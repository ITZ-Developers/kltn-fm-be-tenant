package com.tenant.mapper;

import com.tenant.form.permission.CreatePermissionForm;
import com.tenant.model.Permission;
import com.tenant.dto.permission.PermissionAdminDto;
import com.tenant.dto.permission.PermissionDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {

    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "nameGroup", target = "nameGroup")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @Mapping(source = "isSystem", target = "isSystem")
    @BeanMapping(ignoreByDefault = true)
    Permission fromCreatePermissionFormToEntity(CreatePermissionForm createPermissionForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "nameGroup", target = "nameGroup")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @Mapping(source = "isSystem", target = "isSystem")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToPermissionAdminDto")
    PermissionAdminDto fromEntityToPermissionAdminDto(Permission permission);

    @IterableMapping(elementTargetType = PermissionAdminDto.class, qualifiedByName = "fromEntityToPermissionAdminDto")
    List<PermissionAdminDto> fromEntityListToPermissionAdminDtoList(List<Permission> permissions);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "nameGroup", target = "nameGroup")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @Mapping(source = "isSystem", target = "isSystem")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToPermissionDto")
    PermissionDto fromEntityToPermissionDto(Permission permission);

    @IterableMapping(elementTargetType = PermissionDto.class, qualifiedByName = "fromEntityToPermissionDto")
    List<PermissionDto> fromEntityListToPermissionDtoList(List<Permission> permissions);
}
