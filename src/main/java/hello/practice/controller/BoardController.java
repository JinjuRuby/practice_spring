package hello.practice.controller;

import hello.practice.dto.BoardRequestDto;
import hello.practice.dto.BoardResponseDto;
import hello.practice.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 게시글(Board) 관련 요청을 처리하는 컨트롤러
 *
 * [Annotation 설명]
 * 1. @RestController: JSON 데이터를 반환하는 API용 컨트롤러임을 명시한다. (HTML 제외)
 * 2. @RequestMapping: 해당 클래스의 기본 URL 경로를 "/api/boards"로 설정한다.
 * 3. @RequiredArgsConstructor: final 필드에 대한 생성자를 자동 생성하여 의존성 주입(DI)을 수행한다.
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * [게시글 작성]
     * * * @Valid: DTO 내부의 제약 조건(Not Blank 등)을 기반으로 유효성 검증 수행한다.
     * @RequestBody: HTTP 요청 본문의 JSON 데이터를 자바 객체로 변환한다.
     * @SessionAttribute: 세션 데이터를 파라미터로 주입한다.
     * 세션: 서버가 클라이언트를 식별하기 위해 서버 메모리에 저장하는 정보 공간
     * - name = "loginUser": 세션에서 해당 이름으로 저장된 값을 찾아 매핑한다.
     * - required = false:
     * 1) true(기본값) 설정 시 해당 세션 부재 시 400 에러(Bad Request) 유발함
     * 2) false 설정 시 세션 부재를 허용하며, 이 경우 파라미터에 null을 주입하여 내부 로직 처리를 가능
     * * @param requestDto 클라이언트가 전송한 게시글 데이터 객체
     * @param userId 세션에서 추출한 작성자의 고유 식별자 (비로그인 시 null)
     * * ResponseEntity: HTTP 응답의 상태 코드, 헤더, 본문(Body)을 직접 제어하는 클래스
     * - .status(HttpStatus.CREATED): 응답 상태 코드를 201로 설정하여 자원 생성을 알림
     * - .body("..."): 응답 본문에 포함될 데이터(메시지나 객체)를 설정
     * @return 성공 시 201(Created) 코드 및 성공 메시지 반환
     */
    @PostMapping
    public ResponseEntity<String> write(@Valid @RequestBody BoardRequestDto requestDto,
                                        @SessionAttribute(name = "loginUser", required = false) Long userId) {

        // 세션 유무 확인 및 비로그인 시 401 Unauthorized 반환
        if (userId == null) {
            // .status().body() 구조: 상태 코드와 본문을 단계적으로 설정
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 서비스 계층의 저장 로직 호출
        boardService.write(requestDto, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body("게시글 작성 성공");
    }

    /**
     * [게시글 삭제]
     * * @PathVariable: URL 패턴 내의 {boardId} 위치에 있는 값을 추출하여 변수에 매핑함
     * @param boardId URL 경로에서 추출한 삭제 대상 식별자
     * @param userId 세션에서 추출한 요청자의 고유 식별자
     * * ResponseEntity.ok(): 상태 코드를 200(OK)으로 설정하는 빌더 메서드
     * @return 200(OK): 요청이 성공적으로 처리되었음을 알리는 표준 상태 코드 반환
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> delete(@PathVariable Long boardId,
                                         @SessionAttribute(name = "loginUser", required = false) Long userId) {

        // 로그인 여부 검증 및 미인증 시 401 에러 반환
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }

        // 서비스 계층을 통한 삭제 로직 수행
        boardService.delete(boardId, userId);

        // .ok("..."): 성공 응답을 보내는 가장 간결한 방식
        return ResponseEntity.ok("게시글을 삭제하였습니다.");
    }

    /**
     * [게시글 목록 조회]
     * * @param boardId 경로 변수 {boardId}를 자바 파라미터로 형변환 및 주입
     * * @return 전체 게시글 리스트와 200(OK) 상태 코드 반환
     */
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getBoardList() {

        // 전체 게시글 데이터 조회 수행
        List<BoardResponseDto> allBoards = boardService.getAllBoards();

        return ResponseEntity.ok(allBoards);
    }

    /**
     * [게시글 단건 조회]
     * * @PathVariable: 경로 변수 {boardId}를 자바 파라미터 타입(Long)에 맞춰 형변환 및 주입함
     * @param boardId 경로 패턴에서 추출한 특정 게시글 식별자
     * @return 조회된 게시글 데이터와 200(OK) 상태 코드를 ResponseEntity에 담아 반환
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long boardId) {

        // 특정 게시글 상세 정보 조회 호출
        BoardResponseDto boardResponseDto = boardService.getBoard(boardId);

        return ResponseEntity.ok(boardResponseDto);
    }

    /**
     * [게시글 수정]
     * @param boardId 수정할 게시글 번호
     * @param requestDto 수정 데이터 객체
     * @param userId 수정 요청자 ID
     * @return 수정 완료 메시지와 200(OK) 상태 코드를 담은 ResponseEntity 반환
     */
    @PutMapping("/{boardId}")
    public ResponseEntity<String> edit(@PathVariable Long boardId,
                                       @Valid @RequestBody BoardRequestDto requestDto,
                                       @SessionAttribute(name = "loginUser", required = false) Long userId) {

        // 로그인 인증 상태 확인 및 실패 시 401 반환
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 서비스 계층에 수정 권한 확인 및 업데이트 위임
        boardService.edit(boardId, requestDto, userId);

        return ResponseEntity.ok("게시글이 수정되었습니다.");
    }
}