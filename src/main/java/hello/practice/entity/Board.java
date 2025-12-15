package hello.practice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    // [1. @ManyToOne]
    // 역할: DB의 외래키(FK)를 테이블에 만들고, 동시에 객체 간의 관계(N:1)를 정의하는 애노테이션
    // 애노테이션을 쓰면 JPA는 DB에 저장할 때, 객체 전체가 아니라 해당 객체의 PK 값만 추출해서 외래키 컬럼에 저장한다.
    // DB는 객체를 저장할 수 없기 때문에 엔티티 필드에 객체가 온다면 객체 간의 관계 애노테이션이 필수로 있어야 한다. (@OneToOne, @ManyToMany, @OneToMany, @ManyToOne가 있다.)

    // 참고: @ManyToMany는 실무에서 사용을 지양한다.

    // Q: 단일 애노테이션(@ForeignKey)으로 만들지 않고 애노테이션 이름을 객체 간의 관계(@OneToOne, ...)로 지은 이유가 무엇인가?
    // A: 객체 간의 관계 애노테이션마다 수행하는 기능이 다르기 때문이다.

    // 참고: 외래키(FK)는 무조건 N(다) 쪽에 생긴다.
    // 왜냐하면 일(1)쪽에 있으면 데이터베이스의 한 칸(Cell)에 여러 개의 값이 들어가야하는데 Cell은 오직 하나의 값만 들어갈 수 있기 때문이다.

    // Q: 외래키가 N쪽에 있으면 다(N)쪽 애노테이션만 있으면 되지 일(1)쪽 객체에서 사용하는 애노테이션(@OneToOne, @OneToMany)이 굳이 필요한가?
    // A: 필수는 아니다. 다만 일(1)쪽에서 다(N)쪽 객체가 필요할 경우가 존재한다.(사용자가 자신이 쓴 글 목록을 가져오는 경우) 이때 객체를 편하게 탐색하기 위해 @OneToMany를 추가하여 양방향 관계를 만드는 것이다.

    // (fetch = FetchType.LAZY): '지연 로딩' 설정
    // 글을 조회할 때 작성자 정보는 가져오지 않고, 실제 작성자 정보가 필요할 때(getter 호출 시) DB에서 조회한다. 즉 성능을 최적화하기 위해서 사용하는 것이다.
    @ManyToOne(fetch = FetchType.LAZY)

    // [2. @JoinColumn]
    // @Column 애노테이션과 유사함. Column의 설정을 변경하는 애노테이션
    // (name = "user_id"): name은 이 테이블에 외래키의 컬럼명을 어떻게 할 것인지를 설정하는 기능이다. 만약 이 설정을 하지 않는다면 JPA에서 컬럼명을 이상한 형태로 저장한다. 컬럼명은 보통 '외래키의 변수명_외래키의 PK'로 실무에서는 사용한다.

    // @Column과 @JoinColumn의 차이점
    // @Column은 일반 데이터 타입(String, int ...)의 세부 기능을 설정할 때 사용한다.
    // @JoinColumn은 객체(외래키)의 세부 기능을 설정할 때 사용한다.
    @JoinColumn(name = "user_id")

    // DB에서는 객체를 외래키로 저장한다. 만약 객체를 외래키로 지정하지 않으면 런타임 에러가 발생한다.
    private User user;

    @Column(columnDefinition = "TEXT") // columnDefinition을 TEXT로 설정하면 데이터베이스의 TEXT 타입으로 매핑되어, 길이 제한(255자) 없이 아주 긴 글을 저장할 수 있게 된다.
    private String content;

    @Builder
    public Board(String title, User user, String content) {
        this.title = title;
        this.user = user;
        this.content = content;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
