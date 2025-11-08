package com.dhyey.chatapp_withjpa.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

//    @Autowired
//    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    private MapSessionRepository sessionRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        if(request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");


            Session session = sessionRepository.findById(token);
            if(session == null || session.getAttribute("user_id") == null) {
                System.out.println("Unauthorized connection ...");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            System.out.println("HttpSessionId : "+session.getId());

            attributes.put("user_id", session.getAttribute("user_id"));
            attributes.put("HttpSessionId", session.getId());

            return true;
        }

        response.setStatusCode(HttpStatus.BAD_REQUEST);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
