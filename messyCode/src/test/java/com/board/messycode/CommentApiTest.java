package com.board.messycode;

import com.board.messycode.entity.Comment;
import com.board.messycode.entity.Post;
import com.board.messycode.repository.CommentRepository;
import com.board.messycode.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    // === 댓글 작성 테스트 ===

    @Test
    void 댓글_작성_성공() throws Exception {
        Post post = createTestPost();

        Map<String, Object> request = Map.of(
                "author", "댓글작성자",
                "password", "1234",
                "content", "댓글 내용입니다."
        );

        mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.author").value("댓글작성자"))
                .andExpect(jsonPath("$.content").value("댓글 내용입니다."))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.deleted").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 댓글_작성_존재하지_않는_게시글_404() throws Exception {
        Map<String, Object> request = Map.of(
                "author", "댓글작성자",
                "password", "1234",
                "content", "댓글 내용"
        );

        mockMvc.perform(post("/api/posts/9999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 댓글_작성_비밀번호_짧으면_400() throws Exception {
        Post post = createTestPost();

        Map<String, Object> request = Map.of(
                "author", "댓글작성자",
                "password", "123",
                "content", "댓글 내용"
        );

        mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 댓글_작성_내용_없으면_400() throws Exception {
        Post post = createTestPost();

        Map<String, Object> request = Map.of(
                "author", "댓글작성자",
                "password", "1234"
        );

        mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 댓글 목록 조회 테스트 ===

    @Test
    void 댓글_목록_조회_성공() throws Exception {
        Post post = createTestPost();
        createTestComment(post.getId(), "작성자1", "댓글 내용1");
        createTestComment(post.getId(), "작성자2", "댓글 내용2");

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void 댓글_목록_조회_최신순_정렬() throws Exception {
        Post post = createTestPost();
        Comment c1 = createTestComment(post.getId(), "작성자1", "첫번째 댓글");
        Comment c2 = createTestComment(post.getId(), "작성자2", "두번째 댓글");

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(c2.getId()))
                .andExpect(jsonPath("$.content[1].id").value(c1.getId()));
    }

    @Test
    void 댓글_목록_조회_페이징_5건() throws Exception {
        Post post = createTestPost();
        for (int i = 0; i < 7; i++) {
            createTestComment(post.getId(), "작성자", "댓글" + i);
        }

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.hasMore").value(true));

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void 댓글_목록_조회_삭제된_댓글_내용_마스킹() throws Exception {
        Post post = createTestPost();
        Comment comment = createTestComment(post.getId(), "작성자", "원본 내용");
        comment.setDeleted(true);
        commentRepository.save(comment);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("삭제된 댓글입니다."))
                .andExpect(jsonPath("$.content[0].deleted").value(true));
    }

    @Test
    void 댓글_목록_조회_존재하지_않는_게시글_404() throws Exception {
        mockMvc.perform(get("/api/posts/9999/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 댓글 삭제 테스트 ===

    @Test
    void 댓글_삭제_성공_소프트삭제() throws Exception {
        Post post = createTestPost();
        Comment comment = createTestComment(post.getId(), "작성자", "댓글 내용");

        Map<String, String> request = Map.of("password", "1234");

        mockMvc.perform(delete("/api/posts/" + post.getId() + "/comments/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // 소프트 삭제 확인 - 댓글은 여전히 존재하지만 deleted=true
        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].deleted").value(true))
                .andExpect(jsonPath("$.content[0].content").value("삭제된 댓글입니다."));
    }

    @Test
    void 댓글_삭제_비밀번호_불일치_403() throws Exception {
        Post post = createTestPost();
        Comment comment = createTestComment(post.getId(), "작성자", "댓글 내용");

        Map<String, String> request = Map.of("password", "wrong");

        mockMvc.perform(delete("/api/posts/" + post.getId() + "/comments/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 댓글_삭제_존재하지_않는_댓글_404() throws Exception {
        Post post = createTestPost();

        Map<String, String> request = Map.of("password", "1234");

        mockMvc.perform(delete("/api/posts/" + post.getId() + "/comments/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 댓글_삭제_존재하지_않는_게시글_404() throws Exception {
        Map<String, String> request = Map.of("password", "1234");

        mockMvc.perform(delete("/api/posts/9999/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 게시글 상세 조회에 댓글 포함 테스트 ===

    @Test
    void 게시글_상세_조회_댓글_포함() throws Exception {
        Post post = createTestPost();
        createTestComment(post.getId(), "댓글작성자1", "댓글1");
        createTestComment(post.getId(), "댓글작성자2", "댓글2");

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.commentCount").value(2))
                .andExpect(jsonPath("$.comments").exists())
                .andExpect(jsonPath("$.comments.content").isArray())
                .andExpect(jsonPath("$.comments.content.length()").value(2))
                .andExpect(jsonPath("$.comments.page").value(0))
                .andExpect(jsonPath("$.comments.size").value(5))
                .andExpect(jsonPath("$.comments.hasMore").value(false));
    }

    @Test
    void 게시글_상세_조회_댓글_최대_5건() throws Exception {
        Post post = createTestPost();
        for (int i = 0; i < 7; i++) {
            createTestComment(post.getId(), "작성자", "댓글" + i);
        }

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentCount").value(7))
                .andExpect(jsonPath("$.comments.content.length()").value(5))
                .andExpect(jsonPath("$.comments.totalElements").value(7))
                .andExpect(jsonPath("$.comments.hasMore").value(true));
    }

    @Test
    void 게시글_상세_조회_삭제된_댓글_마스킹() throws Exception {
        Post post = createTestPost();
        Comment comment = createTestComment(post.getId(), "작성자", "원본 내용");
        comment.setDeleted(true);
        commentRepository.save(comment);

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.content[0].content").value("삭제된 댓글입니다."))
                .andExpect(jsonPath("$.comments.content[0].deleted").value(true));
    }

    @Test
    void 게시글_상세_조회_삭제된_댓글_commentCount_제외() throws Exception {
        Post post = createTestPost();
        createTestComment(post.getId(), "작성자1", "댓글1");
        Comment deleted = createTestComment(post.getId(), "작성자2", "댓글2");
        deleted.setDeleted(true);
        commentRepository.save(deleted);

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentCount").value(1));
    }

    @Test
    void 게시글_삭제시_댓글도_삭제() throws Exception {
        Post post = createTestPost();
        createTestComment(post.getId(), "작성자", "댓글");

        Map<String, String> request = Map.of("password", "1234");

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // 게시글 삭제 확인
        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isNotFound());
    }

    // === 답글(대댓글) 테스트 ===

    @Test
    void 답글_작성_성공() throws Exception {
        Post post = createTestPost();
        Comment parentComment = createTestComment(post.getId(), "부모작성자", "부모 댓글");

        Map<String, Object> request = Map.of(
                "author", "답글작성자",
                "password", "1234",
                "content", "답글 내용입니다."
        );

        mockMvc.perform(post("/api/posts/" + post.getId() + "/comments/" + parentComment.getId() + "/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.parentId").value(parentComment.getId()))
                .andExpect(jsonPath("$.author").value("답글작성자"))
                .andExpect(jsonPath("$.content").value("답글 내용입니다."))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.deleted").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 답글에_답글_시도시_400() throws Exception {
        Post post = createTestPost();
        Comment parentComment = createTestComment(post.getId(), "부모작성자", "부모 댓글");
        Comment reply = createTestReply(post.getId(), parentComment.getId(), "답글작성자", "답글 내용");

        Map<String, Object> request = Map.of(
                "author", "대대댓글작성자",
                "password", "1234",
                "content", "대대댓글 내용"
        );

        mockMvc.perform(post("/api/posts/" + post.getId() + "/comments/" + reply.getId() + "/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 답글_목록_조회_오래된순_정렬() throws Exception {
        Post post = createTestPost();
        Comment parentComment = createTestComment(post.getId(), "부모작성자", "부모 댓글");
        Comment reply1 = createTestReply(post.getId(), parentComment.getId(), "답글1", "첫번째 답글");
        Comment reply2 = createTestReply(post.getId(), parentComment.getId(), "답글2", "두번째 답글");

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments/" + parentComment.getId() + "/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(reply1.getId()))
                .andExpect(jsonPath("$.content[1].id").value(reply2.getId()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void 삭제된_부모_댓글에_답글_작성_가능() throws Exception {
        Post post = createTestPost();
        Comment parentComment = createTestComment(post.getId(), "부모작성자", "부모 댓글");
        parentComment.setDeleted(true);
        commentRepository.save(parentComment);

        Map<String, Object> request = Map.of(
                "author", "답글작성자",
                "password", "1234",
                "content", "삭제된 댓글에 답글"
        );

        mockMvc.perform(post("/api/posts/" + post.getId() + "/comments/" + parentComment.getId() + "/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentId").value(parentComment.getId()));
    }

    @Test
    void 댓글_목록_조회시_replyCount_정확성() throws Exception {
        Post post = createTestPost();
        Comment comment1 = createTestComment(post.getId(), "작성자1", "댓글1");
        Comment comment2 = createTestComment(post.getId(), "작성자2", "댓글2");
        createTestReply(post.getId(), comment1.getId(), "답글1", "답글 내용1");
        createTestReply(post.getId(), comment1.getId(), "답글2", "답글 내용2");
        Comment deletedReply = createTestReply(post.getId(), comment1.getId(), "답글3", "삭제될 답글");
        deletedReply.setDeleted(true);
        commentRepository.save(deletedReply);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].replyCount").value(0))
                .andExpect(jsonPath("$.content[1].replyCount").value(2));
    }

    @Test
    void 부모_댓글_삭제후_답글_유지() throws Exception {
        Post post = createTestPost();
        Comment parentComment = createTestComment(post.getId(), "부모작성자", "부모 댓글");
        Comment reply = createTestReply(post.getId(), parentComment.getId(), "답글작성자", "답글 내용");

        // 부모 댓글 삭제
        Map<String, String> deleteRequest = Map.of("password", "1234");
        mockMvc.perform(delete("/api/posts/" + post.getId() + "/comments/" + parentComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNoContent());

        // 답글 조회 - 여전히 존재해야 함
        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments/" + parentComment.getId() + "/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(reply.getId()))
                .andExpect(jsonPath("$.content[0].content").value("답글 내용"));
    }

    @Test
    void 댓글_목록에_일반_댓글만_표시() throws Exception {
        Post post = createTestPost();
        Comment comment1 = createTestComment(post.getId(), "작성자1", "일반 댓글1");
        Comment comment2 = createTestComment(post.getId(), "작성자2", "일반 댓글2");
        createTestReply(post.getId(), comment1.getId(), "답글작성자", "답글 내용");

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void 댓글_목록에_parentId_포함() throws Exception {
        Post post = createTestPost();
        createTestComment(post.getId(), "작성자", "일반 댓글");

        mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].parentId").value((Object) null));
    }

    @Test
    void 게시글_상세_commentCount에_답글_포함() throws Exception {
        Post post = createTestPost();
        Comment comment = createTestComment(post.getId(), "작성자", "일반 댓글");
        createTestReply(post.getId(), comment.getId(), "답글작성자", "답글 내용");

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentCount").value(2));
    }

    @Test
    void 게시글_상세_댓글에_replyCount_포함() throws Exception {
        Post post = createTestPost();
        Comment comment = createTestComment(post.getId(), "작성자", "일반 댓글");
        createTestReply(post.getId(), comment.getId(), "답글작성자1", "답글 내용1");
        createTestReply(post.getId(), comment.getId(), "답글작성자2", "답글 내용2");

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.content[0].replyCount").value(2));
    }

    // === 헬퍼 메서드 ===

    private Post createTestPost() {
        Post post = new Post();
        post.setTitle("테스트 제목");
        post.setContent("테스트 내용");
        post.setAuthor("작성자");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        post.setPassword(encoder.encode("1234"));
        post.setHashtags(new ArrayList<>(List.of("태그1", "태그2")));
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    private Comment createTestComment(Long postId, String author, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthor(author);
        comment.setContent(content);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        comment.setPassword(encoder.encode("1234"));
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);
        return commentRepository.save(comment);
    }

    private Comment createTestReply(Long postId, Long parentId, String author, String content) {
        Comment reply = new Comment();
        reply.setPostId(postId);
        reply.setParentId(parentId);
        reply.setAuthor(author);
        reply.setContent(content);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        reply.setPassword(encoder.encode("1234"));
        reply.setCreatedAt(LocalDateTime.now());
        reply.setDeleted(false);
        return commentRepository.save(reply);
    }
}
