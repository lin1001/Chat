package com.learnzoneyun.chatroom.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.learnzoneyun.chatroom.utils.Base64Util;
/**
 * 聊天室控制器
 * @author lin1
 *
 */
@Controller
@RequestMapping(value = "/room")
public class RoomController {
	private static int talkId = 0;
    @RequestMapping(value = "/list")
    @ResponseBody
	public List<String> roomList(){
		List<String> list = new ArrayList<>();
		list.add("League of Legends");
		list.add("Shadowverse");
		return list;
	}
    
    @RequestMapping(value="/get")
    public String getRoom(String roomId,HttpServletRequest request){
//    	model.addFlashAttribute("roomId", roomId);
//    	request.getSession().setAttribute("roomId", roomId);
    	int i =0;
    	System.out.println(i++);
    	return "redirect:/room/"+Base64Util.encode(roomId);
    }
    @RequestMapping(value="/quit")
    public String quitRoom(HttpServletRequest request){
    	request.getSession().setAttribute("roomId", null);
    	return "redirect:/chat";
    }
    @RequestMapping(value="/talk")
    public String talkRoom(String talkId,HttpServletRequest request){
    	String userId = (String)request.getSession().getAttribute("userid");
    	System.out.println("rc="+talkId);
    	String roomId = userId.compareTo(talkId)>0?(userId+"_"+talkId):(talkId+"_"+userId);
//    	model.addFlashAttribute("roomId", roomId);
//    	request.getSession().setAttribute("roomId", roomId);
    	return "redirect:/talk/"+Base64Util.encode(roomId);
    }
    
}
