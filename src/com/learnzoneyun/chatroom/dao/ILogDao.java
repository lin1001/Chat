package com.learnzoneyun.chatroom.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.learnzoneyun.chatroom.model.LogModel;

@Repository(value = "logDao")
public interface ILogDao {
	List<LogModel> selectAll(@Param("offset") int offset, @Param("limit") int limit);

	List<LogModel> selectLogByUserid(@Param("userid") String userid, @Param("offset") int offset, @Param("limit") int limit);

	LogModel selectCount();

	LogModel selectCountByUserid(@Param("userid") String userid);

	boolean insert(LogModel log);

	boolean delete(String id);

	boolean deleteThisUser(String userid);

	boolean deleteAll();
}
