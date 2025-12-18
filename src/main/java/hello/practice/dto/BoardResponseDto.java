package hello.practice.dto;

import hello.practice.entity.Board;
import lombok.*;

// 작성한 글을 보여주기 위한 DTO
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardResponseDto {

    private Long id;
    private String title;
    private String content;
    private String writer;


    // Board를 BoardResponseDto로 만들 때 생성자가 아니라 메서드로 만든 이유:
    // from 메서드의 역할:

    public static BoardResponseDto from(Board board) {

        return BoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getUsername())
                .build();
    }

}
