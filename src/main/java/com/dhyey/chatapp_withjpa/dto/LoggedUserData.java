package com.dhyey.chatapp_withjpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedUserData {
    String profile_name;
    Long user_id;
    String email;
    String username;
}
