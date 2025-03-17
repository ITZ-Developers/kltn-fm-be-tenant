package com.tenant.controller;

import com.tenant.dto.ApiMessageDto;
import com.tenant.jwt.FinanceJwt;
import com.tenant.multitenancy.feign.FeignDbConfigAuthService;
import com.tenant.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ABasicController {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private FeignDbConfigAuthService dbConfigAuthService;

    public long getCurrentUser(){
        FinanceJwt financeJwt = userService.getAddInfoFromToken();
        return financeJwt.getAccountId();
    }

    public long getTokenId(){
        FinanceJwt financeJwt = userService.getAddInfoFromToken();
        return financeJwt.getTokenId();
    }

    public FinanceJwt getSessionFromToken(){
        return userService.getAddInfoFromToken();
    }

    public boolean isSuperAdmin(){
        FinanceJwt financeJwt = userService.getAddInfoFromToken();
        if(financeJwt !=null){
            return financeJwt.getIsSuperAdmin();
        }
        return false;
    }

    public <T> ApiMessageDto<T> makeErrorResponse(String code, String message){
        ApiMessageDto<T> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setResult(false);
        apiMessageDto.setCode(code);
        apiMessageDto.setMessage(message);
        return apiMessageDto;
    }

    public <T> ApiMessageDto<T> makeSuccessResponse(T data, String message){
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
