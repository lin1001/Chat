package com.learnzoneyun.chatroom.utils;

import com.learnzoneyun.chatroom.model.LogModel;

/**
 * NAME   :  LeaveSystem/com.amayadream.leave.util
 * Author :  Amayadream
 * Date   :  2015.12.29 15:07
 * TODO   :
 */
public class LogUtil {

    public LogModel setLog(String userid, String time, String type, String detail, String ip){
    	LogModel log = new LogModel();
        log.setUserid(userid);
        log.setTime(time);
        log.setType(type);
        log.setDetail(detail);
        log.setId(ip);
        return log;
    }

}
