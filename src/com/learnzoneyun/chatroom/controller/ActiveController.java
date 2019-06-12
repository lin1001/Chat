package com.learnzoneyun.chatroom.controller;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.learnzoneyun.chatroom.serviceImpl.ProducerServiceImpl;
import com.learnzoneyun.chatroom.utils.Base64Util;
import com.learnzoneyun.chatroom.utils.CommonDate;
import com.learnzoneyun.chatroom.websocket.ChatServer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping(value = "/active")
public class ActiveController {
	ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-activeMQ.xml");
	private Destination demoQueueDestination = ctx.getBean("demoQueueDestination",Destination.class);
	private static ApplicationContext jctx=new ClassPathXmlApplicationContext("spring-jedis.xml");
	private static JedisPool jedisPool = jctx.getBean("jedisPool",JedisPool.class);
	@Resource(name = "producerService")
	private ProducerServiceImpl producer;


	@RequestMapping(value = "/team", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setTeam(HttpSession session,String amount,String team_type,String time,String roomId) {
		String userid = session.getAttribute("userid").toString();
		roomId = Base64Util.decode(roomId);
		System.out.println("userid =  "+userid+" amount ="+amount+" team_type = "+team_type+" time = "+time+" roomId = "+roomId);

		String teamId = roomId +"_"+ userid;
		System.out.println("teamId = "+teamId);
		setTeamRedis(teamId,amount,userid);
		String content = "发起了"+(team_type.equals("1")?"抢占":"随机")+"组队";
		System.out.println("content:"+content);
		ChatServer.team_start(teamId, time,team_type, userid,amount,content,roomId);
		//        producer.sendMessage(demoQueueDestination,getMessage(userid,1,teamId));
		//                System.out.println("---------成功发送消息----消息序号："+1); 


		Integer cacheTime = 1000;
		//延迟时间，时间单位为毫秒,读者可自行设定，不得小于等于0
		Integer delay = 1000;

		Timer timer = new Timer(teamId+"-"+time+"-"+team_type+"-"+userid+"-"+amount+"-"+content+"-"+roomId+"-"+CommonDate.getStamp());
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				String[] s = Thread.currentThread().getName().split("-");
				Jedis jedis = jedisPool.getResource();
//				Integer amount = Integer.valueOf(jedis.get(s[0]));
//				System.out.println(s[0]+" amount ="+ amount+" "+jedis.get(s[0]));
				jedis.close();
				
				//判断该组队是否存在
				if(jedis.exists(s[0])){
//					System.out.println("run");
					System.out.println("1");
//					System.out.println("roomId = "+s[6]);
					Long time = CommonDate.getStamp()-Long.parseLong(s[7]);
					
					
//					System.out.println(time);
					if(time>Long.parseLong(s[1])){
						System.out.println(" 2");
						jedis.del(s[0]);
						ChatServer.team_end(s[0], s[2], s[3], s[4], s[6]);
						cancel();
					}else{
						ChatServer.team_start(s[0], s[1],s[2], s[3],s[4]+"    		"+(Long.parseLong(s[1])-time)+"s",s[5],s[6]);
					}
				}else{
					System.out.println("3");
					ChatServer.team_end(s[0], s[2], s[3], s[4], s[6]);
					cancel();
				}
				
			}

		}, delay, cacheTime);

		System.out.println("+++");
		JSONObject message = new JSONObject();
		message.put("message", "success");
		return message;
	}

	@RequestMapping(value = "/join", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setJoin(HttpSession session,String teamId,String team_type) {
		String userId = session.getAttribute("userid").toString();
		//		teamId = Base64Util.decode(teamId);
		System.out.println("userId =  "+userId+"  teamId = "+teamId);
		producer.sendMessage(demoQueueDestination,getMessage(userId,team_type,teamId));
		//                System.out.println("---------成功发送消息----消息序号："+1); 
		JSONObject message = new JSONObject();
		message.put("message", "success");
		return message;
	}

	public void setTeamRedis(String teamId,String number,String userid){
		Jedis jedis = jedisPool.getResource();
		jedis.set(teamId, number);
		jedis.sadd("set_"+teamId, userid);
		jedis.close();
	}

	public static String getMessage(String userId, String type, String teamId){
		JSONObject member = new JSONObject();
		member.put("userId", userId);
		member.put("type", type);
		member.put("teamId", teamId);
		return member.toString();
	}

}
