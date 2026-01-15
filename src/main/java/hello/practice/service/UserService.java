package hello.practice.service;

import hello.practice.dto.UserRequestDto;
import hello.practice.dto.UserResponseDto;
import hello.practice.entity.User;
import hello.practice.repository.BoardRepository;
import hello.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 회원(User)과 관련된 비즈니스 로직을 처리하는 서비스
 * - 회원가입, 로그인, 회원탈퇴 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 성능 최적화를 위해 기본적으로 읽기 전용 모드로 설정
public class UserService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    /**
     * [회원가입]
     * 1. 이메일 중복 검사 (이미 가입된 이메일인지 확인)
     * 2. 가입 요청 데이터(DTO)를 회원 엔티티(Entity)로 변환
     * 3. DB에 저장 (INSERT)
     * 4. 가입된 정보를 응답 DTO로 변환하여 반환
     */
    @Transactional // DB에 데이터를 저장해야 하므로 '쓰기 모드' 활성화
    public UserResponseDto signUp(UserRequestDto requestDto) {

        // [Step 1] 중복 검증 (Validation)
        // DB에 이미 같은 이메일이 있는지 확인한다.
        // 만약 있다면 예외를 발생시켜서 회원가입을 막아야 한다. (Fail-Fast 원칙)
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("가입한 이메일이 존재합니다.");
        }

        // [Step 2] DTO -> Entity 변환
        // 빌더 패턴을 사용하여 DTO의 데이터를 User 객체로 옮겨 담는다.
        // *주의: 현재는 비밀번호를 있는 그대로(Plain Text) 저장하고 있다.
        // (실무에서는 BCryptPasswordEncoder 등을 사용해 반드시 암호화해야 함!)
        User user = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
                .build();

        // [Step 3] DB 저장
        // save() 호출 시점에 영속성 컨텍스트에 저장되고, 트랜잭션 종료 시 INSERT 쿼리가 실행된다.
        userRepository.save(user);

        // [Step 4] Entity -> DTO 변환 후 반환
        // 엔티티(User)는 DB와 직접 연결된 중요한 객체이므로,
        // 클라이언트에게는 필요한 정보만 담은 DTO(UserResponseDto)를 내보낸다.
        // (비밀번호 같은 민감한 정보는 DTO에서 제외하는 것이 좋다.)
        return UserResponseDto.from(user);
    }

    /**
     * [회원 탈퇴]
     * 핵심 로직: 참조 무결성(Referential Integrity) 유지
     * - 회원을 삭제하기 전에, 그 회원이 쓴 게시글을 먼저 삭제해야 한다.
     * - 만약 회원을 먼저 지우면, 게시글은 '작성자 없는 글'이 되어 DB 에러(FK 제약조건 위반)가 발생할 수 있다.
     */
    @Transactional
    public void withdraw(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // [Step 1] 회원이 작성한 모든 게시글 삭제
        // BoardService가 아니라 Repository를 직접 호출해서 삭제한다.
        // (이 과정이 하나의 트랜잭션 안에 묶여야 안전하다.)
        boardRepository.deleteByUser(user);

        // [Step 2] 회원 정보 삭제
        // 게시글이 다 지워진 후에야 안전하게 회원을 삭제할 수 있다.
        userRepository.delete(user);
    }

    /**
     * [로그인]
     * 1. 이메일로 회원을 찾는다.
     * 2. 회원이 존재하고, 비밀번호가 일치하는지 확인한다.
     * 3. 일치하면 회원 정보를 반환하고, 실패하면 예외를 발생시킨다. (수정됨)
     *
     * 참고: 로그인 같은 단순 조회는 데이터를 변경하지 않으므로
     * 클래스 레벨의 @Transactional(readOnly = true)가 적용되어 성능에 유리하다.
     */
    public UserResponseDto login(String email, String password) {

        // [Step 1] 이메일 조회
        // orElse(null): 유저를 못 찾으면 null을 반환하여 다음 단계에서 검증한다.
        User user = userRepository.findByEmail(email).orElse(null);

        // [Step 2] 검증 (아이디 존재 여부 AND 비밀번호 일치 여부)
        // 아이디가 없거나(null), 비밀번호가 틀리면 예외를 터뜨린다.
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // [Step 3] 로그인 성공
        // 검증을 통과했으므로 회원 정보를 DTO로 반환한다.
        return UserResponseDto.from(user);
    }

}