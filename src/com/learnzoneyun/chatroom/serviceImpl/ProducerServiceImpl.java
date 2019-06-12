package com.learnzoneyun.chatroom.serviceImpl;

import javax.annotation.Resource;
import javax.jms.*;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.learnzoneyun.chatroom.service.IProducerService;

@Service(value = "producerService")
public class ProducerServiceImpl implements IProducerService {
	ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-activeMQ.xml");
	private JmsTemplate jmsTemplate = ctx.getBean("jmsTemplate", JmsTemplate.class);

	@Override
	public void sendMessage(Destination destination, final String msg) {
		System.out.println("Send " + msg + " to Destination " + destination.toString());

		MessageCreator messageCreator = new MessageCreator(){

				public Message createMessage(Session session) throws JMSException {

				return session.createTextMessage(msg);
			}

		};
		jmsTemplate.send(destination, messageCreator);

	}

}
