package hello.practice.controller;

import hello.practice.dto.LoginRequestDto;
import hello.practice.dto.UserRequestDto;
import hello.practice.dto.UserResponseDto;
import hello.practice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [UserController]
 * 회원 가입, 탈퇴, 로그인 등 사용자 관련 API 요청을 처리함.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [회원가입]
     * @Valid: 입력받은 회원 정보의 형식 유효성을 검증
     * @RequestBody: JSON 데이터를 UserRequestDto 객체로 변환
     * @return 201(Created): 회원 리소스가 성공적으로 생성되었음을 반환
     */
    @PostMapping
    public ResponseEntity<String> signUp(@Valid @RequestBody UserRequestDto requestDto) {

        userService.signUp(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입에 성공했습니다.");

    }

    /**
     * [회원탈퇴]
     * * @SessionAttribute: 세션 저장소에서 "loginUser" 키로 보관된 유저 식별자(PK)를 추출함
     * - required = false: 비로그인 상태의 접근을 허용하여 내부 로직에서 예외 처리함
     *
     * * HttpServletRequest: 클라이언트의 모든 요청 정보(헤더, 쿠키, 세션 등)를 담고 있는 객체임
     * - request.getSession(false): 기존 세션 존재 여부만 확인함 (없어도 새로 생성하지 않음)
     *
     * @param request: 세션 무효화(invalidate) 처리를 위해 서블릿 요청 객체를 사용함
     * @return 200(OK): 회원 정보 삭제 및 세션 파기 완료 후 성공 메시지 반환
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @SessionAttribute(name = "loginUser", required = false) Long userId,
            HttpServletRequest request) {

        // 로그인 여부 검증 및 미인증 시 401(Unauthorized) 상태 코드 반환
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 서비스 계층을 호출하여 DB에서 회원 정보를 삭제함.
        userService.withdraw(userId);

        // request.getSession(false): 기존 세션이 존재할 때만 가져오며 없으면 null 반환함
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 서버 메모리 내 세션 정보 즉시 제거
        }

        return ResponseEntity.ok("회원탈퇴 되었습니다.");

    }

    /**
     * [로그인]
     * * @param requestDto: 클라이언트가 전송한 이메일과 비밀번호 데이터
     * @param request: 인증 성공 시 새로운 세션을 생성하기 위해 활용
     * @return 200(OK): 세션 생성 및 유저 정보 저장 완료 후 빈 응답 본문 반환
     */
    @PostMapping("/sessions")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletRequest request) {

        UserResponseDto responseDto = userService.login(requestDto.getEmail(), requestDto.getPassword());

        // request.getSession(): 기존 세션이 없으면 새로 생성(true)하여 반환
        // 세션 내에 "loginUser"라는 이름으로 유저 고유 식별자 매핑 및 보관 수행
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", responseDto.getId());

        return ResponseEntity.ok().build();
    }

    /**
     * [로그아웃]
     * * @param request: 현재 클라이언트의 세션을 식별하고 파기하기 위해 사용함
     * @return
     * - 200(OK): 세션이 존재하여 정상적으로 파기 완료함
     * - 400(Bad Request): 아래와 같은 사유로 요청을 처리할 수 없음을 반환함
     * 1) 이미 로그아웃되어 세션이 만료된 경우 (중복 요청)
     * 2) 로그인하지 않은 상태에서 API를 직접 호출한 경우(부정 요청)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        // false 옵션: 세션이 없을 때 새로 생성하지 않도록 하여 부정 요청 여부를 판별함
        HttpSession session = request.getSession(false);

        // 세션이 존재할 경우에만 invalidate()를 호출하여 접속 정보를 제거함.
        if (session != null) {
            session.invalidate(); // 유효 세션 제거
            return ResponseEntity.ok("로그아웃 되었습니다.");
        }

        // 이미 로그아웃된 경우 또는 세션이 없는 부정 요청 시 400(Bad Request) 상태 코드 반환
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 로그아웃 상태이거나 유효하지 않은 요청입니다.");
    }
}