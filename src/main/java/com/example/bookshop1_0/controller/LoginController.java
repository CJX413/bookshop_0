package com.example.bookshop1_0.controller;


import com.example.bookshop1_0.filter.KickoutSessionControlFilter;
import com.example.bookshop1_0.service.userService;
import com.example.bookshop1_0.utils.EhCacheUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;


@Log4j2
@RestController
public class LoginController {
    @Autowired
    userService userService;
    @Autowired
    KickoutSessionControlFilter kickoutFilter;

    @RequestMapping("/loginPage")
    public String loginPage() {
        return "这是登陆页面";
    }

    @RequestMapping("/kickout")
    public String kickout() {
        return "你被T了";
    }

    @RequestMapping("/isLogin")
    public String isLogin() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return "已经登录";
        }
        return "还未登录";
    }

    @RequestMapping("/logout")
    public String logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "logout";
    }

    @RequestMapping("/loginSuccess")
    public String success() {
        Subject subject = SecurityUtils.getSubject();
        String username = (String) subject.getPrincipals().getPrimaryPrincipal();
        Session session = subject.getSession();
        Serializable sessionId = session.getId();
        //EhCacheManager ehCacheManager = new EhCacheUtil().getEhCacheManager();
        //Cache<String, Deque<Serializable>> cache = ehCacheManager.getCache("shiro-activeSessionCache");
        Cache<String, Deque<Serializable>> cache = kickoutFilter.getCache();
        Deque<Serializable> deque = cache.get(username);
        if (deque == null) {
            // 初始化队列
            deque = new ArrayDeque<Serializable>();
        }
        // 如果队列里没有此sessionId，且用户没有被踢出；放入队列
        if (!deque.contains(sessionId)
                && session.getAttribute("kickout") == null) {
            // 将sessionId存入队列
            deque.push(sessionId);
            // 将用户的sessionId队列缓存
            cache.put(username, deque);
            log.info("第一次登陆的存入缓存了");
        }
        CacheManager CacheManager = kickoutFilter.getCacheManager();
        Cache<Serializable, Session> sessionCache = CacheManager.getCache("sessionId-session");
        sessionCache.put(sessionId, session);
        return "Login Success";
    }


    @RequestMapping("/doLogin")
    public String doLogin(@RequestParam(value = "username", required = false) String username, @RequestParam(value = "password", required = false) String password) {
        Subject subject = SecurityUtils.getSubject();
        try {
            if (subject.isAuthenticated()) {
                return "已经登录";
            }
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            subject.login(token);
            Session session = subject.getSession();
            Serializable sessionId = session.getId();
            //EhCacheManager ehCacheManager = new EhCacheUtil().getEhCacheManager();
            //Cache<String, Deque<Serializable>> cache = ehCacheManager.getCache("shiro-activeSessionCache");
            Cache<String, Deque<Serializable>> cache = kickoutFilter.getCache();
            Deque<Serializable> deque = cache.get(username);
            if (deque == null) {
                // 初始化队列
                deque = new ArrayDeque<Serializable>();
            }
            // 如果队列里没有此sessionId，且用户没有被踢出；放入队列
            if (!deque.contains(sessionId)
                    && session.getAttribute("kickout") == null) {
                // 将sessionId存入队列
                deque.push(sessionId);
                // 将用户的sessionId队列缓存
                cache.put(username, deque);
                log.info("第一次登陆的存入缓存了");
            }
            CacheManager CacheManager = kickoutFilter.getCacheManager();
            Cache<Serializable, Session> sessionCache = CacheManager.getCache("sessionId-session");
            sessionCache.put(sessionId, session);
        } catch (IncorrectCredentialsException ice) {
            return "密码错误!";
        } catch (UnknownAccountException uae) {
            return "账号不存在!";
        }
        return "SUCCESS";
    }

    @RequestMapping("/notRole")
    public String unauthc() {
        return "404";
    }

    @RequestMapping("/admin/act")
    public String userAct() {
        return "adminAct";
    }
}
