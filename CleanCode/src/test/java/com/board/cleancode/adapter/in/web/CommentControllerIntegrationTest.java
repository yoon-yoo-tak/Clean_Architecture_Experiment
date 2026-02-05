package com.board.cleancode.adapter.in.web;

import com.board.cleancode.adapter.in.web.dto.CreateCommentRequest;
import com.board.cleancode.adapter.in.web.dto.CreatePostRequest;
import com.board.cleancode.adapter.in.web.dto.DeleteCommentRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerIntegrationTest {

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

    private Long createTestComment(Long postId) throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("댓글작성자", PASSWORD, "댓글 내용");
        MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Nested
    @DisplayName("POST /api/posts/{postId}/comments - 댓글 작성")
    class CreateComment {

        @Test
        @DisplayName("정상적으로 댓글을 작성한다")
        void createComment_success() throws Exception {
            Long postId = createTestPost();
            CreateCommentRequest request = new CreateCommentRequest("댓글작성자", PASSWORD, "댓글 내용입니다");

            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.author").value("댓글작성자"))
                    .andExpect(jsonPath("$.content").value("댓글 내용입니다"))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.deleted").value(false));
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 댓글 작성 시 404를 반환한다")
        void createComment_postNotFound() throws Exception {
            CreateCommentRequest request = new CreateCommentRequest("댓글작성자", PASSWORD, "댓글 내용");

            mockMvc.perform(post("/api/posts/{postId}/comments", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("작성자가 비어있으면 400을 반환한다")
        void createComment_emptyAuthor_returns400() throws Exception {
            Long postId = createTestPost();
            CreateCommentRequest request = new CreateCommentRequest("", PASSWORD, "댓글 내용");

            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호가 4자 미만이면 400을 반환한다")
        void createComment_shortPassword_returns400() throws Exception {
            Long postId = createTestPost();
            CreateCommentRequest request = new CreateCommentRequest("댓글작성자", "abc", "댓글 내용");

            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("내용이 비어있으면 400을 반환한다")
        void createComment_emptyContent_returns400() throws Exception {
            Long postId = createTestPost();
            CreateCommentRequest request = new CreateCommentRequest("댓글작성자", PASSWORD, "");

            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/comments - 댓글 목록 조회")
    class GetComments {

        @Test
        @DisplayName("댓글 목록을 정상 조회한다")
        void getComments_success() throws Exception {
            Long postId = createTestPost();
            createTestComment(postId);
            createTestComment(postId);

            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.hasMore").value(false));
        }

        @Test
        @DisplayName("최신순으로 정렬된다")
        void getComments_orderedByLatest() throws Exception {
            Long postId = createTestPost();
            createTestComment(postId);

            // 두 번째 댓글은 다른 내용으로 생성
            CreateCommentRequest request = new CreateCommentRequest("작성자2", PASSWORD, "최신 댓글");
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].content").value("최신 댓글"))
                    .andExpect(jsonPath("$.content[1].content").value("댓글 내용"));
        }

        @Test
        @DisplayName("5건씩 페이징하고 hasMore를 정확히 반환한다")
        void getComments_pagination() throws Exception {
            Long postId = createTestPost();
            for (int i = 0; i < 7; i++) {
                createTestComment(postId);
            }

            // 첫 페이지: 5건, hasMore=true
            mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(5)))
                    .andExpect(jsonPath("$.totalElements").value(7))
                    .andExpect(jsonPath("$.hasMore").value(true));

            // 두 번째 페이지: 2건, hasMore=false
            mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(7))
                    .andExpect(jsonPath("$.hasMore").value(false));
        }

        @Test
        @DisplayName("삭제된 댓글은 content가 마스킹되어 반환된다")
        void getComments_deletedCommentMasked() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 댓글 삭제
            DeleteCommentRequest deleteRequest = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequest)));

            // 목록 조회 시 삭제된 댓글의 content가 마스킹됨
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].content").value("삭제된 댓글입니다."))
                    .andExpect(jsonPath("$.content[0].deleted").value(true));
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 댓글 조회 시 404를 반환한다")
        void getComments_postNotFound() throws Exception {
            mockMvc.perform(get("/api/posts/{postId}/comments", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{postId}/comments/{commentId} - 댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("비밀번호가 일치하면 댓글을 소프트 삭제한다")
        void deleteComment_success() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            DeleteCommentRequest request = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            // 삭제 후에도 목록에 나타나지만 content가 마스킹됨
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].deleted").value(true))
                    .andExpect(jsonPath("$.content[0].content").value("삭제된 댓글입니다."));
        }

        @Test
        @DisplayName("비밀번호가 불일치하면 403을 반환한다")
        void deleteComment_wrongPassword_returns403() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            DeleteCommentRequest request = new DeleteCommentRequest("wrongpass");
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시 404를 반환한다")
        void deleteComment_commentNotFound() throws Exception {
            Long postId = createTestPost();

            DeleteCommentRequest request = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 댓글 삭제 시 404를 반환한다")
        void deleteComment_postNotFound() throws Exception {
            DeleteCommentRequest request = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 999L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/posts/{postId}/comments/{commentId}/replies - 답글 작성")
    class CreateReply {

        @Test
        @DisplayName("정상적으로 답글을 작성한다")
        void createReply_success() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);
            CreateCommentRequest request = new CreateCommentRequest("답글작성자", PASSWORD, "답글 내용입니다");

            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.parentId").value(commentId))
                    .andExpect(jsonPath("$.author").value("답글작성자"))
                    .andExpect(jsonPath("$.content").value("답글 내용입니다"))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.deleted").value(false));
        }

        @Test
        @DisplayName("답글에 답글을 시도하면 400을 반환한다")
        void createReply_nestedReply_returns400() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 첫 번째 답글 작성
            CreateCommentRequest replyRequest = new CreateCommentRequest("답글작성자", PASSWORD, "답글 내용");
            MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();
            Long replyId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            // 답글에 답글 시도
            CreateCommentRequest nestedReplyRequest = new CreateCommentRequest("중첩답글작성자", PASSWORD, "중첩 답글");
            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, replyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nestedReplyRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("삭제된 부모 댓글에도 답글 작성이 가능하다")
        void createReply_deletedParent_success() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 부모 댓글 삭제
            DeleteCommentRequest deleteRequest = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deleteRequest)))
                    .andExpect(status().isNoContent());

            // 삭제된 댓글에 답글 작성
            CreateCommentRequest replyRequest = new CreateCommentRequest("답글작성자", PASSWORD, "답글 내용");
            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.parentId").value(commentId));
        }

        @Test
        @DisplayName("존재하지 않는 부모 댓글에 답글 작성 시 404를 반환한다")
        void createReply_parentNotFound_returns404() throws Exception {
            Long postId = createTestPost();
            CreateCommentRequest request = new CreateCommentRequest("답글작성자", PASSWORD, "답글 내용");

            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/comments/{commentId}/replies - 답글 목록 조회")
    class GetReplies {

        @Test
        @DisplayName("답글 목록을 오래된순으로 정렬하여 조회한다")
        void getReplies_orderedByOldest() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 첫 번째 답글
            CreateCommentRequest request1 = new CreateCommentRequest("작성자1", PASSWORD, "첫 번째 답글");
            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)));

            // 두 번째 답글
            CreateCommentRequest request2 = new CreateCommentRequest("작성자2", PASSWORD, "두 번째 답글");
            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)));

            // 오래된순 정렬 확인
            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].content").value("첫 번째 답글"))
                    .andExpect(jsonPath("$.content[1].content").value("두 번째 답글"))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.hasMore").value(false));
        }

        @Test
        @DisplayName("삭제된 답글은 content가 마스킹되어 반환된다")
        void getReplies_deletedReplyMasked() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 답글 작성
            CreateCommentRequest request = new CreateCommentRequest("답글작성자", PASSWORD, "답글 내용");
            MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            Long replyId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            // 답글 삭제
            DeleteCommentRequest deleteRequest = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, replyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deleteRequest)))
                    .andExpect(status().isNoContent());

            // 답글 목록에서 마스킹 확인
            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].content").value("삭제된 댓글입니다."))
                    .andExpect(jsonPath("$.content[0].deleted").value(true));
        }

        @Test
        @DisplayName("페이징이 정상 동작한다")
        void getReplies_pagination() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 7개의 답글 생성
            for (int i = 0; i < 7; i++) {
                CreateCommentRequest request = new CreateCommentRequest("작성자" + i, PASSWORD, "답글" + i);
                mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // 첫 페이지
            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(5)))
                    .andExpect(jsonPath("$.totalElements").value(7))
                    .andExpect(jsonPath("$.hasMore").value(true));

            // 두 번째 페이지
            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.hasMore").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/comments - 댓글 목록에 replyCount 포함")
    class GetCommentsWithReplyCount {

        @Test
        @DisplayName("댓글 목록에 replyCount가 포함된다")
        void getComments_includesReplyCount() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 답글 3개 생성
            for (int i = 0; i < 3; i++) {
                CreateCommentRequest request = new CreateCommentRequest("작성자" + i, PASSWORD, "답글" + i);
                mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].replyCount").value(3));
        }

        @Test
        @DisplayName("삭제된 답글은 replyCount에 포함되지 않는다")
        void getComments_deletedReplyNotCountedInReplyCount() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 답글 2개 생성
            CreateCommentRequest request1 = new CreateCommentRequest("작성자1", PASSWORD, "답글1");
            MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andReturn();
            Long replyId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            CreateCommentRequest request2 = new CreateCommentRequest("작성자2", PASSWORD, "답글2");
            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)));

            // 첫 번째 답글 삭제
            DeleteCommentRequest deleteRequest = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, replyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequest)));

            // replyCount는 1이어야 함 (삭제된 답글 제외)
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].replyCount").value(1));
        }

        @Test
        @DisplayName("댓글 목록에는 일반 댓글만 표시된다 (답글은 별도 조회)")
        void getComments_onlyRootComments() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 답글 생성
            CreateCommentRequest replyRequest = new CreateCommentRequest("답글작성자", PASSWORD, "답글 내용");
            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(replyRequest)));

            // 댓글 목록에는 일반 댓글만 1개
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].parentId").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("부모 댓글 삭제 시 답글 유지")
    class ParentCommentDeletion {

        @Test
        @DisplayName("부모 댓글 삭제 후에도 답글은 유지된다")
        void deleteParentComment_repliesRemain() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 답글 생성
            CreateCommentRequest replyRequest = new CreateCommentRequest("답글작성자", PASSWORD, "답글 내용");
            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(replyRequest)));

            // 부모 댓글 삭제
            DeleteCommentRequest deleteRequest = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequest)));

            // 답글은 여전히 조회 가능
            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].content").value("답글 내용"));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{id} - 게시글 상세에 댓글 포함")
    class GetPostWithComments {

        @Test
        @DisplayName("게시글 상세 조회 시 최신 5건의 댓글이 포함된다")
        void getPost_includesComments() throws Exception {
            Long postId = createTestPost();
            createTestComment(postId);
            createTestComment(postId);

            mockMvc.perform(get("/api/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentCount").value(2))
                    .andExpect(jsonPath("$.comments.content", hasSize(2)))
                    .andExpect(jsonPath("$.comments.page").value(0))
                    .andExpect(jsonPath("$.comments.size").value(5))
                    .andExpect(jsonPath("$.comments.totalElements").value(2))
                    .andExpect(jsonPath("$.comments.hasMore").value(false));
        }

        @Test
        @DisplayName("댓글이 없으면 빈 목록이 반환된다")
        void getPost_noComments() throws Exception {
            Long postId = createTestPost();

            mockMvc.perform(get("/api/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentCount").value(0))
                    .andExpect(jsonPath("$.comments.content", hasSize(0)))
                    .andExpect(jsonPath("$.comments.totalElements").value(0))
                    .andExpect(jsonPath("$.comments.hasMore").value(false));
        }

        @Test
        @DisplayName("게시글 상세에 댓글의 replyCount가 포함된다")
        void getPost_commentsIncludeReplyCount() throws Exception {
            Long postId = createTestPost();
            Long commentId = createTestComment(postId);

            // 답글 2개 생성
            for (int i = 0; i < 2; i++) {
                CreateCommentRequest request = new CreateCommentRequest("답글작성자" + i, PASSWORD, "답글" + i);
                mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            mockMvc.perform(get("/api/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.comments.content[0].replyCount").value(2));
        }

        @Test
        @DisplayName("삭제된 댓글은 commentCount에 포함되지 않는다")
        void getPost_deletedCommentNotCounted() throws Exception {
            Long postId = createTestPost();
            Long commentId1 = createTestComment(postId);
            createTestComment(postId);

            // 첫 번째 댓글 삭제
            DeleteCommentRequest deleteRequest = new DeleteCommentRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequest)));

            mockMvc.perform(get("/api/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.commentCount").value(1))
                    .andExpect(jsonPath("$.comments.content", hasSize(2)))
                    .andExpect(jsonPath("$.comments.totalElements").value(2));
        }

        @Test
        @DisplayName("게시글 삭제 시 연관 댓글도 함께 삭제된다")
        void deletePost_deletesComments() throws Exception {
            Long postId = createTestPost();
            createTestComment(postId);
            createTestComment(postId);

            DeletePostRequest deleteRequest = new DeletePostRequest(PASSWORD);
            mockMvc.perform(delete("/api/posts/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deleteRequest)))
                    .andExpect(status().isNoContent());

            // 게시글이 삭제된 후 댓글 조회 시 404
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isNotFound());
        }
    }
}
