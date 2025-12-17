package hello.practice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// 회원가입을 할 때 사용자에게 정보를 받는 DTO
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserRequestDto {

    @NotBlank(message = "이름을 작성해주세요")
    private String username;
    @NotBlank(message = "이메일을 작성해주세요")
    @Email
    private String email;
    @NotBlank(message = "비밀번호를 작성해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상, 20자 이하입니다.")
    private String password;

}
