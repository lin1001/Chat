package com.learnzoneyun.chatroom.serviceImpl;

import javax.jms.*;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service(value = "MessageListen")
public class MessageListenImpl implements MessageListener {
	private static ApplicationContext jctx=new ClassPathXmlApplicationContext("spring-jedis.xml");
	private static JedisPool jedisPool = jctx.getBean("jedisPool",JedisPool.class);

	@Override
	public void onMessage(Message message) {
		System.out.println("开始监听============");
		TextMessage tm = (TextMessage) message;
		try {
			JSONObject ms = JSON.parseObject(tm.getText());
			System.out.println("监听："+ms);
			Jedis jedis = jedisPool.getResource();
			//判断该组队是否存在
			if(jedis.exists(ms.get("teamId").toString())){
				System.out.println("1");
				Integer amount = Integer.valueOf(jedis.get(ms.get("teamId").toString()));
				if(ms.get("type").toString().equals("1")){
					System.out.println(" 2");
					//判断用户是否存在组队列表
					System.out.println(" 2"+jedis.sismember("set_"+ms.get("teamId").toString(),ms.get("userId").toString()));
					if(!jedis.sismember("set_"+ms.get("teamId").toString(),ms.get("userId").toString())){
						System.out.println("  3");
						Long number = jedis.scard("set_"+ms.get("teamId").toString());
						//判断组队列表人数是否低于组队上限
						if(number<amount){
							System.out.println("   4");
							setTeamNumberRedis(ms.get("teamId").toString(),ms.get("userId").toString(),jedis);
							if(number.equals(amount.longValue()-1)){
								System.out.println("    5");
								jedis.del(ms.get("teamId").toString());
							}
						}else if(number.equals(amount.longValue())){
							System.out.println("满人");
							jedis.del(ms.get("teamId").toString());
						}
					}
				}else if(ms.get("type").toString().equals("2")){
					System.out.println(" 6");
					setTeamNumberRedis(ms.get("teamId").toString(),ms.get("userId").toString(),jedis);
				}
			}
			jedis.close();

		} catch (JMSException e) {
			e.printStackTrace();
		}

	}
	public void setTeamNumberRedis(String teamId,String userid,Jedis jedis){
		jedis.sadd("set_"+teamId, userid);
	}
}
