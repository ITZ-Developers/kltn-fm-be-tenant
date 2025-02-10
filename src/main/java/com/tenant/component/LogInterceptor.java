package com.tenant.component;

import com.tenant.constant.FinanceConstant;
import com.tenant.exception.ErrorNotReadyException;
import com.tenant.jwt.FinanceJwt;
import com.tenant.multitenancy.tenant.TenantDBContext;
import com.tenant.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    @Qualifier("applicationConfig")
    ConcurrentMap<String, String> concurrentMap;

    private static final List<String> ALLOWED_URLS = Arrays.asList(
            "/v1/account/**", "/v1/group/**" , "/v1/permission/**"
    );
    private static final List<String> NOT_ALLOWED_URLS = Arrays.asList(
            "/v1/account/request-key", "/v1/account/my-key"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name())
                && request.getMethod().equals(HttpMethod.GET.name())) {
        }
        FinanceJwt financeJwt = userService.getAddInfoFromToken();
        if (financeJwt != null){
            TenantDBContext.setCurrentTenant(financeJwt.getTenantId());
            if (StringUtils.isBlank(concurrentMap.get(FinanceConstant.PRIVATE_KEY))) {
                if (!financeJwt.getIsSuperAdmin() || !isUrlAllowed(request.getRequestURI())){
                    throw new ErrorNotReadyException("Not ready");
                }
            }
        }
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        log.debug("Starting call url: [" + getUrl(request) + "]");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;
        log.debug("Complete [" + getUrl(request) + "] executeTime : " + executeTime + "ms");
        if (ex != null) {
            log.error("afterCompletion>> " + ex.getMessage());

        }
    }

    /**
     * get full url request
     * @param req
     * @return
     */
    private static String getUrl(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();   // d=789
        if (!StringUtils.isEmpty(queryString)) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }

    private boolean isUrlAllowed(String url) {
        if (NOT_ALLOWED_URLS.contains(url)) {
            return false;
        }
        return ALLOWED_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, url));
    }
}
