package com.board.cleancode.adapter.in.web;

import com.board.cleancode.adapter.in.web.dto.ChangePasswordRequest;
import com.board.cleancode.adapter.in.web.dto.CreatePostRequest;
import com.board.cleancode.adapter.in.web.dto.DeletePostRequest;
import com.board.cleancode.adapter.in.web.dto.UpdatePostRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/posts";
    private static final String PASSWORD = "test1234";

    private Long createTestPost() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", "작성자", PASSWORD, List.of("태그1", "태그2")
        );
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Nested
    @DisplayName("POST /api/posts - 게시글 작성")
    class CreatePost {

        @Test
        @DisplayName("정상적으로 게시글을 작성한다")
        void createPost_success() throws Exception {
            CreatePostRequest request = new CreatePostRequest(
                    "제목입니다", "내용입니다", "작성자", PASSWORD, List.of("태그1", "태그2")
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("제목입니다"))
                    .andExpect(jsonPath("$.content").value("내용입니다"))
                    .andExpect(jsonPath("$.author").value("작성자"))
                    .andExpect(jsonPath("$.hashtags", hasSize(2)))
                    .andExpect(jsonPath("$.hashtags[0]").value("태그1"))
                    .andExpect(jsonPath("$.viewCount").value(0))
                    .andExpect(jsonPath("$.likeCount").value(0))
                    .andExpect(jsonPath("$.commentCount").value(0))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.updatedAt").isNotEmpty());
        }

        @Test
        @DisplayName("해시태그 없이 게시글을 작성한다")
        void createPost_withoutHashtags() throws Exception {
            CreatePostRequest request = new CreatePostRequest(
                    "제목입니다", "내용입니다", "작성자", PASSWORD, null
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.hashtags", hasSize(0)));
        }

        @Test
        @DisplayName("제목이 비어있으면 400을 반환한다")
        void createPost_emptyTitle_returns400() throws Exception {
            CreatePostRequest request = new CreatePostRequest(
                    "", "내용", "작성자", PASSWORD, null
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호가 4자 미만이면 400을 반환한다")
        void createPost_shortPassword_returns400() throws Exception {
            CreatePostRequest request = new CreatePostRequest(
                    "제목", "내용", "작성자", "abc", null
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("해시태그가 5개를 초과하면 400을 반환한다")
        void createPost_tooManyHashtags_returns400() throws Exception {
            CreatePostRequest request = new CreatePostRequest(
                    "제목", "내용", "작성자", PASSWORD,
                    List.of("태그1", "태그2", "태그3", "태그4", "태그5", "태그6")
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{id} - 게시글 상세 조회")
    class GetPost {

        @Test
        @DisplayName("게시글을 정상 조회하고 조회수가 1 증가한다")
        void getPost_success() throws Exception {
            Long postId = createTestPost();

            mockMvc.perform(get(BASE_URL + "/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(postId))
                    .andExpect(jsonPath("$.title").value("테스트 제목"))
                    .andExpect(jsonPath("$.viewCount").value(1))
                    .andExpect(jsonPath("$.likeCount").value(0))
                    .andExpect(jsonPath("$.commentCount").value(0));
        }

        @Test
        @DisplayName("두 번 조회하면 조회수가 2가 된다")
        void getPost_viewCountIncrements() throws Exception {
            Long postId = createTestPost();

            mockMvc.perform(get(BASE_URL + "/{id}", postId))
                    .andExpect(status().isOk());

            mockMvc.perform(get(BASE_URL + "/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.viewCount").value(2));
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 404를 반환한다")
        void getPost_notFound() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/posts/{id} - 게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("비밀번호가 일치하면 게시글을 수정한다")
        void updatePost_success() throws Exception {
            Long postId = createTestPost();

            UpdatePostRequest request = new UpdatePostRequest(
                    "수정된 제목", "수정된 내용", PASSWORD, List.of("새태그")
            );

            mockMvc.perform(put(BASE_URL + "/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.content").value("수정된 내용"))
                    .andExpect(jsonPath("$.hashtags", hasSize(1)))
                    .andExpect(jsonPath("$.hashtags[0]").value("새태그"));
        }

        @Test
        @DisplayName("비밀번호가 불일치하면 403을 반환한다")
        void updatePost_wrongPassword_returns403() throws Exception {
            Long postId = createTestPost();

            UpdatePostRequest request = new UpdatePostRequest(
                    "수정된 제목", "수정된 내용", "wrongpass", List.of("새태그")
            );

            mockMvc.perform(put(BASE_URL + "/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정 시 404를 반환한다")
        void updatePost_notFound() throws Exception {
            UpdatePostRequest request = new UpdatePostRequest(
                    "수정된 제목", "수정된 내용", PASSWORD, null
            );

            mockMvc.perform(put(BASE_URL + "/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{id} - 게시글 삭제")
    class DeletePost {

        @Test
        @DisplayName("비밀번호가 일치하면 게시글을 삭제한다")
        void deletePost_success() throws Exception {
            Long postId = createTestPost();

            DeletePostRequest request = new DeletePostRequest(PASSWORD);

            mockMvc.perform(delete(BASE_URL + "/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            // 삭제 후 조회하면 404
            mockMvc.perform(get(BASE_URL + "/{id}", postId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("비밀번호가 불일치하면 403을 반환한다")
        void deletePost_wrongPassword_returns403() throws Exception {
            Long postId = createTestPost();

            DeletePostRequest request = new DeletePostRequest("wrongpass");

            mockMvc.perform(delete(BASE_URL + "/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 404를 반환한다")
        void deletePost_notFound() throws Exception {
            DeletePostRequest request = new DeletePostRequest(PASSWORD);

            mockMvc.perform(delete(BASE_URL + "/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/posts/{id}/password - 게시글 비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("현재 비밀번호가 일치하면 비밀번호를 변경한다")
        void changePassword_success() throws Exception {
            Long postId = createTestPost();
            String newPassword = "newpass1234";

            ChangePasswordRequest request = new ChangePasswordRequest(PASSWORD, newPassword);

            mockMvc.perform(patch(BASE_URL + "/{id}/password", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("비밀번호 변경 후 새 비밀번호로 게시글을 수정할 수 있다")
        void changePassword_thenUpdateWithNewPassword() throws Exception {
            Long postId = createTestPost();
            String newPassword = "newpass1234";

            // 비밀번호 변경
            ChangePasswordRequest changeRequest = new ChangePasswordRequest(PASSWORD, newPassword);
            mockMvc.perform(patch(BASE_URL + "/{id}/password", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andExpect(status().isOk());

            // 새 비밀번호로 게시글 수정
            UpdatePostRequest updateRequest = new UpdatePostRequest(
                    "수정된 제목", "수정된 내용", newPassword, null
            );
            mockMvc.perform(put(BASE_URL + "/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("수정된 제목"));
        }

        @Test
        @DisplayName("비밀번호 변경 후 새 비밀번호로 게시글을 삭제할 수 있다")
        void changePassword_thenDeleteWithNewPassword() throws Exception {
            Long postId = createTestPost();
            String newPassword = "newpass1234";

            // 비밀번호 변경
            ChangePasswordRequest changeRequest = new ChangePasswordRequest(PASSWORD, newPassword);
            mockMvc.perform(patch(BASE_URL + "/{id}/password", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andExpect(status().isOk());

            // 새 비밀번호로 게시글 삭제
            DeletePostRequest deleteRequest = new DeletePostRequest(newPassword);
            mockMvc.perform(delete(BASE_URL + "/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deleteRequest)))
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get(BASE_URL + "/{id}", postId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("현재 비밀번호가 불일치하면 403을 반환한다")
        void changePassword_wrongCurrentPassword_returns403() throws Exception {
            Long postId = createTestPost();

            ChangePasswordRequest request = new ChangePasswordRequest("wrongpass", "newpass1234");

            mockMvc.perform(patch(BASE_URL + "/{id}/password", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("새 비밀번호가 4자 미만이면 400을 반환한다")
        void changePassword_shortNewPassword_returns400() throws Exception {
            Long postId = createTestPost();

            ChangePasswordRequest request = new ChangePasswordRequest(PASSWORD, "abc");

            mockMvc.perform(patch(BASE_URL + "/{id}/password", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 비밀번호 변경 시 404를 반환한다")
        void changePassword_notFound() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(PASSWORD, "newpass1234");

            mockMvc.perform(patch(BASE_URL + "/{id}/password", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
