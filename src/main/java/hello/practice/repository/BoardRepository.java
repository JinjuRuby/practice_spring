package hello.practice.repository;

import hello.practice.entity.Board;
import hello.practice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 게시글(Board) 데이터 접근을 위한 Repository
 *
 * [학습 메모 0: JpaRepository란?]
 * Q: JpaRepository<Board, Long>은 무엇이며 왜 상속받나?
 * A: Spring Data JPA가 제공하는 '만능 도구 모음'이다.
 * - 역할: 기본적인 CRUD(저장, 조회, 수정, 삭제) 메서드가 이미 구현되어 있어 바로 쓸 수 있다.
 * - 제네릭 <Board, Long>: "이 리포지토리는 'Board 엔티티'를 다루고, PK타입은 'Long'이다"라고 명시하는 것.
 *
 * [학습 메모 1: 동작 원리]
 * Q: 인터페이스인데 구현체 없이 어떻게 동작하나?
 * A: Spring Data JPA가 실행 시점에 이 인터페이스를 보고 자동으로 가짜 구현체(Proxy 객체)를 만들어 주입(DI)해준다.
 *
 *
 * [학습 메모 2: 복잡한 쿼리 해결]
 * Q: 메서드 이름만으로 해결되지 않는 복잡한 쿼리는?
 * A: 1. @Query 어노테이션으로 JPQL/SQL 직접 작성
 * 2. Querydsl이나 별도의 Custom 인터페이스 + Impl 클래스를 만들어 상속
 *
 */
public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 모든 게시글을 최신순(ID 내림차순)으로 조회한다.
     *
     * [학습 메모: findBy vs findAllBy]
     * 1. 기능적 차이: 사실상 없다. JPA는 'All' 같은 중간 수식어를 무시한다.
     * 2. 에러 발생 원인: 메서드 이름보다는 '반환 타입'이 중요하다.
     * - 반환 타입이 List인 경우: 데이터가 없으면 빈 리스트([]) 반환, 여러 개면 모두 반환. (안전)
     * - 반환 타입이 단건(Board, Optional)인 경우: 결과가 2개 이상이면 예외 발생.
     * 3. 관례: 리스트를 반환할 때는 가독성을 위해 'findAllBy'를 붙여주는 것이 좋다.
     *
     */
    List<Board> findAllByOrderByIdDesc();

    /**
     * 제목(Title)에 특정 단어가 포함된 게시글을 최신순으로 조회한다.
     *
     * [학습 메모: 검색 키워드]
     * - findByTitle : 제목이 정확히 일치(=)하는 것만 조회
     * - findByTitleContaining : 제목에 검색어가 포함(LIKE %word%)된 것을 조회
     *
     * @param title 검색할 제목 키워드
     */
    List<Board> findAllByTitleContainingOrderByIdDesc(String title);

    /**
     * 작성자(User)의 이름(username)이 포함된 게시글을 최신순으로 조회한다.
     *
     * [학습 메모 3: 연관 관계 탐색과 언더바(_)]
     * Q: Board에는 username 필드가 없는데 어떻게 찾나?
     * A: JPA의 속성 탐색 기능이 Board 내의 User 객체를 타고 들어가 JOIN 쿼리를 생성한다.
     *
     * Q: 왜 User_Username 처럼 언더바(_)를 넣었나?
     * A: 'Board의 User'와 'User의 username' 경계를 명확히 하기 위해서다.
     * (언더바가 없으면 Spring이 BoardUser라는 필드를 찾으려다 실패할 수 있음)
     *
     * * 주의: User 엔티티의 변수명이 `userName` -> `username`으로 바뀌었으므로,
     * 메서드 명도 `UserName` -> `Username`으로 맞춰야 한다. (CamelCase 적용)
     *
     * @param username 검색할 작성자 이름
     */
    List<Board> findAllByUser_UsernameContainingOrderByIdDesc(String username);

    /**
     * 작성자(User)가 작성한 게시글들을 모두 삭제한다. (회원 탈퇴 시 사용)
     *
     * [학습 메모 4: 삭제 메서드의 동작 원리와 주의점]
     * Q: deleteByUser는 SQL의 'DELETE FROM Board WHERE user_id = ?' 처럼 한 방에 지워지나?
     * A: 아니다. Spring Data JPA의 기본 deleteBy 메서드는 다음과 같이 동작한다.
     * 1. SELECT: 해당 조건의 데이터를 모두 조회해온다. (만약 글이 100개면 100개를 다 가져옴)
     * 2. LOOP: 조회된 엔티티를 하나씩 순회한다.
     * 3. DELETE: 하나씩 삭제 쿼리를 날린다. (DELETE 쿼리가 100번 나감)
     *
     * Q: 왜 이렇게 비효율적으로 동작하나?
     * A: JPA는 '영속성 컨텍스트'를 통해 엔티티의 생명주기를 관리하기 때문이다.
     * 하나씩 지워야 그 엔티티와 연관된 다른 작업(Cascade, 고아 객체 제거 등)을 안전하게 처리할 수 있다.
     *
     * * 결론: 데이터가 적을 때는 안전하고 좋지만, 데이터가 수만 건일 때는 성능 이슈가 발생할 수 있다.
     * (그때는 @Modifying, @Query를 써서 벌크 연산으로 바꿔야 한다.)
     *
     * * 필수: delete 작업은 트랜잭션이 필수이므로, Service 계층에 @Transactional이 꼭 있어야 한다.
     *
     * @param user 삭제할 게시글의 작성자
     */
    void deleteByUser(User user);
}