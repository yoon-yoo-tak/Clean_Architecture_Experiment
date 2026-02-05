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
class PostApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    // === 게시글 목록 조회 테스트 ===

    @Test
    void 게시글_목록_조회_기본_페이징() throws Exception {
        for (int i = 0; i < 15; i++) {
            createTestPostWithTitle("게시글 " + i);
        }

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPostCount").value(15))
                .andExpect(jsonPath("$.posts[0].id").exists())
                .andExpect(jsonPath("$.posts[0].title").exists())
                .andExpect(jsonPath("$.posts[0].author").exists())
                .andExpect(jsonPath("$.posts[0].createdAt").exists())
                .andExpect(jsonPath("$.posts[0].commentCount").exists())
                .andExpect(jsonPath("$.posts[0].viewCount").exists())
                .andExpect(jsonPath("$.posts[0].likeCount").exists())
                .andExpect(jsonPath("$.posts[0].isNew").exists());
    }

    @Test
    void 게시글_목록_조회_size_20() throws Exception {
        for (int i = 0; i < 25; i++) {
            createTestPostWithTitle("게시글 " + i);
        }

        mockMvc.perform(get("/api/posts").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(20))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(25));
    }

    @Test
    void 게시글_목록_조회_허용되지_않는_size_400() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/api/posts").param("size", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 게시글_검색_제목() throws Exception {
        createTestPostWithTitle("Spring Boot 입문");
        createTestPostWithTitle("Spring Security 활용");
        createTestPostWithTitle("JPA 기초");

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "title")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.totalPostCount").value(3));
    }

    @Test
    void 게시글_검색_작성자() throws Exception {
        createTestPostWithAuthor("홍길동");
        createTestPostWithAuthor("홍길동");
        createTestPostWithAuthor("김철수");

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "author")
                        .param("keyword", "홍길동"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.posts.length()").value(2));
    }

    @Test
    void 게시글_검색_내용() throws Exception {
        createTestPostWithContent("Java는 객체지향 언어입니다.");
        createTestPostWithContent("Python은 스크립트 언어입니다.");
        createTestPostWithContent("Java와 Spring을 함께 사용합니다.");

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "content")
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.posts.length()").value(2));
    }

    @Test
    void 게시글_검색_해시태그() throws Exception {
        createTestPostWithHashtags(List.of("Spring", "Java"));
        createTestPostWithHashtags(List.of("Spring", "Boot"));
        createTestPostWithHashtags(List.of("Python"));

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "hashtag")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.posts.length()").value(2));
    }

    @Test
    void 게시글_검색_해시태그_부분일치_안됨() throws Exception {
        createTestPostWithHashtags(List.of("SpringBoot"));

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "hashtag")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.posts.length()").value(0));
    }

    @Test
    void 게시글_목록_totalPostCount_totalCommentCount_정확성() throws Exception {
        Post post1 = createTestPost();
        Post post2 = createTestPost();

        // 댓글 3개 생성 (post1에 2개, post2에 1개)
        createTestComment(post1.getId(), "댓글러1", "댓글1");
        createTestComment(post1.getId(), "댓글러2", "댓글2");
        Comment deletedComment = createTestComment(post2.getId(), "댓글러3", "삭제될 댓글");

        // 1개 소프트 삭제
        deletedComment.setDeleted(true);
        commentRepository.save(deletedComment);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPostCount").value(2))
                .andExpect(jsonPath("$.totalCommentCount").value(2));
    }

    @Test
    void 게시글_목록_검색시에도_totalPostCount는_전체() throws Exception {
        createTestPostWithTitle("Spring 게시글");
        createTestPostWithTitle("JPA 게시글");
        createTestPostWithTitle("기타 게시글");

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "title")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPostCount").value(3))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void 게시글_목록_isNew_플래그() throws Exception {
        // 최근 게시글 (isNew = true)
        createTestPost();

        // 오래된 게시글 (isNew = false)
        Post oldPost = new Post();
        oldPost.setTitle("오래된 게시글");
        oldPost.setContent("내용");
        oldPost.setAuthor("작성자");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        oldPost.setPassword(encoder.encode("1234"));
        oldPost.setHashtags(new ArrayList<>());
        oldPost.setViewCount(0);
        oldPost.setLikeCount(0);
        oldPost.setCommentCount(0);
        oldPost.setCreatedAt(LocalDateTime.now().minusDays(5));
        oldPost.setUpdatedAt(LocalDateTime.now().minusDays(5));
        postRepository.save(oldPost);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.posts[0].isNew").value(true))
                .andExpect(jsonPath("$.posts[1].isNew").value(false));
    }

    @Test
    void 게시글_목록_commentCount_정확성() throws Exception {
        Post post = createTestPost();
        createTestComment(post.getId(), "댓글러1", "댓글1");
        createTestComment(post.getId(), "댓글러2", "댓글2");
        Comment deleted = createTestComment(post.getId(), "댓글러3", "삭제될 댓글");
        deleted.setDeleted(true);
        commentRepository.save(deleted);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].commentCount").value(2));
    }

    // === 게시글 정렬 테스트 ===

    @Test
    void 게시글_정렬_latest_기본값() throws Exception {
        createTestPostWithViewAndLike("게시글1", 10, 5);
        Thread.sleep(10);
        createTestPostWithViewAndLike("게시글2", 5, 10);
        Thread.sleep(10);
        createTestPostWithViewAndLike("게시글3", 20, 1);

        // sort 없이 호출하면 latest(최신순)로 정렬
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글3"))
                .andExpect(jsonPath("$.posts[1].title").value("게시글2"))
                .andExpect(jsonPath("$.posts[2].title").value("게시글1"));
    }

    @Test
    void 게시글_정렬_latest_명시적() throws Exception {
        createTestPostWithViewAndLike("게시글1", 10, 5);
        Thread.sleep(10);
        createTestPostWithViewAndLike("게시글2", 5, 10);
        Thread.sleep(10);
        createTestPostWithViewAndLike("게시글3", 20, 1);

        mockMvc.perform(get("/api/posts").param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("게시글3"))
                .andExpect(jsonPath("$.posts[1].title").value("게시글2"))
                .andExpect(jsonPath("$.posts[2].title").value("게시글1"));
    }

    @Test
    void 게시글_정렬_views_조회수순() throws Exception {
        createTestPostWithViewAndLike("조회수5", 5, 10);
        Thread.sleep(10);
        createTestPostWithViewAndLike("조회수20", 20, 1);
        Thread.sleep(10);
        createTestPostWithViewAndLike("조회수10", 10, 5);

        mockMvc.perform(get("/api/posts").param("sort", "views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("조회수20"))
                .andExpect(jsonPath("$.posts[0].viewCount").value(20))
                .andExpect(jsonPath("$.posts[1].title").value("조회수10"))
                .andExpect(jsonPath("$.posts[1].viewCount").value(10))
                .andExpect(jsonPath("$.posts[2].title").value("조회수5"))
                .andExpect(jsonPath("$.posts[2].viewCount").value(5));
    }

    @Test
    void 게시글_정렬_likes_좋아요순() throws Exception {
        createTestPostWithViewAndLike("좋아요5", 20, 5);
        Thread.sleep(10);
        createTestPostWithViewAndLike("좋아요15", 5, 15);
        Thread.sleep(10);
        createTestPostWithViewAndLike("좋아요10", 10, 10);

        mockMvc.perform(get("/api/posts").param("sort", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("좋아요15"))
                .andExpect(jsonPath("$.posts[0].likeCount").value(15))
                .andExpect(jsonPath("$.posts[1].title").value("좋아요10"))
                .andExpect(jsonPath("$.posts[1].likeCount").value(10))
                .andExpect(jsonPath("$.posts[2].title").value("좋아요5"))
                .andExpect(jsonPath("$.posts[2].likeCount").value(5));
    }

    @Test
    void 게시글_정렬_views_동일시_최신순() throws Exception {
        createTestPostWithViewAndLike("먼저작성", 10, 5);
        Thread.sleep(10);
        createTestPostWithViewAndLike("나중작성", 10, 5);

        mockMvc.perform(get("/api/posts").param("sort", "views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("나중작성"))
                .andExpect(jsonPath("$.posts[1].title").value("먼저작성"));
    }

    @Test
    void 게시글_정렬_likes_동일시_최신순() throws Exception {
        createTestPostWithViewAndLike("먼저작성", 5, 10);
        Thread.sleep(10);
        createTestPostWithViewAndLike("나중작성", 5, 10);

        mockMvc.perform(get("/api/posts").param("sort", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("나중작성"))
                .andExpect(jsonPath("$.posts[1].title").value("먼저작성"));
    }

    @Test
    void 게시글_정렬_검색과_조합_title_views() throws Exception {
        createTestPostWithTitleAndViewAndLike("Spring Boot", 5, 1);
        Thread.sleep(10);
        createTestPostWithTitleAndViewAndLike("Spring Security", 15, 1);
        Thread.sleep(10);
        createTestPostWithTitleAndViewAndLike("JPA 기초", 100, 1);

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "title")
                        .param("keyword", "Spring")
                        .param("sort", "views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.posts[0].title").value("Spring Security"))
                .andExpect(jsonPath("$.posts[1].title").value("Spring Boot"));
    }

    @Test
    void 게시글_정렬_검색과_조합_author_likes() throws Exception {
        createTestPostWithAuthorAndViewAndLike("홍길동", 1, 5);
        Thread.sleep(10);
        createTestPostWithAuthorAndViewAndLike("홍길동", 1, 20);
        Thread.sleep(10);
        createTestPostWithAuthorAndViewAndLike("김철수", 1, 100);

        mockMvc.perform(get("/api/posts")
                        .param("searchType", "author")
                        .param("keyword", "홍길동")
                        .param("sort", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.posts[0].likeCount").value(20))
                .andExpect(jsonPath("$.posts[1].likeCount").value(5));
    }

    @Test
    void 게시글_정렬_허용되지_않은_sort값_400() throws Exception {
        mockMvc.perform(get("/api/posts").param("sort", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/api/posts").param("sort", "newest"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/api/posts").param("sort", "popular"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 게시글 작성 테스트 ===

    @Test
    void 게시글_작성_성공() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "테스트 제목",
                "content", "테스트 내용",
                "author", "작성자",
                "password", "1234",
                "hashtags", List.of("태그1", "태그2")
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.author").value("작성자"))
                .andExpect(jsonPath("$.hashtags[0]").value("태그1"))
                .andExpect(jsonPath("$.hashtags[1]").value("태그2"))
                .andExpect(jsonPath("$.viewCount").value(0))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 게시글_작성_해시태그_없이_성공() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "제목",
                "content", "내용",
                "author", "작성자",
                "password", "1234"
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hashtags").isArray())
                .andExpect(jsonPath("$.hashtags").isEmpty());
    }

    @Test
    void 게시글_작성_제목_없으면_400() throws Exception {
        Map<String, Object> request = Map.of(
                "content", "내용",
                "author", "작성자",
                "password", "1234"
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 게시글_작성_비밀번호_짧으면_400() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "제목",
                "content", "내용",
                "author", "작성자",
                "password", "123"
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 게시글_작성_해시태그_6개이면_400() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "제목",
                "content", "내용",
                "author", "작성자",
                "password", "1234",
                "hashtags", List.of("1", "2", "3", "4", "5", "6")
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 게시글 상세 조회 테스트 ===

    @Test
    void 게시글_조회_성공_조회수_증가() throws Exception {
        Post post = createTestPost();

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.viewCount").value(1))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 게시글_조회_두번하면_조회수_2() throws Exception {
        Post post = createTestPost();

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(2));
    }

    @Test
    void 게시글_조회_존재하지_않으면_404() throws Exception {
        mockMvc.perform(get("/api/posts/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 게시글 수정 테스트 ===

    @Test
    void 게시글_수정_성공() throws Exception {
        Post post = createTestPost();

        Map<String, Object> request = Map.of(
                "title", "수정된 제목",
                "content", "수정된 내용",
                "password", "1234",
                "hashtags", List.of("새태그")
        );

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"))
                .andExpect(jsonPath("$.hashtags[0]").value("새태그"))
                .andExpect(jsonPath("$.viewCount").value(0))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void 게시글_수정_비밀번호_불일치_403() throws Exception {
        Post post = createTestPost();

        Map<String, Object> request = Map.of(
                "title", "수정된 제목",
                "content", "수정된 내용",
                "password", "wrong"
        );

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 게시글_수정_존재하지_않으면_404() throws Exception {
        Map<String, Object> request = Map.of(
                "title", "수정된 제목",
                "content", "수정된 내용",
                "password", "1234"
        );

        mockMvc.perform(put("/api/posts/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 게시글 삭제 테스트 ===

    @Test
    void 게시글_삭제_성공() throws Exception {
        Post post = createTestPost();

        Map<String, String> request = Map.of("password", "1234");

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // 삭제 확인
        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void 게시글_삭제_비밀번호_불일치_403() throws Exception {
        Post post = createTestPost();

        Map<String, String> request = Map.of("password", "wrong");

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 게시글_삭제_존재하지_않으면_404() throws Exception {
        Map<String, String> request = Map.of("password", "1234");

        mockMvc.perform(delete("/api/posts/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // === 비밀번호 변경 테스트 ===

    @Test
    void 비밀번호_변경_성공() throws Exception {
        Post post = createTestPost();

        Map<String, String> request = Map.of(
                "currentPassword", "1234",
                "newPassword", "5678"
        );

        mockMvc.perform(patch("/api/posts/" + post.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다."));
    }

    @Test
    void 비밀번호_변경_후_새_비밀번호로_수정_가능() throws Exception {
        Post post = createTestPost();

        // 비밀번호 변경
        Map<String, String> changeRequest = Map.of(
                "currentPassword", "1234",
                "newPassword", "newpass"
        );

        mockMvc.perform(patch("/api/posts/" + post.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk());

        // 새 비밀번호로 수정
        Map<String, Object> updateRequest = Map.of(
                "title", "수정된 제목",
                "content", "수정된 내용",
                "password", "newpass"
        );

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"));

        // 기존 비밀번호로 수정 시도하면 403
        Map<String, Object> oldPasswordRequest = Map.of(
                "title", "또 수정",
                "content", "또 수정 내용",
                "password", "1234"
        );

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldPasswordRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 비밀번호_변경_후_새_비밀번호로_삭제_가능() throws Exception {
        Post post = createTestPost();

        // 비밀번호 변경
        Map<String, String> changeRequest = Map.of(
                "currentPassword", "1234",
                "newPassword", "deletepw"
        );

        mockMvc.perform(patch("/api/posts/" + post.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk());

        // 새 비밀번호로 삭제
        Map<String, String> deleteRequest = Map.of("password", "deletepw");

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    void 비밀번호_변경_현재_비밀번호_불일치_403() throws Exception {
        Post post = createTestPost();

        Map<String, String> request = Map.of(
                "currentPassword", "wrong",
                "newPassword", "5678"
        );

        mockMvc.perform(patch("/api/posts/" + post.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 비밀번호_변경_새_비밀번호_4자_미만_400() throws Exception {
        Post post = createTestPost();

        Map<String, String> request = Map.of(
                "currentPassword", "1234",
                "newPassword", "123"
        );

        mockMvc.perform(patch("/api/posts/" + post.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void 비밀번호_변경_존재하지_않는_게시글_404() throws Exception {
        Map<String, String> request = Map.of(
                "currentPassword", "1234",
                "newPassword", "5678"
        );

        mockMvc.perform(patch("/api/posts/9999/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
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

    private Post createTestPostWithTitle(String title) {
        Post post = new Post();
        post.setTitle(title);
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

    private Post createTestPostWithAuthor(String author) {
        Post post = new Post();
        post.setTitle("테스트 제목");
        post.setContent("테스트 내용");
        post.setAuthor(author);
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

    private Post createTestPostWithContent(String content) {
        Post post = new Post();
        post.setTitle("테스트 제목");
        post.setContent(content);
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

    private Post createTestPostWithHashtags(List<String> hashtags) {
        Post post = new Post();
        post.setTitle("테스트 제목");
        post.setContent("테스트 내용");
        post.setAuthor("작성자");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        post.setPassword(encoder.encode("1234"));
        post.setHashtags(new ArrayList<>(hashtags));
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

    private Post createTestPostWithViewAndLike(String title, int viewCount, int likeCount) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent("테스트 내용");
        post.setAuthor("작성자");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        post.setPassword(encoder.encode("1234"));
        post.setHashtags(new ArrayList<>());
        post.setViewCount(viewCount);
        post.setLikeCount(likeCount);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    private Post createTestPostWithTitleAndViewAndLike(String title, int viewCount, int likeCount) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent("테스트 내용");
        post.setAuthor("작성자");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        post.setPassword(encoder.encode("1234"));
        post.setHashtags(new ArrayList<>());
        post.setViewCount(viewCount);
        post.setLikeCount(likeCount);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    private Post createTestPostWithAuthorAndViewAndLike(String author, int viewCount, int likeCount) {
        Post post = new Post();
        post.setTitle("테스트 제목");
        post.setContent("테스트 내용");
        post.setAuthor(author);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        post.setPassword(encoder.encode("1234"));
        post.setHashtags(new ArrayList<>());
        post.setViewCount(viewCount);
        post.setLikeCount(likeCount);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }
}
