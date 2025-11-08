package com.dhyey.chatapp_withjpa.services;

import com.dhyey.chatapp_withjpa.dto.GlobalUsersSearch;
import com.dhyey.chatapp_withjpa.dto.LoggedUserData;
import com.dhyey.chatapp_withjpa.dto.UserLoginDTO;
import com.dhyey.chatapp_withjpa.entities.User;
import com.dhyey.chatapp_withjpa.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Optional<LoggedUserData> findLoggedUserDataByUserId(Long userId){
        return userRepository.findUserByLoggedUserId(userId);
    }

    public List<GlobalUsersSearch> findGlobalUsers(String username, String profileName){
        return userRepository.findByUsernameContainingIgnoreCaseOrProfileNameContainingIgnoreCase(username, profileName);
    }

    public Optional<User> findUserByUsernameAndPassword(String username, String password){
        return userRepository.findByUsernameAndPassword(username, password);
    }

    public Optional<User> findUserByEmailAndPassword(String email, String password){
        return userRepository.findByEmailAndPassword(email, password);
    }

    public Optional<User> getUser(UserLoginDTO user) {
        Optional<User> usr;

        if(user.getEmail() != null && user.getPassword() != null && user.getUsername() == null){
           usr  = findUserByEmailAndPassword(user.getEmail(), user.getPassword());
        }else if(user.getUsername() != null && user.getPassword() != null && user.getEmail() == null) {
            usr = findUserByUsernameAndPassword(user.getUsername(), user.getPassword());
        }else{
            return Optional.empty();
        }

        return usr;
    }

}
