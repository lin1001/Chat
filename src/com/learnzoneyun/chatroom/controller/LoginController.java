package com.learnzoneyun.chatroom.controller;

import com.learnzoneyun.chatroom.utils.*;
import com.learnzoneyun.chatroom.model.UserModel;
import com.learnzoneyun.chatroom.service.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * TODO   :  用户登录与注销
 */
@Controller
@RequestMapping(value = "/user")
public class LoginController {

    @Resource
    private IUserService userService;

    @Resource
    private ILogService logService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(String userid, String password, HttpSession session, RedirectAttributes attributes,
                        WordDefined defined, CommonDate date, LogUtil logUtil, NetUtil netUtil, HttpServletRequest request) {
        UserModel user = userService.selectUserByUserid(userid);
        if (user == null) {
            attributes.addFlashAttribute("error", defined.LOGIN_USERID_ERROR);
            return "redirect:/user/login";
        } else {
            if (!user.getPassword().equals(password)) {
                attributes.addFlashAttribute("error", defined.LOGIN_PASSWORD_ERROR);
                return "redirect:/user/login";
            } else {
                if (user.getStatus() != 1) {
                    attributes.addFlashAttribute("error", defined.LOGIN_USERID_DISABLED);
                    return "redirect:/user/login";
                } else {
                    logService.insert(logUtil.setLog(userid, date.getTime24(), defined.LOG_TYPE_LOGIN, defined.LOG_DETAIL_USER_LOGIN, netUtil.getIpAddress(request)));
                    session.setAttribute("userid", userid);
                    session.setAttribute("login_status", true);
                    user.setLasttime(date.getTime24());
                    userService.update(user);
                    attributes.addFlashAttribute("message", defined.LOGIN_SUCCESS);
                    return "redirect:/chat";
                }
            }
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register() {
        return "register";
    }
    @RequestMapping(value = "/logout")
    public String logout(HttpSession session, RedirectAttributes attributes, WordDefined defined) {
        session.removeAttribute("userid");
        session.removeAttribute("login_status");
        attributes.addFlashAttribute("message", defined.LOGOUT_SUCCESS);
        return "redirect:/user/login";
    }
    
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(String userid, String password, HttpSession session, RedirectAttributes attributes,
                        WordDefined defined, CommonDate date, LogUtil logUtil, NetUtil netUtil, HttpServletRequest request) {
        UserModel user = userService.selectUserByUserid(userid);
        if(user == null){
        	user = new UserModel();
        	user.setUserid(userid);
        	user.setPassword(password);
        	user.setFirsttime(date.getTime24());
        	user.setLasttime(date.getTime24());
        	user.setStatus(1);
        	userService.insert(user);
        	session.setAttribute("userid", userid);
            session.setAttribute("login_status", true);
        	logService.insert(logUtil.setLog(userid, date.getTime24(), defined.LOG_TYPE_REGISTER, defined.LOG_DETAIL_REGISTER, netUtil.getIpAddress(request)));
        	attributes.addFlashAttribute("message", defined.REGISTER_SUCCESS);
        	return "redirect:/chat";
        }else{
        	attributes.addFlashAttribute("error", defined.REGISTER_USERID_ERROR);
            return "redirect:/user/register";
        }
        
    }
}
