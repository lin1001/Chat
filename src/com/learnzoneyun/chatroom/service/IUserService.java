package com.learnzoneyun.chatroom.service;

import java.util.List;

import com.learnzoneyun.chatroom.model.UserModel;


public interface IUserService {
    List<UserModel> selectAll(int page, int pageSize);
    UserModel selectUserByUserid(String userid);
    int selectCount(int pageSize);
    boolean insert(UserModel user);
    boolean update(UserModel user);
    boolean delete(String userid);
}
