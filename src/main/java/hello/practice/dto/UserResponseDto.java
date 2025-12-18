package hello.practice.dto;

import hello.practice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 사용자가 회원 정보를 조회할 때 사용하는 DTO
@Getter
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private String username;
    private String email;

    public static UserResponseDto from(User user) {

        return UserResponseDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

    }

}
