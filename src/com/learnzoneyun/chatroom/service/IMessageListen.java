package com.learnzoneyun.chatroom.service;

import javax.jms.Message;

public interface IMessageListen {
	public void onMeaasge(Message message);
}
