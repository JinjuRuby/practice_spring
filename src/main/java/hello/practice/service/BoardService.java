package hello.practice.service;

import hello.practice.dto.BoardRequestDto;
import hello.practice.dto.BoardResponseDto;
import hello.practice.entity.Board;
import hello.practice.entity.User;
import hello.practice.repository.BoardRepository;
import hello.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글(Board)과 관련된 비즈니스 로직을 처리하는 서비스 클래스
 * - Controller와 Repository 사이에서 '중간 다리' 역할을 수행함.
 * - 데이터 가공(DTO <-> Entity), 트랜잭션 관리, 예외 처리, 권한 검사 등을 담당.
 */
@Service // 1. 스프링 컨테이너에 이 클래스를 '빈(Bean)'으로 등록 (싱글톤 객체로 관리됨)
@RequiredArgsConstructor // 2. final이 붙은 필드(boardRepository)를 매개변수로 받는 생성자를 자동 생성 (의존성 주입 해결)
@Transactional(readOnly = true) // 3. 기본적으로 모든 메서드에 '읽기 전용 트랜잭션'을 적용 (조회 성능 최적화)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /**
     * [게시글 작성]
     * 1. 요청 받은 데이터(DTO)를 엔티티(Entity)로 변환한다.
     * 2. DB에 저장한다. (INSERT)
     * 3. 저장된 엔티티를 다시 응답용 DTO로 변환해서 반환한다.
     */
    @Transactional // 쓰기 작업이므로 readOnly = true를 덮어쓰고 '쓰기 모드' 활성화
    public BoardResponseDto write(BoardRequestDto requestDto, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다"));

        Board board = requestDto.toEntity(user);

        // [Step 2] DB 저장 (Repository 호출)
        // save()를 호출하는 순간 영속성 컨텍스트에 저장되고, 트랜잭션이 끝날 때 INSERT SQL이 날아간다.
        boardRepository.save(board);

        // [Step 3] Entity -> DTO 변환 후 반환
        // 엔티티를 그대로 반환하면 내부 정보 노출 등의 문제가 있으므로 DTO로 감싸서 보낸다.
        return BoardResponseDto.from(board);
    }

    /**
     * [게시글 삭제]
     * 1. 삭제할 게시글이 진짜 있는지 확인한다. (없으면 에러)
     * 2. 삭제를 요청한 사람이 '작성자 본인'인지 확인한다. (보안)
     * 3. DB에서 삭제한다. (DELETE)
     */
    @Transactional
    public void delete(Long boardId, Long userId) {

        // [Step 1] 대상 조회 (예외 처리)
        // orElseThrow: 값이 있으면 꺼내오고, 없으면 예외를 발생시킨다. (안전한 처리를 위함)
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 게시글이 존재하지 않습니다. id = " + boardId));

        // [Step 2] 권한 검증 (본인 확인)
        // 게시글에 저장된 작성자 ID(board.getUser().getId())와
        // 로그인한 유저 ID(user.getId())가 다르면 예외를 터뜨린다.
        if (!board.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        // [Step 3] 삭제 수행
        // JPA가 DELETE SQL을 생성하여 DB에 전달한다.
        boardRepository.delete(board);
    }

    /**
     * [게시글 수정]
     * 핵심 기술: 변경 감지 (Dirty Checking)
     * 1. 게시글을 조회해서 영속성 컨텍스트에 올린다.
     * 2. 권한을 확인한다.
     * 3. 객체의 값만 바꾼다. (update 쿼리를 날리지 않음!)
     * 4. 메서드가 끝날 때, JPA가 변경된 점을 감지하고 알아서 UPDATE SQL을 날려준다.
     */
    @Transactional
    public BoardResponseDto edit(Long boardId, BoardRequestDto requestDto, Long userId) {

        // [Step 1] 영속성 컨텍스트에 Entity 로딩
        // 이 순간부터 JPA는 이 'board' 객체를 감시하기 시작한다. (스냅샷 생성)
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 게시글이 존재하지 않습니다. id = " + boardId));

        // [Step 2] 권한 검증
        if (!board.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // [Step 3] 데이터 변경 (핵심!)
        // 서비스는 단순히 객체의 필드값만 수정한다. (board.update() 메서드 호출)
        // repository.save()를 호출하지 않는 것이 포인트.
        board.update(requestDto.getTitle(), requestDto.getContent());

        // [Step 4] 트랜잭션 종료 & 자동 커밋
        // 메서드가 끝나면 트랜잭션이 커밋된다.
        // 이때 JPA는 "어? 처음에 조회했을 때랑 값이 달라졌네?"라고 감지하고 UPDATE 쿼리를 자동으로 DB에 보낸다.
        return BoardResponseDto.from(board);
    }

    /**
     * [게시글 단건 조회]
     * - @Transactional(readOnly = true)가 적용되어 있다.
     * - JPA가 '변경 감지'를 위한 스냅샷을 만들지 않아 메모리가 절약되고 속도가 빠르다.
     */
    public BoardResponseDto getBoard(Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id = " + boardId));

        return BoardResponseDto.from(board);
    }

    /**
     * [게시글 전체 조회]
     * - DB에 있는 모든 글을 가져와서 DTO 리스트로 변환한다.
     */
    public List<BoardResponseDto> getAllBoards() {

        // [Step 1] 결과를 담을 빈 리스트(바구니) 준비
        List<BoardResponseDto> responseDtos = new ArrayList<>();

        // [Step 2] DB 조회 (Entity 리스트 가져오기)
        List<Board> boards = boardRepository.findAllByOrderByIdDesc();

        // [Step 3] Entity -> DTO 변환 (반복문)
        // Entity를 그대로 컨트롤러에 넘기면 안 되므로 하나씩 꺼내서 DTO로 포장한다.
        for (Board board : boards) {
            responseDtos.add(BoardResponseDto.from(board));
        }

        return responseDtos;
    }

    /**
     * [제목 검색]
     * - 제목에 특정 단어가 포함된 글을 찾는다.
     */
    public List<BoardResponseDto> getBoardsByTitle(String title) {

        List<BoardResponseDto> responseDtos = new ArrayList<>(); // 타입 명시는 생략 가능 (<>)

        // Repository에 만들어둔 검색 메서드 호출 (LIKE %title%)
        List<Board> boards = boardRepository.findAllByTitleContainingOrderByIdDesc(title);

        for (Board board : boards) {
            responseDtos.add(BoardResponseDto.from(board));
        }

        return responseDtos;
    }

    /**
     * [작성자 검색]
     * - 작성자 닉네임에 특정 단어가 포함된 글을 찾는다.
     */
    public List<BoardResponseDto> getBoardsByUsername(String username) {

        List<BoardResponseDto> responseDtos = new ArrayList<>();

        // User 테이블과 조인하여 검색 (LIKE %username%)
        List<Board> boards = boardRepository.findAllByUser_UsernameContainingOrderByIdDesc(username);

        for (Board board : boards) {
            responseDtos.add(BoardResponseDto.from(board));
        }

        return responseDtos;
    }
}