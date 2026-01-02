package hello.practice.repository;

import hello.practice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [핵심 학습 노트]
 *
 * 1. Interface인데 작동하는 이유?
 * - 실행 시점(Runtime)에 스프링이 가짜 구현체(Proxy 객체)를 만들어서 주입해준다.
 *
 * 2. 제네릭 <User, Long>의 의미
 * - User: 다룰 엔티티 (테이블)
 * - Long: 엔티티의 PK(@Id) 타입
 *
 * 3. 쿼리 메서드 이름 규칙 (Prefix)
 * - 조회: find..By.. (예: findByEmail, findByTitleContaining)
 * - 검사: exists..By.. (참/거짓 반환)
 * - 개수: count..By..
 * - 삭제: delete..By..
 */

// @Repository 생략 가능 (JpaRepository 상속 시 자동 빈 등록)
public interface UserRepository extends JpaRepository<User, Long> {

    // =================================================================
    //  [1] 직접 추가한 메서드 (Query Methods)
    // =================================================================

    /**
     * 이메일로 회원 조회 (로그인 시 사용)
     *
     * Q: 왜 반환 타입을 Optional로 했나?
     * A1: NPE 방지(회원이 없는 경우 null 대신 '빈 상자'를 반환해 에러를 막는다.)
     * A2: 명시적 표현("결과가 없을 수도 있음"을 리턴 타입만 보고 알 수 있다.)
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 여부를 확인한다. (회원가입 시 사용)
     * - true: 이미 존재함 / false: 사용 가능
     */
    boolean existsByEmail(String email);


    // =================================================================
    //  [2] JpaRepository 기본 제공 메서드 (참고용)
    // =================================================================
    /*
     * save(entity)     : 저장 및 수정 (ID 없으면 insert, 있으면 update)
     * findById(id)     : PK로 단건 조회 (Optional 반환)
     * findAll()        : 전체 조회
     * deleteById(id)   : PK로 삭제
     */

}
