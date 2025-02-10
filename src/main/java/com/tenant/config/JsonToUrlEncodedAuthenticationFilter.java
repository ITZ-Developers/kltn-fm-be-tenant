package com.tenant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenant.dto.ApiMessageDto;
import com.tenant.exception.GlobalExceptionHandler;
import com.tenant.multitenancy.dto.DbConfigDto;
import com.tenant.multitenancy.feign.FeignDbConfigAuthService;
import com.tenant.multitenancy.tenant.TenantDBContext;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.Enumerator;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JsonToUrlEncodedAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private FeignDbConfigAuthService dbConfigAuthService;
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Token request content type: "+request.getContentType());
        if (Objects.equals(request.getServletPath(), "/api/token") && request.getContentType()!=null && request.getContentType().contains( "application/json")) {
            byte[] json = ByteStreams.toByteArray(request.getInputStream());
            Map<String, String> jsonMap = new ObjectMapper().readValue(json, Map.class);;
            Map<String, String[]> parameters =
                    jsonMap.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e ->  new String[]{e.getValue()})
                            );
            HttpServletRequest requestWrapper = new RequestWrapper(request, parameters);
            // Set the current tenant based on tenantId from the request before login
            String tenantId = requestWrapper.getParameter("tenant_id");
            if (StringUtils.isBlank(tenantId)) {
                globalExceptionHandler.exceptionResponse(response, "ERROR-INVALID-TENANT", "tenant_id cannot be null");
                return;
            }
            ApiMessageDto<DbConfigDto> tenant = dbConfigAuthService.getByTenantId(tenantId);
            if (tenant == null || !tenant.getResult() || tenant.getData() == null) {
                globalExceptionHandler.exceptionResponse(response, "ERROR-INVALID-TENANT", "No such tenant: " + tenantId);
                return;
            }
            TenantDBContext.setCurrentTenant(tenant.getData().getName());
            filterChain.doFilter(requestWrapper, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private class RequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> params;

        RequestWrapper(HttpServletRequest request, Map<String, String[]> params) {
            super(request);
            this.params = params;
        }

        @Override
        public String getParameter(String name) {
            if (this.params.containsKey(name)) {
                return this.params.get(name)[0];
            }
            return "";
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return this.params;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return new Enumerator<>(params.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            return params.get(name);
        }
    }
}
