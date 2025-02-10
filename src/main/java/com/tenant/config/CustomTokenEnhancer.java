package com.tenant.config;

import com.tenant.multitenancy.tenant.TenantDBContext;
import com.tenant.utils.ZipUtils;
import com.tenant.dto.AccountForTokenDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomTokenEnhancer implements TokenEnhancer {
    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper;

    public CustomTokenEnhancer(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo = new HashMap<>();
        String tenantId = TenantDBContext.getCurrentTenant();
        String username = authentication.getName();
        if(authentication.getOAuth2Request().getGrantType() != null){
            additionalInfo = getAdditionalInfo(tenantId, username, authentication.getOAuth2Request().getGrantType(), null);
        }else {
            String grantType = authentication.getOAuth2Request().getRequestParameters().get("grantType");
            if(grantType.equals(SecurityConstant.GRANT_TYPE_KITPLAY)){
                Long userId = Long.parseLong(authentication.getOAuth2Request().getRequestParameters().get("userId"));
                additionalInfo = getAdditionalInfoCustom(null, username, grantType, userId);
            }
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    private Map<String, Object> getAdditionalInfoCustom(String tenantName, String username, String grantType, Long userId) {
        Map<String, Object> additionalInfo = new HashMap<>();
        AccountForTokenDto a = getAccountByUsername(username);

        if (a != null) {
            Long accountId = userId;
            Long storeId = -1L;
            String kind = a.getKind() + "";//token kind
            Long deviceId = -1L;// id cua thiet bi, lưu ở table device để get firebase url..
            String pemission = "<>";//empty string
            Integer userKind = a.getKind(); //loại user là admin hay là gì
            Integer tabletKind = -1;
            Long orderId = -1L;
            Boolean isSuperAdmin = a.getIsSuperAdmin();
            additionalInfo.put("user_id", accountId);
            additionalInfo.put("user_kind", a.getKind());
            additionalInfo.put("grant_type", grantType);
            String DELIM = "|";
            String additionalInfoStr = ZipUtils.zipString(accountId + DELIM
                    + storeId + DELIM
                    + kind + DELIM
                    + pemission + DELIM
                    + deviceId + DELIM
                    + userKind + DELIM
                    + username + DELIM
                    + tabletKind + DELIM
                    + orderId + DELIM
                    + isSuperAdmin);
            additionalInfo.put("additional_info", additionalInfoStr);
        }
        return additionalInfo;
    }

    private Map<String, Object> getAdditionalInfo(String tenantName, String username, String grantType, Long userId) {
        Map<String, Object> additionalInfo = new HashMap<>();
        AccountForTokenDto a = getAccountByUsername(username);

        if (a != null) {
            Long accountId = a.getId();
            Long storeId = -1L;
            String kind = a.getKind() + "";//token kind
            Long deviceId = -1L;// id cua thiet bi, lưu ở table device để get firebase url..
            String listPermission = "<>";//empty string
            Integer userKind = a.getKind(); //loại user là admin hay là gì
            Integer tabletKind = -1;
            Long orderId = -1L;
            Boolean isSuperAdmin = a.getIsSuperAdmin();
            additionalInfo.put("user_id", accountId);
            additionalInfo.put("user_kind", a.getKind());
            additionalInfo.put("grant_type", grantType);
            additionalInfo.put("tenant_info", tenantName);
            String DELIM = "|";
            String additionalInfoStr = ZipUtils.zipString(accountId + DELIM
                    + storeId + DELIM
                    + kind + DELIM
                    + listPermission + DELIM
                    + deviceId + DELIM
                    + userKind + DELIM
                    + username + DELIM
                    + tabletKind + DELIM
                    + orderId + DELIM
                    + isSuperAdmin + DELIM
                    + tenantName
            );
            additionalInfo.put("additional_info", additionalInfoStr);
        }
        return additionalInfo;
    }

    public AccountForTokenDto getAccountByUsername(String username) {
        try {
            String query = "SELECT id, kind, username, email, full_name, is_super_admin " +
                    "FROM db_fn_account WHERE username = ? and status = 1 limit 1";
            log.debug(query);
            List<AccountForTokenDto> dto = jdbcTemplate.query(query, new Object[]{username},  new BeanPropertyRowMapper<>(AccountForTokenDto.class));
            if (dto.size() > 0)return dto.get(0);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
