package com.tenant.controller;

import com.tenant.constant.FinanceConstant;
import com.tenant.dto.ApiMessageDto;
import com.tenant.dto.ErrorCode;
import com.tenant.form.notification.CreateNotificationForm;
import com.tenant.form.paymentPeriod.CreatePaymentPeriodForm;
import com.tenant.mapper.NotificationMapper;
import com.tenant.mapper.PaymentPeriodMapper;
import com.tenant.storage.tenant.model.*;
import com.tenant.storage.tenant.model.criteria.*;
import com.tenant.storage.tenant.repository.*;
import com.tenant.service.KeyService;
import com.tenant.service.TransactionService;
import com.tenant.utils.AESUtils;
import com.tenant.utils.GenerateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/reset-data")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ResetDataController extends ABasicController{
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private KeyInformationRepository keyInformationRepository;
    @Autowired
    private KeyInformationGroupRepository keyInformationGroupRepository;
    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionGroupRepository transactionGroupRepository;
    @Autowired
    private PaymentPeriodRepository paymentPeriodRepository;
    @Autowired
    private UserGroupNotificationRepository userGroupNotificationRepository;
    @Autowired
    private NotificationGroupRepository notificationGroupRepository;
    @Autowired
    private ServiceScheduleRepository serviceScheduleRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceGroupRepository serviceGroupRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private KeyService keyService;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private PaymentPeriodMapper paymentPeriodMapper;
    @Autowired
    private TransactionService transactionService;

    @ApiIgnore
    @GetMapping(value = "/database", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> resetDatabase() {
        if(!isSuperAdmin()){
            return makeErrorResponse(null, "Not allowed reset database");
        }
        notificationRepository.deleteAll();
        keyInformationRepository.deleteAll();
        keyInformationGroupRepository.deleteAll();
        transactionHistoryRepository.deleteAll();
        transactionRepository.deleteAll();
        transactionGroupRepository.deleteAll();
        paymentPeriodRepository.deleteAll();
        userGroupNotificationRepository.deleteAll();
        notificationGroupRepository.deleteAll();
        serviceScheduleRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceGroupRepository.deleteAll();
        categoryRepository.deleteAll();
        accountRepository.updateAllAccountByDepartmentToNull();
        departmentRepository.deleteAll();
        return makeSuccessResponse(null, "Reset database success");
    }

    @ApiIgnore
    @GetMapping(value = "/super-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> resetSuperAdmin(@Param("id") Long id, @Param("isSuperAdmin") Boolean isSuperAdmin) {
        if(!isSuperAdmin()){
            return makeErrorResponse(null, "Not allowed reset super admin");
        }
        accountRepository.updateAccountByIdAndIsSuperAdmin(id, isSuperAdmin);
        return makeSuccessResponse(null, "Reset super admin success");
    }

    @ApiIgnore
    @GetMapping(value = "/generate-key", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> generateKeyAll() {
        if(!isSuperAdmin()){
            return makeErrorResponse(null, "Not allowed generate key");
        }
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts){
            account.setSecretKey(getUniqueSecretKey());
            accountRepository.save(account);
        }
        return makeSuccessResponse(null, "Generate key all success");
    }

    @ApiIgnore
    @GetMapping(value = "/generate-key/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> generateKey(@PathVariable("id") Long id) {
        if(!isSuperAdmin()){
            return makeErrorResponse(null, "Not allowed generate key");
        }
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        account.setSecretKey(getUniqueSecretKey());
        accountRepository.save(account);
        return makeSuccessResponse(null, "Generate key success");
    }

    private String getUniqueSecretKey(){
        String encryptSecretKey;
        Account existAccount;
        String secretKey = keyService.getKeyInformationSecretKey();
        do {
            encryptSecretKey = AESUtils.encrypt(secretKey, GenerateUtils.generateRandomString(16), FinanceConstant.AES_ZIP_ENABLE) ;
            existAccount = accountRepository.findFirstBySecretKey(encryptSecretKey).orElse(null);
        } while (existAccount != null);
        return encryptSecretKey;
    }

    @ApiIgnore
    @GetMapping(value = "/permission-is-system", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> permissionIsSystem(@RequestParam(value = "isSystem", required = false) Boolean isSystem) {
        if(!isSuperAdmin()){
            return makeErrorResponse(null, "Not allowed generate key");
        }
        permissionRepository.updateAllByIsSystem(isSystem);
        return makeSuccessResponse(null, "Permission is system key success");
    }

    @ApiIgnore
    @GetMapping(value = "/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> resetTransaction() {
        if(!isSuperAdmin()){
            return makeErrorResponse(null, "Not allowed reset transaction");
        }
        transactionHistoryRepository.deleteAll();
        transactionRepository.deleteAll();
        return makeSuccessResponse(null, "Reset transaction success");
    }

    @ApiIgnore
    @GetMapping(value = "/key-information", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> resetKeyInformation() {
        if(!isSuperAdmin()){
            return makeErrorResponse(null, "Not allowed reset key information");
        }
        keyInformationRepository.deleteAll();
        return makeSuccessResponse(null, "Reset key information success");
    }

    @ApiIgnore
    @PostMapping(value = "/notification-create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> createNotification(@Valid @RequestBody CreateNotificationForm createNotificationForm, BindingResult bindingResult) {
        Notification notification = notificationMapper.fromCreateNotificationFormToEntity(createNotificationForm);
        notificationRepository.save(notification);
        return makeSuccessResponse(null, "Create notification success");
    }

    @ApiIgnore
    @PostMapping(value = "/payment-period-create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> createPaymentPeriod(@Valid @RequestBody CreatePaymentPeriodForm createPaymentPeriodForm, BindingResult bindingResult) {
        if (createPaymentPeriodForm.getStartDate().after(createPaymentPeriodForm.getEndDate())) {
            return makeErrorResponse(ErrorCode.PAYMENT_PERIOD_ERROR_DATE_INVALID, "Start date must be before end date");
        }
        PaymentPeriod paymentPeriod = paymentPeriodMapper.fromCreatePaymentPeriodFormToEncryptEntity(createPaymentPeriodForm, keyService.getFinanceSecretKey());
        paymentPeriod.setState(FinanceConstant.PAYMENT_PERIOD_STATE_CREATED);
        paymentPeriodRepository.save(paymentPeriod);
        transactionRepository.updateAllByStateAndCreatedDate(paymentPeriod.getId(), FinanceConstant.TRANSACTION_STATE_APPROVE, paymentPeriod.getStartDate(), paymentPeriod.getEndDate());
        transactionService.recalculatePaymentPeriod(paymentPeriod.getId());
        return makeSuccessResponse(null, "Create payment period success");
    }
}
