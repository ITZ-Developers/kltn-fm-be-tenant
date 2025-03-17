package com.tenant.service;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.account.KeyWrapperDto;
import com.tenant.dto.account.SubKeyWrapperDto;
import com.tenant.jwt.FinanceJwt;
import com.tenant.storage.tenant.model.*;
import com.tenant.storage.tenant.repository.*;
import com.tenant.service.impl.UserServiceImpl;
import com.tenant.utils.AESUtils;
import com.tenant.utils.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentMap;

@Service
public class KeyService {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private AccountRepository accountRepository;
    private final ConcurrentMap<String, String> concurrentMap;
    @Value("${aes.secret-key.key-information}")
    private String keyInformationSecretKey;
    @Value("${aes.secret-key.finance}")
    private String financeSecretKey;
    @Value("${aes.secret-key.decrypt-password}")
    private String encryptPasswordSecretKey;

    public KeyService(@Qualifier("applicationConfig") ConcurrentMap<String, String> concurrentMap) {
        this.concurrentMap = concurrentMap;
    }

    public long getCurrentUser(){
        FinanceJwt financeJwt = userService.getAddInfoFromToken();
        return financeJwt.getAccountId();
    }

    public String getFinanceSecretKey() {
        return concurrentMap.get(FinanceConstant.FINANCE_SECRET_KEY);
    }

    public String getKeyInformationSecretKey() {
        return concurrentMap.get(FinanceConstant.KEY_INFORMATION_SECRET_KEY);
    }

    public String getDecryptPasswordSecretKey() {
        return concurrentMap.get(FinanceConstant.DECRYPT_PASSWORD_SECRET_KEY);
    }

    public String getUserSecretKey(){
        Account account = accountRepository.findById(getCurrentUser()).orElse(null);
        if (account != null){
            return AESUtils.decrypt(getKeyInformationSecretKey(), account.getSecretKey(), FinanceConstant.AES_ZIP_ENABLE);
        }
        return null;
    }

    public String getUserPublicKey(){
        Account account = accountRepository.findById(getCurrentUser()).orElse(null);
        if (account != null){
            return account.getPublicKey();
        }
        return null;
    }

    public void clearConcurrentMap(){
        concurrentMap.clear();
    }

    public KeyWrapperDto getFinanceKeyWrapper() {
        return new KeyWrapperDto(getFinanceSecretKey(), getUserSecretKey());
    }

    public KeyWrapperDto getKeyInformationKeyWrapper() {
        return new KeyWrapperDto(getKeyInformationSecretKey(), getUserSecretKey());
    }

    public SubKeyWrapperDto getFinanceSubKeyWrapper() {
        return new SubKeyWrapperDto(getFinanceSecretKey(), getUserSecretKey());
    }

    public SubKeyWrapperDto getKeyInformationSubKeyWrapper() {
        return new SubKeyWrapperDto(getKeyInformationSecretKey(), getUserSecretKey());
    }

    public void setMasterKey(String privateKey) {
        String decryptFinanceSecretKey= RSAUtils.decrypt(privateKey, financeSecretKey);
        String decryptKeyInformationSecretKey = RSAUtils.decrypt(privateKey, keyInformationSecretKey);
        String decryptPasswordSecretKey = RSAUtils.decrypt(privateKey, encryptPasswordSecretKey);
        concurrentMap.put(FinanceConstant.PRIVATE_KEY, privateKey);
        concurrentMap.put(FinanceConstant.FINANCE_SECRET_KEY, decryptFinanceSecretKey);
        concurrentMap.put(FinanceConstant.KEY_INFORMATION_SECRET_KEY, decryptKeyInformationSecretKey);
        concurrentMap.put(FinanceConstant.DECRYPT_PASSWORD_SECRET_KEY, decryptPasswordSecretKey);
    }
}
