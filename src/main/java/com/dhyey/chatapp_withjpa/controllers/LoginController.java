package com.dhyey.chatapp_withjpa.controllers;

import com.dhyey.chatapp_withjpa.dto.UserLoginDTO;
import com.dhyey.chatapp_withjpa.entities.User;
import com.dhyey.chatapp_withjpa.services.UserService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        origins = "http://localhost:3000",
        exposedHeaders = "X-Auth-Token",
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    private ResponseEntity<?> login(ServletRequest request, @RequestBody UserLoginDTO user) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpSession existing_session = httpRequest.getSession(false);

        if (existing_session != null) {
            return ResponseEntity.ok(existing_session.getId() + " You are already logged in ....");
        }

        Optional<User> usr = userService.getUser(user);
        Long userId = usr.map(User::getUserId).orElse(null);

        if (userId != null && userId != -1L) {
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("user_id", userId);
            session.setMaxInactiveInterval(5 * 60 * 60); // 5 hours in seconds
            return ResponseEntity.ok(session.getId());
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such user found");
        }
    }

}
