package com.board.messycode;

import com.board.messycode.entity.Post;
import com.board.messycode.repository.PostLikeRepository;
import com.board.messycode.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LikeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    // === 좋아요 테스트 ===

    @Test
    void 좋아요_성공() throws Exception {
        Post post = createTestPost();
        String guestId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void 좋아요_여러명() throws Exception {
        Post post = createTestPost();
        String guestId1 = UUID.randomUUID().toString();
        String guestId2 = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.liked").value(true));

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(2))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void 좋아요_중복_409() throws Exception {
        Post post = createTestPost();
        String guestId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 좋아요_헤더_없으면_400() throws Exception {
        Post post = createTestPost();

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 좋아요_게시글_없으면_404() throws Exception {
        String guestId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/posts/9999/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 좋아요 취소 테스트 ===

    @Test
    void 좋아요_취소_성공() throws Exception {
        Post post = createTestPost();
        String guestId = UUID.randomUUID().toString();

        // 먼저 좋아요
        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk());

        // 좋아요 취소
        mockMvc.perform(delete("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    void 좋아요_취소_좋아요하지_않은_상태_409() throws Exception {
        Post post = createTestPost();
        String guestId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 좋아요_취소_헤더_없으면_400() throws Exception {
        Post post = createTestPost();

        mockMvc.perform(delete("/api/posts/" + post.getId() + "/likes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 좋아요_취소_게시글_없으면_404() throws Exception {
        String guestId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/posts/9999/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 게시글 상세 조회 liked 필드 테스트 ===

    @Test
    void 게시글_상세_좋아요한_상태에서_liked_true() throws Exception {
        Post post = createTestPost();
        String guestId = UUID.randomUUID().toString();

        // 좋아요
        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk());

        // 게시글 상세 조회
        mockMvc.perform(get("/api/posts/" + post.getId())
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    void 게시글_상세_좋아요하지_않은_상태에서_liked_false() throws Exception {
        Post post = createTestPost();
        String guestId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/posts/" + post.getId())
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    void 게시글_상세_헤더_없으면_liked_false() throws Exception {
        Post post = createTestPost();

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    void 게시글_상세_다른_사용자_좋아요는_liked_false() throws Exception {
        Post post = createTestPost();
        String guestId1 = UUID.randomUUID().toString();
        String guestId2 = UUID.randomUUID().toString();

        // guestId1이 좋아요
        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId1))
                .andExpect(status().isOk());

        // guestId2로 조회 -> liked는 false, likeCount는 1
        mockMvc.perform(get("/api/posts/" + post.getId())
                        .header("X-Guest-Id", guestId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    // === 게시글 삭제 시 좋아요 데이터 삭제 테스트 ===

    @Test
    void 게시글_삭제시_좋아요_데이터도_삭제() throws Exception {
        Post post = createTestPost();
        String guestId = UUID.randomUUID().toString();

        // 좋아요
        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .header("X-Guest-Id", guestId))
                .andExpect(status().isOk());

        // 게시글 삭제
        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(java.util.Map.of("password", "1234"))))
                .andExpect(status().isNoContent());

        // 게시글 확인
        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isNotFound());
    }

    // === 헬퍼 메서드 ===

    private Post createTestPost() {
        Post post = new Post();
        post.setTitle("테스트 제목");
        post.setContent("테스트 내용");
        post.setAuthor("작성자");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        post.setPassword(encoder.encode("1234"));
        post.setHashtags(new ArrayList<>());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }
}
