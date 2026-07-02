package com.sales.visits.app.sales.dto.response;

import com.sales.visits.app.sales.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamLeaderResponse {
    private Long id;
    private String username;

    public static TeamLeaderResponse from(User user) {
        return TeamLeaderResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
