package com.dhyey.chatapp_withjpa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalUsersSearch {
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("profile_name")
    private String profileName;
    @JsonProperty("username")
    private String username;

    @JsonProperty("avatar")
    private String avatar;
}
