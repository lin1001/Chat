package com.learnzoneyun.chatroom.service;

import java.util.List;

import com.learnzoneyun.chatroom.model.LogModel;


public interface ILogService {
	List<LogModel> selectAll(int page, int pageSize);
	List<LogModel> selectLogByUserid(String userid, int page, int pageSize);
	int selectCount(int pageSize);
	int selectCountByUserid(String userid, int pageSize);
	boolean insert(LogModel log);
	boolean delete(String id);
	boolean deleteThisUser(String userid);
	boolean deleteAll();
}
