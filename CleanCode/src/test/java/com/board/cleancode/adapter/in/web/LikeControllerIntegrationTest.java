package com.board.cleancode.adapter.in.web;

import com.board.cleancode.adapter.in.web.dto.CreatePostRequest;
import com.board.cleancode.adapter.in.web.dto.DeletePostRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LikeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PASSWORD = "test1234";

    private Long createTestPost() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", "작성자", PASSWORD, List.of("태그1")
        );
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Nested
    @DisplayName("POST /api/posts/{postId}/likes - 좋아요")
    class LikePost {

        @Test
        @DisplayName("정상적으로 좋아요한다")
        void likePost_success() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(1))
                    .andExpect(jsonPath("$.liked").value(true));
        }

        @Test
        @DisplayName("여러 사용자가 좋아요하면 likeCount가 증가한다")
        void likePost_multipleGuests() throws Exception {
            Long postId = createTestPost();
            String guestId1 = UUID.randomUUID().toString();
            String guestId2 = UUID.randomUUID().toString();

            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(1));

            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(2));
        }

        @Test
        @DisplayName("이미 좋아요한 상태에서 재요청 시 409를 반환한다")
        void likePost_duplicate_returns409() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("X-Guest-Id 헤더가 없으면 400을 반환한다")
        void likePost_missingGuestId_returns400() throws Exception {
            Long postId = createTestPost();

            mockMvc.perform(post("/api/posts/{postId}/likes", postId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 좋아요 시 404를 반환한다")
        void likePost_postNotFound_returns404() throws Exception {
            String guestId = UUID.randomUUID().toString();

            mockMvc.perform(post("/api/posts/{postId}/likes", 999L)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{postId}/likes - 좋아요 취소")
    class UnlikePost {

        @Test
        @DisplayName("정상적으로 좋아요를 취소한다")
        void unlikePost_success() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            // 먼저 좋아요
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk());

            // 좋아요 취소
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(0))
                    .andExpect(jsonPath("$.liked").value(false));
        }

        @Test
        @DisplayName("좋아요하지 않은 상태에서 취소 시 409를 반환한다")
        void unlikePost_notLiked_returns409() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("X-Guest-Id 헤더가 없으면 400을 반환한다")
        void unlikePost_missingGuestId_returns400() throws Exception {
            Long postId = createTestPost();

            mockMvc.perform(delete("/api/posts/{postId}/likes", postId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 좋아요 취소 시 404를 반환한다")
        void unlikePost_postNotFound_returns404() throws Exception {
            String guestId = UUID.randomUUID().toString();

            mockMvc.perform(delete("/api/posts/{postId}/likes", 999L)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{id} - 게시글 상세에서 liked 필드")
    class GetPostWithLikeInfo {

        @Test
        @DisplayName("좋아요한 게시글 상세 조회 시 liked가 true이다")
        void getPost_liked_true() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            // 좋아요
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk());

            // 상세 조회
            mockMvc.perform(get("/api/posts/{id}", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(1))
                    .andExpect(jsonPath("$.liked").value(true));
        }

        @Test
        @DisplayName("좋아요하지 않은 게시글 상세 조회 시 liked가 false이다")
        void getPost_liked_false() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            mockMvc.perform(get("/api/posts/{id}", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(0))
                    .andExpect(jsonPath("$.liked").value(false));
        }

        @Test
        @DisplayName("X-Guest-Id 헤더가 없으면 liked가 false이다")
        void getPost_noGuestId_liked_false() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            // 좋아요
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk());

            // X-Guest-Id 없이 상세 조회
            mockMvc.perform(get("/api/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(1))
                    .andExpect(jsonPath("$.liked").value(false));
        }

        @Test
        @DisplayName("다른 사용자의 좋아요는 liked에 영향을 주지 않는다")
        void getPost_otherGuestLike_liked_false() throws Exception {
            Long postId = createTestPost();
            String guestId1 = UUID.randomUUID().toString();
            String guestId2 = UUID.randomUUID().toString();

            // guestId1이 좋아요
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId1))
                    .andExpect(status().isOk());

            // guestId2로 상세 조회 - likeCount는 1이지만 liked는 false
            mockMvc.perform(get("/api/posts/{id}", postId)
                            .header("X-Guest-Id", guestId2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.likeCount").value(1))
                    .andExpect(jsonPath("$.liked").value(false));
        }
    }

    @Nested
    @DisplayName("게시글 삭제 시 좋아요 데이터 삭제")
    class DeletePostWithLikes {

        @Test
        @DisplayName("게시글 삭제 시 연관된 좋아요 데이터도 함께 삭제된다")
        void deletePost_deletesLikes() throws Exception {
            Long postId = createTestPost();
            String guestId = UUID.randomUUID().toString();

            // 좋아요
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk());

            // 게시글 삭제
            DeletePostRequest deleteRequest = new DeletePostRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deleteRequest)))
                    .andExpect(status().isNoContent());

            // 삭제된 게시글에 좋아요 시 404
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isNotFound());
        }
    }
}
