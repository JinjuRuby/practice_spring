package hello.practice.dto;

import hello.practice.entity.Board;
import hello.practice.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 사용자가 글을 작성하고 저장할 때 사용하는 DTO
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Spring(Jackson)이 JSON을 변환할 때, '빈 상자(객체)'를 만들기 위해 필요하다.
// -----------------------------------------------------------
// Q: Spring은 DTO를 JSON으로 변환을 하는 이유가 무엇인가?
// 클라이언트가 보낸 'JSON 데이터를 DTO 객체로 변환'하는 것이다.
// A: 웹(Web)과 자바(Java)의 언어가 다르기 때문이다.
//    - 웹 브라우저는 데이터를 'JSON(문자열)' 형태로 보내고,
//    - 자바는 데이터를 '객체(Class)' 형태로 다룬다.
//    - 따라서 둘 사이의 통역사(Jackson)가 JSON을 DTO 객체로 바꿔주는 과정(파싱)이 필요하다.
// -----------------------------------------------------------

@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자를 생성. 필드에 값을 넣기 위해 사용한다.
@Builder // 클래스에 붙이면, 모든 필드(title, content)를 재료로 쓰는 빌더를 만들어준다.
public class BoardRequestDto {

    @NotBlank(message = "제목을 작성해주세요")
    private String title;
    @NotBlank(message = "글을 작성해주세요")
    private String content;

    public Board toEntity(User user) {

        return Board.builder()
                .title(this.title)
                .content(this.content)
                .user(user)
                .build();

    }

    // -----------------------------------------------------------
    // [학습 메모 1: 생성자를 2개나 만드는 이유]
    // Q: 하나만 있으면 안 되는가?
    // A: 사용하는 주체와 기술이 다르기 때문이다.
    //    1. @NoArgsConstructor : Spring(Jackson)이 'Reflection' 기술로 빈 객체를 생성할 때 필수적이다.
    //    2. @AllArgsConstructor : 우리가 값을 채워 넣을 때 사용한다. (Builder가 내부적으로 이 생성자를 호출함)
    //    => 따라서 두 기술이 모두 정상 동작하려면 2개의 생성자가 모두 존재해야 한다.
    // -----------------------------------------------------------

    // -----------------------------------------------------------
    // [학습 메모 2: 클래스 레벨 @Builder와 생성자의 관계]
    // Q: 클래스 위에 @Builder를 붙이면 정확히 무슨 일이 일어나는가?
    // A: 이 클래스의 '모든 필드(변수)'를 재료로 사용하는 빌더가 생성된다.
    // -----------------------------------------------------------

    // -----------------------------------------------------------
    // [학습 메모 3: 특정 필드만 빌더로 만들고 싶을 때]
    // Q: 만약 특정 필드값만 생성자 매개변수를 통해 값을 넣고 싶을 때 @Builder를 사용할 수 있나? 그렇다면 어떻게 사용해야 하나?
    // A: 가능하다.
    //    1. 클래스 위의 @Builder를 지운다.
    //    2. 원하는 필드(변수)만 포함하는 생성자를 직접 작성한다.
    //    3. 그 '생성자 위에' @Builder를 붙인다.
    //    => 이렇게 하면 딱 그 생성자의 재료들만 사용하는 빌더가 만들어진다.
    // -----------------------------------------------------------

    // -----------------------------------------------------------
    // [학습 메모 4: @Builder 사용 시 @AllArgsConstructor 필수 여부]
    // Q: @Builder를 쓸 때 무조건 @AllArgsConstructor를 써야 하는가?
    // A: 상황에 따라 다르다. (생성자 충돌 문제)
    //    1. 생성자가 아예 없을 때: 롬복이 눈치껏 생성자를 만들어주므로 안 써도 된다.
    //    2. @NoArgsConstructor가 있을 때: 롬복이 자동 생성을 멈추므로 빌더가 갈 길을 잃어 에러가 난다.
    //       => 이때는 @AllArgsConstructor를 명시적으로 꼭 써줘야 한다.
    //    => 결론: @NoArgs를 쓴다면 @AllArgs도 세트로 꼭 같이 써야 안전하다.
    // 하지만 상황에 상관없이 코드의 안정성을 위해 @Builder를 사용한다면 @AllArgsConstructor를 사용해야한다.
    // ----------------------------------------------------------

    // -----------------------------------------------------------
    // [학습 메모 5: Entity vs DTO의 NoArgsConstructor 차이]
    // Q: 엔티티의 @NoArgsConstructor랑 DTO의 @NoArgsConstructor랑 무슨 차이가 있는가?
    // A: '누가' 그리고 '왜' 사용하는지가 다르다.
    //    1. DTO: 'Jackson 라이브러리'가 JSON 데이터를 담을 빈 객체를 만들기 위해 사용한다. (단순 데이터 전달용)
    //    2. Entity: 'JPA(Hibernate)'가 데이터베이스 조회를 위해 '프록시(가짜 객체)'를 만들 때 필수적으로 사용한다. (기능 동작용)
    // -----------------------------------------------------------

}