package com.learnzoneyun.chatroom.junittest;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.learnzoneyun.chatroom.dao.ILogDao;
import com.learnzoneyun.chatroom.model.LogModel;

public class LogTest {
	@Test
	public void query(){
	ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-mybatis.xml");
	ILogDao logDao = ctx.getBean("logDao",ILogDao.class);
//	List<LogModel> list = logDao.selectLogByUserid("admin", 1, 10);
	logDao.selectAll(1, 10);
//	logDao.selectCountByUserid("admin");
//	System.out.println(list);
	}
	@Test
	public void time_count()throws Exception{
//		Long l = 222l;
//		Integer i = 200;
		String uuid = 'U'+UUID.randomUUID().toString().replaceAll("-", "");
		if(uuid.charAt(0)=='U'){
			System.out.println(uuid.charAt(0)+"  "+uuid+" length = "+uuid.length());
		}
		
	}
}
