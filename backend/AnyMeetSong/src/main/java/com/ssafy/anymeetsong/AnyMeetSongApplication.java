package com.ssafy.anymeetsong;

import org.kurento.client.KurentoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.ssafy.anymeetsong.kurento.KurentoHandler;
import com.ssafy.anymeetsong.kurento.ManageRooms;
import com.ssafy.anymeetsong.kurento.RunShellScript;
import com.ssafy.anymeetsong.kurento.UserParticipate;

@SpringBootApplication
@EnableWebSocket
public class AnyMeetSongApplication implements WebSocketConfigurer{

	@Bean
	public UserParticipate participate() {
		return new UserParticipate();
	}
	
	@Bean
	public ManageRooms manageRooms() {
		return new ManageRooms();
	}
	
	@Bean
	public KurentoClient kurentoClient() {
		return KurentoClient.create("ws://3.36.108.155:8888/kurento");
	}
	
	@Bean
	public KurentoHandler kurentoHandler() {
		return new KurentoHandler();		
	}
	
	@Bean
	public RunShellScript runShellScript() {
		return new RunShellScript();
	}
	
	@Bean
	public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(32768);
		return container;
	}
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(kurentoHandler(), "/kurento")
		.setAllowedOrigins("*");
	}
	
	public static void main(String[] args) {
		SpringApplication.run(AnyMeetSongApplication.class, args);
	}

}
