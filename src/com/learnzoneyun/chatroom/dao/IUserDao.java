package com.learnzoneyun.chatroom.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.learnzoneyun.chatroom.model.UserModel;

@Repository(value = "userDao")
public interface IUserDao {
    List<UserModel> selectAll(@Param("offset") int offset, @Param("limit") int limit);

    UserModel selectUserByUserid(String userid);

    UserModel selectCount();

    boolean insert(UserModel user);

    boolean update(UserModel user);

    boolean delete(String userid);
}
