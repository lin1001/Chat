package com.learnzoneyun.chatroom.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/talkServer/{talkId}", configurator = HttpSessionConfigurator.class)
public class TalkServer {
    private static int onlineCount = 0; //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static final Map<String, CopyOnWriteArraySet<TalkServer>> rooms = new HashMap<>();
    private static final Map<String, Integer> counts = new HashMap<>();
    private static final Map<String, List> lists = new HashMap<>();
    private Session session;    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private String userid;      //用户名
    private HttpSession httpSession;    //request的session
    private String talkId;
    private List list = new ArrayList<>();   //在线列表,记录用户名称
    private static Map routetab = new HashMap<>();  //用户名和websocket的session绑定的路由表

    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(@PathParam(value="talkId") String talkId,Session session, EndpointConfig config){
    	System.out.println("talkId="+talkId);
        this.session = session;
        this.talkId = talkId;
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
//        this.roomId = (String) httpSession.getAttribute("roomId");  
        this.userid=(String) httpSession.getAttribute("userid"); 
//        this.roomId = talkId+"_"+this.userid;
        CopyOnWriteArraySet<TalkServer> room = rooms.get(talkId);
        if (room == null) {
            synchronized (rooms) {
                if (!rooms.containsKey(talkId)) {
                	room = new CopyOnWriteArraySet<>();
                    rooms.put(talkId, room);
                    counts.put(talkId,0);
                    lists.put(talkId, list);
                }
            }
        }
        list = lists.get(talkId);
		list.add(userid);
		lists.put(talkId, list);
        room.add(this);
        counts.put(talkId,counts.get(talkId)+1);
//        webSocketSet.add(this);     //加入set中
//        addOnlineCount();           //在线数加1;

   //获取当前用户

        routetab.put(userid, session);   //将用户名和session绑定到路由表
        String message = getMessage("[" + userid + "]加入聊天室,当前在线人数为"+getOnlineCount()+"位", "notice",  list);
        broadcast(message);     //广播
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
    	CopyOnWriteArraySet<TalkServer> room = rooms.get(talkId);
    	list = lists.get(talkId);
    	room.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        list.remove(userid);        //从在线列表移除这个用户
        routetab.remove(userid);
        String message = getMessage("[" + userid +"]离开了聊天室,当前在线人数为"+getOnlineCount()+"位", "notice", list);
        broadcast(message);         //广播
    }

    /**
     * 接收客户端的message,判断是否有接收人而选择进行广播还是指定发送
     * @param _message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String _message) {
        JSONObject chat = JSON.parseObject(_message);
        JSONObject message = JSON.parseObject(chat.get("message").toString());
        if(message.get("to") == null || message.get("to").equals("")){      //如果to为空,则广播;如果不为空,则对指定的用户发送消息
            broadcast(_message);
        }else{
            String [] userlist = message.get("to").toString().split(",");
            singleSend(_message, (Session) routetab.get(message.get("from")));      //发送给自己,这个别忘了
            for(String user : userlist){
                if(!user.equals(message.get("from"))){
                    singleSend(_message, (Session) routetab.get(user));     //分别发送给每个指定用户
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
    	for(TalkServer chat: rooms.get(talkId) ){
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

    public  int getOnlineCount() {
        return counts.get(talkId);
    }

    public  void addOnlineCount() {
    	counts.put(talkId,counts.get(talkId)+1);
    }

    public  void subOnlineCount() {
    	counts.put(talkId,counts.get(talkId)-1);
    }
}
