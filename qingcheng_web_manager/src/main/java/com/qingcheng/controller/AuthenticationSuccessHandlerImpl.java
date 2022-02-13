package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference
    LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        String name = authentication.getName(); //当前用户
        String ip = httpServletRequest.getLocalAddr(); //用户ip地址
        String agent = httpServletRequest.getHeader("user-agent");

        LoginLog loginLog = new LoginLog();
        loginLog.setLoginName(name);
        loginLog.setIp(ip);
        loginLog.setLocation(WebUtil.getCityByIP(ip)); //当前城市
        loginLog.setLoginTime(new Date());
        loginLog.setBrowserName(WebUtil.getBrowserName(agent));

        loginLogService.add(loginLog);

//        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest,httpServletResponse);
        httpServletResponse.sendRedirect("/main.html");
    }
}
