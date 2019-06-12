package com.learnzoneyun.chatroom.junittest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.jms.Destination;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Base64Utils;

import com.learnzoneyun.chatroom.dao.IUserDao;
import com.learnzoneyun.chatroom.model.UserModel;
import com.learnzoneyun.chatroom.serviceImpl.MessageListenImpl;
import com.learnzoneyun.chatroom.serviceImpl.ProducerServiceImpl;
import com.learnzoneyun.chatroom.utils.Base64Util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
public class UserTest {
	ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-mybatis.xml");
	private IUserDao userDao = ctx.getBean("userDao",IUserDao.class);
	private int count=0;
//	private UserModel userModel = ctx.getBean("user",UserModel.class);
	@Test
	public void query() throws Exception{
		userDao.selectAll(1, 10);
		UserModel userModel = userDao.selectUserByUserid("admin");
		System.out.println(userModel);
//		System.out.println(userModel);
	}
	@Test
	public void base()throws Exception{
		String str ;
		str = Base64Util.encode("123");
		System.out.println(str);
		str = Base64Util.decode(str);
		System.out.println(str);
//		System.out.println(Base64Util.encode("asda"));
//		System.out.println(Base64Utils.encodeToString("asda".getBytes("utf-8")));
//		System.out.println(new String(Base64Utils.decodeFromString(Base64Utils.encodeToString("asda".getBytes("utf-8")))));
//		System.out.println(Base64Util.encode(Base64Util.decode("a1234")));
//		String base64encodedString = Base64.getEncoder().encodeToString("123".getBytes("utf-8"));
//        System.out.println("Base64 编码字符串 (基本) :" + base64encodedString);
	}
	@Test
	public void jedis()throws Exception{
//		System.out.println(new Jedis("134.175.6.213",6379).ping()); 
//		Jedis jedis = new Jedis("134.175.6.213",6379);
//		jedis.set("hello", "hello world");
//		String str = jedis.get("hello");
//		System.out.println(str);
		ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-jedis.xml");
		JedisPool jedisPool = ctx.getBean("jedisPool",JedisPool.class);
		
		Jedis jedis = jedisPool.getResource();
//		jedis.set("hello", "hello world");
//		List<String> list =jedis.mget("he*");
		
//		Long count = jedis.zcard("测试室");
//		System.out.println("数量 = "+count);
//		Set<String> s = jedis.zrange("测试室", count-3, -1);
//		for(String str:s){
//			System.out.println("str = "+str);
//		}
//		String sets = "stes";
//		jedis.set(sets, "11");
//		jedis.sadd("set_"+sets, "element001");
//		jedis.sadd("set_"+sets, "element001");
//		jedis.sadd("set_"+sets, "element001");
//        System.out.println("查看sets集合中的所有元素:"+jedis.smembers("set_"+sets)); 
//        System.out.println();
//		jedis.sadd("set_英雄联盟_admin", "element001");
		System.out.println(jedis.exists("英雄联盟_1"));
//		Integer amount = Integer.valueOf(jedis.get("asdw"));
//        System.out.println("amount = "+amount);
//		String str = jedis.get("hello");
//		System.out.println(str);
//		System.out.println("list[0]  = "+list.get(0));
	}
	@Test
	public void jedis_add()throws Exception{
		ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-jedis.xml");
		JedisPool jedisPool = ctx.getBean("jedisPool",JedisPool.class);
		Jedis jedis = jedisPool.getResource();
//		jedis.lpush("测试室", "第一条");
//		jedis.lpush("测试室", "第二条");
//		jedis.lpush("测试室", "第三条");
//		jedis.lpush("测试室", "第四条");
//		jedis.lpush("测试室", "第五条");
//		jedis.lpush("测试室", "第六条");
		
		jedis.zadd("测试室",new Date().getTime(),"第一条");
		jedis.zadd("测试室",new Date().getTime(),"第二条");
		jedis.zadd("测试室",new Date().getTime(),"第三条");
		jedis.zadd("测试室",new Date().getTime(),"第四条");
		jedis.zadd("测试室",new Date().getTime(),"第五条");
	}
	@Test
	public void judge()throws Exception{
		String str = "1";
		String str2 = "3";
		System.out.println(str.compareTo(str2)>0?str:str2);
//		List<String> list = new ArrayList<String>();
//		list.add(str);
//		list.add(str2);
//		Arrays.sort
	}
	
	@Test
	public void activeMQ_test()throws Exception{
//		MessageListenImpl messagelisten = new MessageListenImpl();
		ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-activeMQ.xml");
		Destination demoQueueDestination = ctx.getBean("demoQueueDestination",Destination.class);
		ProducerServiceImpl producer = new ProducerServiceImpl();
		
		producer.sendMessage(demoQueueDestination, "消息序号：123");

	}
	@Test
	public void time_count()throws Exception{
		Integer cacheTime = 2;
		//延迟时间，时间单位为毫秒,读者可自行设定，不得小于等于0
		Integer delay = 1;
		Timer timer = new Timer();
		    timer.schedule(new TimerTask() {
		    @Override
		    public void run() {
		    	System.out.println("count");
		                        }
		    }, 0, 1);
	}
}
