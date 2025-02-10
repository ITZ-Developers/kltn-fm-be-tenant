package com.tenant.controller;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.account.MyKeyDto;
import com.tenant.exception.BadRequestException;
import com.tenant.form.account.*;
import com.tenant.mapper.AccountMapper;
import com.tenant.model.Account;
import com.tenant.model.Department;
import com.tenant.repository.*;
import com.tenant.service.FinanceApiService;
import com.tenant.service.KeyService;
import com.tenant.utils.*;
import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.dto.ResponseListDto;
import com.tenant.dto.account.AccountAdminDto;
import com.tenant.dto.account.AccountDto;
import com.tenant.dto.account.AccountForgetPasswordDto;
import com.tenant.model.Group;
import com.tenant.model.criteria.AccountCriteria;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/account")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AccountController extends ABasicController{
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private KeyInformationRepository keyInformationRepository;
    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private FinanceApiService financeApiService;
    @Autowired
    private KeyService keyService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_V')")
    public ApiMessageDto<AccountAdminDto> get(@PathVariable("id") Long id) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        return makeSuccessResponse(accountMapper.fromEntityToAccountAdminDto(account), "Get account success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_L')")
    public ApiMessageDto<ResponseListDto<List<AccountAdminDto>>> list(AccountCriteria accountCriteria, Pageable pageable) {
        if (accountCriteria.getIsPaged().equals(FinanceConstant.IS_PAGED_FALSE)){
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Page<Account> accounts = accountRepository.findAll(accountCriteria.getCriteria(), pageable);
        ResponseListDto<List<AccountAdminDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(accountMapper.fromEntityListToAccountAdminDtoList(accounts.getContent()));
        responseListObj.setTotalPages(accounts.getTotalPages());
        responseListObj.setTotalElements(accounts.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list account success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<AccountDto>>> autoComplete(AccountCriteria accountCriteria) {
        Pageable pageable = accountCriteria.getIsPaged().equals(FinanceConstant.IS_PAGED_TRUE) ? PageRequest.of(0, 10) : PageRequest.of(0, Integer.MAX_VALUE);
        accountCriteria.setStatus(FinanceConstant.STATUS_ACTIVE);
        Page<Account> accounts = accountRepository.findAll(accountCriteria.getCriteria(), pageable);
        ResponseListDto<List<AccountDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(accountMapper.fromEntityListToAccountDtoListAutoComplete(accounts.getContent()));
        responseListObj.setTotalPages(accounts.getTotalPages());
        responseListObj.setTotalElements(accounts.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list account success");
    }

    @PostMapping(value = "/create-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_C_AD')")
    public ApiMessageDto<String> createAdmin(@Valid @RequestBody CreateAccountAdminForm createAccountAdminForm, BindingResult bindingResult) {
        Integer count = accountRepository.countAllAccounts();
        String maxAccountString = Utils.getValueFromJsonByKey(getCurrentDbConfig().getLicense(), "max_account");
        if (maxAccountString != null) {
            Integer maxAccount = Integer.parseInt(maxAccountString);
            if (count >= maxAccount) {
                return makeErrorResponse(null, "Reached the limit");
            }
        }
        Account accountByUsername = accountRepository.findFirstByUsername(createAccountAdminForm.getUsername()).orElse(null);
        if (accountByUsername != null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_USERNAME_EXISTED, "Username existed");
        }
        Account accountByEmail = accountRepository.findFirstByEmail(createAccountAdminForm.getEmail()).orElse(null);
        if (accountByEmail != null){
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED, "Email existed");
        }
        Account accountByPhone = accountRepository.findFirstByPhone(createAccountAdminForm.getPhone()).orElse(null);
        if (accountByPhone != null){
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED, "Phone existed");
        }
        if (createAccountAdminForm.getBirthDate() != null && createAccountAdminForm.getBirthDate().after(new Date())) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_BIRTHDATE_INVALID, "Birthdate is invalid");
        }
        Group group = groupRepository.findById(createAccountAdminForm.getGroupId()).orElse(null);
        if (group == null) {
            return makeErrorResponse(ErrorCode.GROUP_ERROR_NOT_FOUND, "Not found group");
        }
        Department department = departmentRepository.findById(createAccountAdminForm.getDepartmentId()).orElse(null);
        if (department == null){
            return makeErrorResponse(ErrorCode.DEPARTMENT_ERROR_NOT_FOUND, "Not found department");
        }
        Account account = accountMapper.fromCreateAccountAdminFormToEntity(createAccountAdminForm);
        account.setPassword(passwordEncoder.encode(createAccountAdminForm.getPassword()));
        account.setKind(FinanceConstant.USER_KIND_ADMIN);
        account.setGroup(group);
        account.setDepartment(department);
        accountRepository.save(account);
        return makeSuccessResponse(null, "Create account admin success");
    }

    private String getUniqueSecretKey(){
        String encryptSecretKey;
        Account existAccount;
        String secretKeyInformation = keyService.getKeyInformationSecretKey();
        do {
            encryptSecretKey = AESUtils.encrypt(secretKeyInformation, GenerateUtils.generateRandomString(16), FinanceConstant.AES_ZIP_ENABLE) ;
            existAccount = accountRepository.findFirstBySecretKey(encryptSecretKey).orElse(null);
        } while (existAccount != null);
        return encryptSecretKey;
    }

    @PutMapping(value = "/update-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_U_AD')")
    public ApiMessageDto<String> updateAdmin(@Valid @RequestBody UpdateAccountAdminForm updateAccountAdminForm, BindingResult bindingResult) {
        Account account = accountRepository.findById(updateAccountAdminForm.getId()).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        if (updateAccountAdminForm.getEmail() != null && !updateAccountAdminForm.getEmail().equals(account.getEmail())){
            Account accountByEmail = accountRepository.findFirstByEmail(updateAccountAdminForm.getEmail()).orElse(null);
            if (accountByEmail != null){
                return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED, "Email existed");
            }
        }
        if (updateAccountAdminForm.getPhone() != null && !updateAccountAdminForm.getPhone().equals(account.getPhone())){
            Account accountByPhone = accountRepository.findFirstByPhone(updateAccountAdminForm.getPhone()).orElse(null);
            if (accountByPhone != null){
                return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED, "Phone existed");
            }
        }
        Group group = groupRepository.findById(updateAccountAdminForm.getGroupId()).orElse(null);
        if (group == null) {
            return makeErrorResponse(ErrorCode.GROUP_ERROR_NOT_FOUND, "Not found group");
        }
        Department department = departmentRepository.findById(updateAccountAdminForm.getDepartmentId()).orElse(null);
        if (department == null){
            return makeErrorResponse(ErrorCode.DEPARTMENT_ERROR_NOT_FOUND, "Not found department");
        }
        if (StringUtils.isNoneBlank(updateAccountAdminForm.getAvatarPath())) {
            if(!updateAccountAdminForm.getAvatarPath().equals(account.getAvatarPath())){
                financeApiService.deleteFile(account.getAvatarPath());
            }
            account.setAvatarPath(updateAccountAdminForm.getAvatarPath());
        }
        if (updateAccountAdminForm.getBirthDate() != null && updateAccountAdminForm.getBirthDate().after(new Date())) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_BIRTHDATE_INVALID, "Birthdate is invalid");
        }
        accountMapper.fromUpdateAccountAdminFormToEntity(updateAccountAdminForm, account);
        account.setGroup(group);
        account.setDepartment(department);
        if (account.getStatus() != FinanceConstant.STATUS_ACTIVE) {
            account.setSecretKey(null);
            account.setPublicKey(null);
        }
        accountRepository.save(account);
        return makeSuccessResponse(null, "Update account admin success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        if (account.getIsSuperAdmin()) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_DELETE_SUPPER_ADMIN, "Not allow to delete super admin");
        }
        if (Long.valueOf(getCurrentUser()).equals(account.getId())){
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_DELETE_YOURSELF, "Not allow to delete yourself");
        }
        financeApiService.deleteFile(account.getAvatarPath());
        transactionHistoryRepository.updateAllByAccountId(id);
        keyInformationRepository.updateAllByAccountId(id);
        accountRepository.deleteById(id);
        return makeSuccessResponse(null, "Delete account success");
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<AccountDto> profile() {
        Account account = accountRepository.findById(getCurrentUser()).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        return makeSuccessResponse(accountMapper.fromEntityToAccountDto(account), "Get profile success");
    }

    @PutMapping(value = "/update-profile-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> updateProfileAdmin(@Valid @RequestBody UpdateProfileAdminForm updateProfileAdminForm, BindingResult bindingResult) {
        Account account = accountRepository.findById(getCurrentUser()).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        if(!passwordEncoder.matches(updateProfileAdminForm.getOldPassword(), account.getPassword())){
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD, "Old password is incorrect");
        }
        if (StringUtils.isNoneBlank(updateProfileAdminForm.getAvatarPath())) {
            if(!updateProfileAdminForm.getAvatarPath().equals(account.getAvatarPath())){
                //delete old image
                financeApiService.deleteFile(account.getAvatarPath());
            }
            account.setAvatarPath(updateProfileAdminForm.getAvatarPath());
        }
        if (updateProfileAdminForm.getBirthDate() != null && updateProfileAdminForm.getBirthDate().after(new Date())) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_BIRTHDATE_INVALID, "Birthdate is invalid");
        }
        accountMapper.fromUpdateProfileAdminFormToEntity(updateProfileAdminForm, account);
        accountRepository.save(account);
        return makeSuccessResponse(null, "Update profile success");
    }

    @PostMapping(value = "/request-forget-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<AccountForgetPasswordDto> requestForgetPassword(@Valid @RequestBody RequestForgetPasswordForm forgetForm, BindingResult bindingResult){
        Account account = accountRepository.findFirstByEmail(forgetForm.getEmail()).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        String otp = financeApiService.getOTPForgetPassword();
        account.setAttemptCode(0);
        account.setResetPwdCode(otp);
        account.setResetPwdTime(new Date());
        accountRepository.save(account);
        financeApiService.sendEmail(account.getEmail(),"OTP: " + otp, "Request forget password successful, please check email",false);
        AccountForgetPasswordDto accountForgetPasswordDto = new AccountForgetPasswordDto();
        String zipUserId = ZipUtils.zipString(account.getId()+ ";" + otp);
        accountForgetPasswordDto.setUserId(zipUserId);
        return makeSuccessResponse(accountForgetPasswordDto, "Request forget password successful, please check email");
    }

    @PostMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> forgetPassword(@Valid @RequestBody ResetPasswordForm resetPasswordForm, BindingResult bindingResult){
        String[] unzip = ZipUtils.unzipString(resetPasswordForm.getUserId()).split(";", 2);
        Long id = ConvertUtils.convertStringToLong(unzip[0]);
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null ) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        if(account.getAttemptCode() >= FinanceConstant.MAX_ATTEMPT_FORGET_PWD){
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_EXCEEDED_NUMBER_OF_INPUT_ATTEMPT_OTP, "Exceeded number of input attempt OTP");
        }
        if(!account.getResetPwdCode().equals(resetPasswordForm.getOtp()) ||
                (new Date().getTime() - account.getResetPwdTime().getTime() >= FinanceConstant.MAX_TIME_FORGET_PWD)){
            account.setAttemptCode(account.getAttemptCode() + 1);
            accountRepository.save(account);
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_OTP_INVALID, "OTP code invalid or has expired");
        }
        if (passwordEncoder.matches(resetPasswordForm.getNewPassword(), account.getPassword())) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NEW_PASSWORD_INVALID, "New password must be different from old password");
        }
        account.setResetPwdTime(null);
        account.setResetPwdCode(null);
        account.setAttemptCode(null);
        account.setPassword(passwordEncoder.encode(resetPasswordForm.getNewPassword()));
        accountRepository.save(account);
        return makeSuccessResponse(null, "Reset password success");
    }

    @GetMapping(value = "/my-key", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<MyKeyDto> myKey() {
        String encryptSecretKey = RSAUtils.encrypt(keyService.getUserPublicKey(), keyService.getUserSecretKey());
        MyKeyDto myKeyDto = new MyKeyDto();
        myKeyDto.setSecretKey(encryptSecretKey);
        return makeSuccessResponse(myKeyDto, "My key success");
    }

    @PostMapping("/request-key")
    public ResponseEntity<Resource> requestKey(@Valid @RequestBody RequestKeyForm requestKeyForm, BindingResult bindingResult){
        Account account = accountRepository.findById(getCurrentUser()).orElse(null);
        if (account == null) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        if (!passwordEncoder.matches(requestKeyForm.getPassword(), account.getPassword())){
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_REQUEST_KEY, "Wrong password");
        }
        if (account.getStatus() == FinanceConstant.STATUS_PENDING) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_REQUEST_KEY, "Account status is currently pending");
        }
        if (account.getStatus() == FinanceConstant.STATUS_LOCK) {
            throw new BadRequestException(ErrorCode.ACCOUNT_ERROR_NOT_ALLOW_REQUEST_KEY, "Account has already been locked");
        }
        KeyPair keyPair = RSAUtils.generateKeyPair();
        String publicKey = RSAUtils.keyToString(keyPair.getPublic());
        String privateKey = RSAUtils.keyToString(keyPair.getPrivate());
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("-----BEGIN PRIVATE KEY-----").append("\n");
        contentBuilder.append(privateKey).append("\n");
        contentBuilder.append("-----END PRIVATE KEY-----").append("\n");
        account.setPublicKey(publicKey);
        account.setSecretKey(getUniqueSecretKey());
        accountRepository.save(account);
        byte[] contentBytes = contentBuilder.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource byteArrayResource = new ByteArrayResource(contentBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "key_information.txt");
        return ResponseEntity.ok()
                .headers(headers)
                .body(byteArrayResource);
    }

    @PutMapping(value = "/change-profile-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> changeProfilePassword(@Valid @RequestBody ChangeProfilePasswordAccountForm changeProfilePasswordAccountForm, BindingResult bindingResult) {
        Account account = accountRepository.findById(getCurrentUser()).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        if(!passwordEncoder.matches(changeProfilePasswordAccountForm.getOldPassword(), account.getPassword())){
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD, "Old password is incorrect");
        }
        if (changeProfilePasswordAccountForm.getNewPassword().equals(changeProfilePasswordAccountForm.getOldPassword())) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NEW_PASSWORD_INVALID, "New password must be different from old password");
        }
        account.setPassword(passwordEncoder.encode(changeProfilePasswordAccountForm.getNewPassword()));
        accountRepository.save(account);
        return makeSuccessResponse(null, "Change profile password success");
    }
}
