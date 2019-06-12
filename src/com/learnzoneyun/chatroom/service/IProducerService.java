package com.learnzoneyun.chatroom.service;

import javax.jms.Destination;

public interface IProducerService {
	public void sendMessage(Destination destination,final String msg);
}
