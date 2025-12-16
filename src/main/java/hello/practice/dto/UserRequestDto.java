package hello.practice.dto;

import lombok.*;

// 회원가입을 할 때 사용자에게 정보를 받는 DTO
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserRequestDto {

    private String userName;
    private String email;
    private String password;

}
