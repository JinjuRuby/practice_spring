package hello.practice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter // JPA는 setter가 없어도 강제로 값을 넣는 기능이 존재한다.
        // 즉 개발자가 따로 값을 set할 일이 없으면 넣으면 안된다. (치명적인 오류가 날 수 있기 때문에)

@NoArgsConstructor(access = AccessLevel.PROTECTED) // 빈 기본 생성자를 자동으로 만들어주는 애노테이션
                   // -> JPA는 기본 생성자가 필요하다. 왜냐하면 생성자가 여러 개 있을 때 어떤 생성자가 필요한지 JPA가 판단할 수 없기 때문에 먼저 기본 생성자를 호출한다
                   // Q: 그렇다면 생성자를 만들지 않으면 기본 생성자를 자동으로 만들어주는데 왜 이 애노테이션이 필요한가?
                   // A: 개발자가 모르고 생성자를 만들었을 때는 기본 생성자가 자동으로 만들어지지 않는다. 즉, 일종의 보험으로 @NoArgsConstructor를 사용한다
                   // -> ()안의 기능은 기본 생성자의 접근 권한을 protected로 설정하는 것이다.
                   // 다른 개발자들이 의미없는 기본 생성자를 사용할 수 있는 것을 방지하기 위해서 사용한다.
                   // Q: 그렇다면 기본 생성자의 접근 권한을 private으로 하지 왜 protected로 하나?
                   // A: JPA는 지연 로딩(Lazy Loading)을 위해 엔티티를 상속받은 가짜 객체(프록시)를 만들어 사용한다.
                   //    이때 자식(프록시)이 부모(엔티티)의 생성자를 호출해야 하므로,
                   //    상속 관계에서 접근 가능한 protected로 설정해야 한다. (private은 상속받은 자식도 접근 불가)

@Table(name = "users") // 데이터베이스에 user라는 예약어가 있는 경우가 많다. 따라서 테이블 명을 바꿔주는게 좋은데 이때 이 애노테이션을 사용한다
                       // -> 테이블명을 바꾸기 위해서 ()안 처럼했고 이러면 테이블명이 users가 된다
public class User {

    @Id //primary key로 지정하는 애노테이션
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id를 생성하는 애노테이션 ()안의 내용은 값을 어떻게 부여할 것인지를 설정
                                                        // GenerationType.IDENTITY로 해야 primary key값이 1부터 순차적으로 부여된다.
    private Long id; // long이나 int의 초기값은 0이다. 따라서 JPA 입장에서는 이게 id값인지 초기값인지 알 수 없다.
                     // 따라서 래퍼 클래스(Long, Integer)로 지정해야 한다. (래퍼 클래스는 초기값이 null이다.)
                     // 하지만 실무에서는 무조건 Long과 Integer중에서 Long을 사용하는데 Integer는 약 21억을 저장할 수 있고 Long은 약 922경을 저장할 수 있기 때문이다

    @Column(nullable = false, length = 10) // userName은 필수로 있어야 하는 값이기 때문에 nullable을 false로 지정했고 길이를 10으로 제한했다.
                                           // @Column은 해당 컬럼의 설정을 변경할 수 있는 애노테이션이다.
    private String userName;

    @Column(unique = true) // @Column에서 unique를 true로 설정하면 이 컬럼은 중복되면 안된다는 의미이다.
    private String email;

    @Builder // @Builder는 객체를 생성할 때 생성자의 파라미터 순서 상관없이 값을 넣을 수 있다. 또한 생성자를 호출할 때 변수명을 사용해 값을 넣기 때문에 가독성이 좋다.
    public User(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }
}
