package hello.practice.dto;

import lombok.*;

// 글을 작성하고 저장할 때 사용하는 DTO
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardRequestDto {

    private String title;
    private String content;

    @Builder
    public BoardRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
