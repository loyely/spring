package com.zzw;

import com.zzw.service.UserService;
import com.zzw.spring.ClassPathXmlApplicationContext;

public class App {

	
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("com.zzw");
		UserService userService = (UserService) app.getBean("userServiceImpl");
		userService.add();
	}
}
