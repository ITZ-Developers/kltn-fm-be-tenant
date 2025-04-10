package com.tenant.controller;

import com.tenant.constant.SecurityConstant;
import com.tenant.dto.ApiMessageDto;
import com.tenant.jwt.FinanceJwt;
import com.tenant.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ABasicController {
    @Autowired
    private UserServiceImpl userService;

    public long getCurrentUser() {
        try {
            FinanceJwt financeJwt = userService.getAddInfoFromToken();
            return financeJwt.getAccountId();
        } catch (Exception e) {
            return -1000L;
        }
    }

    public String getCurrentGrantType() {
        try {
            Map<String, Object> attributes = userService.getAttributesFromToken();
            return String.valueOf(attributes.get("grant_type"));
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isCustomer() {
        FinanceJwt financeJwt = userService.getAddInfoFromToken();
        if (financeJwt != null) {
            return SecurityConstant.USER_KIND_CUSTOMER.equals(financeJwt.getUserKind());
        }
        return false;
    }

    public <T> ApiMessageDto<T> makeErrorResponse(String code, String message) {
        ApiMessageDto<T> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setResult(false);
        apiMessageDto.setCode(code);
        apiMessageDto.setMessage(message);
        return apiMessageDto;
    }

    public <T> ApiMessageDto<T> makeSuccessResponse(T data, String message) {
        ApiMessageDto<T> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setData(data);
        apiMessageDto.setMessage(message);
        return apiMessageDto;
    }

    public boolean hasRole(String permissionCode) {
        List<String> authorities = userService.getAuthorities();
        return authorities != null && authorities.contains(permissionCode);
    }
}
