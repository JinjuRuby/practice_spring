package hello.practice.dto;

import lombok.*;

// 회원가입을 할 때 사용하는 DTO
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRequestDto {

    private String userName;
    private String email;
    private String password;

    @Builder
    public UserRequestDto(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }
}
