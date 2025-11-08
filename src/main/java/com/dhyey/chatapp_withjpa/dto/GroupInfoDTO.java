package com.dhyey.chatapp_withjpa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoDTO {
    String group_name;
    Boolean is_group;
    String group_avatar;
    String group_desc;
}
