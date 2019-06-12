package com.learnzoneyun.chatroom.utils;



import java.io.UnsupportedEncodingException;
import java.util.Base64;


public class Base64Util {

	
	//编码
	public static String encode(String str){
		
		try{
			str = new String(Base64.getEncoder().encodeToString(str.getBytes("utf-8")));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return str;
	}

	//解码
	public static String decode(String str){
		try{
			str = new String(Base64.getDecoder().decode(str), "utf-8");
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return str;
	}

}
