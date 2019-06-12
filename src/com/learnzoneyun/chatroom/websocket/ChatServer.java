package com.learnzoneyun.chatroom.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.learnzoneyun.chatroom.utils.Base64Util;
import com.learnzoneyun.chatroom.utils.CommonDate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * websocket服务
 */
@ServerEndpoint(value = "/chatServer/{roomId}", configurator = HttpSessionConfigurator.class)
public class ChatServer {
	private static final Map<String, CopyOnWriteArraySet<ChatServer>> rooms = new HashMap<>();
	//用户连接数,使用原子类，使之线程安全
	private static final Map<String, AtomicInteger> connections = new HashMap<>();
	//在线数量
	private static final Map<String, AtomicInteger> counts = new HashMap<>();
	private static final Map<String, List> lists = new HashMap<>();
	//    private static CopyOnWriteArraySet<ChatServer> webSocketSet = new CopyOnWriteArraySet<ChatServer>();
	private Session session;    //与某个客户端的连接会话，需要通过它来给客户端发送数据
	private String userid;      //用户名
	private String roomId;
	private HttpSession httpSession;    //request的session
	private List list = new ArrayList<>();   //在线列表,记录用户名称
	//    private static Map routetab = new HashMap<>();  //用户名和websocket的session绑定的路由表
	private int kind;

	private static ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-jedis.xml");
	private static JedisPool jedisPool = ctx.getBean("jedisPool",JedisPool.class);
	/**
	 * 连接建立成功调用的方法
	 * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	@OnOpen
	public void onOpen(@PathParam(value="roomId") String id,Session session, EndpointConfig config){
		this.session = session;
		this.roomId = id.substring(0,4).equals("UUID")?id:Base64Util.decode(id);
//		this.roomId = Base64Util.decode(id);
		this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		this.userid=(String) httpSession.getAttribute("userid");
		System.out.println("roomId = "+roomId+" id= "+id);
		//三种房间
		if(!this.roomId.contains("_")&&!id.substring(0,4).equals("UUID")){
			kind=0;
		}else if(this.roomId.split("_").length==2){
			kind=1;
		}else{
			kind=2;
		}
		System.out.println("kind = "+kind);
		CopyOnWriteArraySet<ChatServer> room = rooms.get(roomId);
		if (room == null) {
			synchronized (rooms) {
				if (!rooms.containsKey(roomId)) {
					room = new CopyOnWriteArraySet<>();
					rooms.put(roomId, room);
					counts.put(roomId,new AtomicInteger(0));
					lists.put(roomId, list);
				}
			}
			if(kind==1){
				String [] strs=this.roomId.split("_");
				for (int i=0;i<strs.length;i++){
					list.add(strs[i]);
					counts.get(roomId).incrementAndGet();
				}
				lists.put(roomId, list);
			}else if(kind==2){
				Jedis jedis = jedisPool.getResource();
				Set<String> sets = jedis.smembers("t_"+roomId);
				jedis.close();
				for(String str:sets){
					list.add(str);
					counts.get(roomId).incrementAndGet();
				}
				lists.put(roomId, list);
			}
		}
		list = lists.get(roomId);
		synchronized(room){
			AtomicInteger cid =connections.get(this.roomId+this.userid);

			//判断是否为多开窗口
			if(cid!=null){
				cid.incrementAndGet();
				System.out.println("cid = "+cid);
				room.add(this);
				connections.put(this.roomId+this.userid, cid);
				String message = getMessage("", "",  list);
				broadcast(message); 
			}else{

				//判断是否为临时房间
				//				if(this.roomId.contains("_")){
				//					String [] strs=this.roomId.split("_");
				//					for (int i=1;i<this.roomId.split("_").length;i++){
				//						list.add(strs[i]);
				//					}
				//				}
				room.add(this);
				switch(kind){
				case 0:
					list.add(userid);
					lists.put(roomId, list);
					counts.get(roomId).incrementAndGet();
					//广播
					String message = getMessage("[" + userid + "]加入聊天室,当前在线人数为"+getOnlineCount()+"位", "notice",  list);
					broadcast(message); 
					connections.put(this.roomId+this.userid, new AtomicInteger(1));
					break;
				case 1:
					broadcast(getMessage("", "",  list)); 
					connections.put(this.roomId+this.userid, new AtomicInteger(1));
					break;
				case 2:
					broadcast(getMessage("", "",  list)); 
					connections.put(this.roomId+this.userid, new AtomicInteger(1));
					break;
				}

			}
		}
			Set<String> set =getRedis();
			String message_set = getMessage_Set(set);
			System.out.println(message_set);
			session.getAsyncRemote().sendText(message_set);
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(){
		CopyOnWriteArraySet<ChatServer> room = rooms.get(this.roomId);
		synchronized(room){
			AtomicInteger cid =connections.get(this.roomId+this.userid);
			if(cid!=null){
				if(cid.intValue()==1){
					//    	    	CopyOnWriteArraySet<ChatServer> room = rooms.get(roomId);
					list = lists.get(roomId);
					room.remove(this);  //从set中删除
					if(kind==0){


						subOnlineCount();           //在线数减1
						list.remove(userid);        //从在线列表移除这个用户

						//    	        routetab.remove(userid);
						String message = getMessage("[" + userid +"]离开了聊天室,当前在线人数为"+getOnlineCount()+"位", "notice", list);
						broadcast(message);         //广播
					}
					connections.remove(this.roomId+this.userid);
				}else{
					room.remove(this);  //从set中删除
					cid.decrementAndGet();
					connections.put(this.roomId+this.userid, cid);
				}
			}
		}
	}

	/**
	 * 接收客户端的message,判断是否有接收人而选择进行广播还是指定发送
	 * @param _message 客户端发送过来的消息
	 */
	@OnMessage
	public void onMessage(String _message) {
		JSONObject mes = JSON.parseObject(_message);
		JSONObject message = JSON.parseObject(mes.get("message").toString());
		System.out.println("time = "+message.get("time"));
		System.out.println("时间戳 = "+CommonDate.time2stamp(message.get("time").toString()));
		if(mes.get("type").toString().equals("message")){
			setRedis(_message);
		}
		//        if(message.get("to") == null || message.get("to").equals("")){      //如果to为空,则广播;如果不为空,则对指定的用户发送消息
		broadcast(_message);
		//        }else{
		//            String [] userlist = message.get("to").toString().split(",");
		//            singleSend(_message, (Session) routetab.get(message.get("from")));      //发送给自己,这个别忘了
		//            for(String user : userlist){
		//                if(!user.equals(message.get("from"))){
		//                    singleSend(_message, (Session) routetab.get(user));     //分别发送给每个指定用户
		//                }
		//            }
		//        }
		if(kind==1){
			String str[] = roomId.split("_");
			String talkId = str[0].equals(userid)?str[1]:str[0];
			int count = 0;
			System.out.println("str = "+str[0]+"   "+str[1]);
			System.out.println("talkId = "+talkId);
			for(ChatServer chat: rooms.get(roomId) ){
				//判断是否在房间内
				if(chat.userid.equals(talkId)){
					count++;
				}
			}
			//发送提示消息
			if(count==0){
				for(Entry<String, CopyOnWriteArraySet<ChatServer>> entry : rooms.entrySet()){

					for(ChatServer chat: entry.getValue() ){
						if(chat.userid.equals(talkId)){
							String getmessage = getMessage_News(message.toString(), "news", Base64Util.encode(roomId));
							//				    		   System.out.println(getmessage);
							chat.session.getAsyncRemote().sendText(getmessage);
						}
					}
				}
			}
		}
	}

	/**
	 * 发生错误时调用
	 * @param error
	 */
	@OnError
	public void onError(Throwable error){
		error.printStackTrace();
	}

	/**
	 * 广播消息
	 * @param message
	 */
	public void broadcast(String message){

		for(ChatServer chat: rooms.get(roomId) ){
			chat.session.getAsyncRemote().sendText(message);
		}

	}

	/**
	 * 对特定用户发送消息
	 * @param message
	 * @param session
	 */
	public void singleSend(String message, Session session){
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 组装返回给前台的消息
	 * @param message   交互信息
	 * @param type      信息类型
	 * @param list      在线列表
	 * @return
	 */
	public String getMessage(String message, String type, List list){
		JSONObject member = new JSONObject();
		member.put("message", message);
		member.put("type", type);
		member.put("list", list);
		return member.toString();
	}
	public static String getMessage_News(String message, String type, String roomId){
		JSONObject member = new JSONObject();
		member.put("message", message);
		member.put("type", type);
		member.put("roomId", roomId);
		return member.toString();
	}
	public static String getMessage_Team(String message, String type, String teamId){
		JSONObject member = new JSONObject();
		member.put("message", message);
		member.put("type", type);
		member.put("teamId", teamId);
		return member.toString();
	}
	public static String getMessage_Message(String from, String content, String time,String team_type,String amount){
		JSONObject member = new JSONObject();
		member.put("from", from);
		member.put("content", content);
		member.put("time", time);
		member.put("team_type", team_type);
		member.put("amount", amount);
		return member.toString();
	}
	public String getMessage_Set(Set<String> set){
		JSONObject member = new JSONObject();
		member.put("set", set);
		return member.toString();
	}
	public static String getMessage_Talk(String content, String type, String url){
		JSONObject member = new JSONObject();
		member.put("content", content);
		member.put("type", type);
		member.put("url", url);
		JSONObject me = new JSONObject();
		me.put("message", member.toString());
		me.put("type", "talk");
		return me.toString();
	}
	public void setRedis(String _message){
		JSONObject mes = JSON.parseObject(_message);
		JSONObject message = JSON.parseObject(mes.get("message").toString());
		Jedis jedis = jedisPool.getResource();
		jedis.zadd(roomId, CommonDate.time2stamp(message.get("time").toString()), _message);
		jedis.close();
	}
	public Set<String> getRedis(){
		Jedis jedis = jedisPool.getResource();
		Long count = jedis.zcard(roomId);
		System.out.println("数量 = "+count);
		Set<String> s = jedis.zrange(roomId, count-10, -1);
		for(String str:s){
			System.out.println("str = "+str);
		}
		jedis.close();
		return s;
	}
	public  int getOnlineCount() {
		return counts.get(roomId).intValue();
	}

	public  void addOnlineCount() {
		counts.get(roomId).incrementAndGet();
	}

	public  void subOnlineCount() {
		counts.get(roomId).decrementAndGet();
	}

	public static void team_start(String teamId,String time,String team_type,String team_userid,String amount,String content,String team_roomId){
		Jedis jedis = jedisPool.getResource();
		Set<String> sets = jedis.smembers("set_"+teamId);
		String message = getMessage_Message(team_userid,content,time,team_type,jedis.scard("set_"+teamId).toString()+"/"+amount);
		String _message = getMessage_Team(message,"team",teamId);
		//		System.out.println(_message);
		for(ChatServer chat: rooms.get(team_roomId) ){
			JSONObject mes = JSON.parseObject(_message);
			if(sets.contains(chat.userid)){
				mes.put("state", "1");
			}else{
				mes.put("state", "2");
			}
			//			System.out.println(mes.toString());

			chat.session.getAsyncRemote().sendText(mes.toString());
		}
		jedis.close();
	}
	public static void team_end(String teamId,String team_type,String team_userid,String amount,String team_roomId){
		Jedis jedis = jedisPool.getResource();
		String talkId = "UUID"+UUID.randomUUID().toString().replaceAll("-", "");
		String message = null;
		Long number = jedis.scard("set_"+teamId);
		if(number>1){
			if(team_type.equals("1")){
				jedis.rename("set_"+teamId, "t_"+talkId);
			}else{
				jedis.sadd("t_"+talkId, team_userid);
				for(int i=0;i<Integer.valueOf(amount)-1;i++){
					jedis.sadd("t_"+talkId, jedis.spop("set_"+teamId));
				}
				jedis.del("set_"+teamId);
			}
			Set<String> sets=jedis.smembers("t_"+talkId);

			for(ChatServer chat: rooms.get(team_roomId) ){
				if(sets.contains(chat.userid)){
					message = getMessage_Talk("组队成功","1","talk/"+talkId);
				}else{
					message = getMessage_Talk("组队失败","2","");
				}
				chat.session.getAsyncRemote().sendText(message);
			}
			System.out.println("talk:"+message);
		}else{
			jedis.del("set_"+teamId);
			message = getMessage_Talk("组队失败","2","");
			for(ChatServer chat: rooms.get(team_roomId) ){
				chat.session.getAsyncRemote().sendText(message);
			}
		}
		jedis.close();
	}
}
