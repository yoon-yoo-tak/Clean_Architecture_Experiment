package com.board.cleancode.adapter.in.web;

import com.board.cleancode.adapter.in.web.dto.CreateCommentRequest;
import com.board.cleancode.adapter.in.web.dto.CreatePostRequest;
import com.board.cleancode.adapter.in.web.dto.DeleteCommentRequest;
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
class PostListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/posts";
    private static final String PASSWORD = "test1234";

    private Long createTestPost(String title, String author, String content, List<String> hashtags) throws Exception {
        CreatePostRequest request = new CreatePostRequest(title, content, author, PASSWORD, hashtags);
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createComment(Long postId, String author, String content) throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(author, PASSWORD, content);
        MvcResult result = mockMvc.perform(post(BASE_URL + "/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void deleteComment(Long postId, Long commentId) throws Exception {
        DeleteCommentRequest request = new DeleteCommentRequest(PASSWORD);
        mockMvc.perform(delete(BASE_URL + "/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Nested
    @DisplayName("GET /api/posts - 게시글 목록 조회")
    class GetPostList {

        @Test
        @DisplayName("게시글이 없을 때 빈 목록을 반환한다")
        void getPostList_empty() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPostCount").value(0))
                    .andExpect(jsonPath("$.totalCommentCount").value(0))
                    .andExpect(jsonPath("$.posts", hasSize(0)))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("게시글 목록을 최신순으로 반환한다")
        void getPostList_orderedByCreatedAtDesc() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);
            Long id3 = createTestPost("세번째 글", "작성자3", "내용3", null);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(3)))
                    .andExpect(jsonPath("$.posts[0].id").value(id3))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[2].id").value(id1))
                    .andExpect(jsonPath("$.totalElements").value(3));
        }

        @Test
        @DisplayName("각 게시글에 필수 필드가 포함된다")
        void getPostList_containsRequiredFields() throws Exception {
            createTestPost("제목", "작성자", "내용", List.of("태그1"));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts[0].id").isNumber())
                    .andExpect(jsonPath("$.posts[0].title").value("제목"))
                    .andExpect(jsonPath("$.posts[0].author").value("작성자"))
                    .andExpect(jsonPath("$.posts[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.posts[0].commentCount").value(0))
                    .andExpect(jsonPath("$.posts[0].viewCount").value(0))
                    .andExpect(jsonPath("$.posts[0].likeCount").value(0))
                    .andExpect(jsonPath("$.posts[0].isNew").value(true));
        }
    }

    @Nested
    @DisplayName("페이징")
    class Paging {

        @Test
        @DisplayName("size=10으로 페이징한다")
        void paging_size10() throws Exception {
            for (int i = 0; i < 15; i++) {
                createTestPost("글 " + i, "작성자", "내용", null);
            }

            mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(10)))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.totalElements").value(15));

            mockMvc.perform(get(BASE_URL).param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(5)))
                    .andExpect(jsonPath("$.page").value(1));
        }

        @Test
        @DisplayName("size=20으로 페이징한다")
        void paging_size20() throws Exception {
            for (int i = 0; i < 25; i++) {
                createTestPost("글 " + i, "작성자", "내용", null);
            }

            mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(20)))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.totalElements").value(25));
        }

        @Test
        @DisplayName("허용되지 않는 size 값이면 400을 반환한다")
        void paging_invalidSize_returns400() throws Exception {
            mockMvc.perform(get(BASE_URL).param("size", "5"))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(BASE_URL).param("size", "15"))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(BASE_URL).param("size", "100"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("기본 size는 10이다")
        void paging_defaultSize() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(10));
        }
    }

    @Nested
    @DisplayName("검색")
    class Search {

        @Test
        @DisplayName("title로 검색한다")
        void search_byTitle() throws Exception {
            createTestPost("Spring Boot 강좌", "작성자1", "내용1", null);
            createTestPost("JPA 강좌", "작성자2", "내용2", null);
            createTestPost("다른 제목", "작성자3", "내용3", null);

            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "title")
                            .param("keyword", "강좌"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("author로 검색한다")
        void search_byAuthor() throws Exception {
            createTestPost("글1", "홍길동", "내용1", null);
            createTestPost("글2", "홍길동", "내용2", null);
            createTestPost("글3", "김철수", "내용3", null);

            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "author")
                            .param("keyword", "홍길동"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("content로 검색한다")
        void search_byContent() throws Exception {
            createTestPost("글1", "작성자1", "Java 프로그래밍", null);
            createTestPost("글2", "작성자2", "Python 프로그래밍", null);
            createTestPost("글3", "작성자3", "요리 레시피", null);

            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "content")
                            .param("keyword", "프로그래밍"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("hashtag로 검색한다 (정확히 일치)")
        void search_byHashtag() throws Exception {
            createTestPost("글1", "작성자1", "내용1", List.of("java", "spring"));
            createTestPost("글2", "작성자2", "내용2", List.of("java", "jpa"));
            createTestPost("글3", "작성자3", "내용3", List.of("python"));

            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "hashtag")
                            .param("keyword", "java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("hashtag 검색은 부분 일치가 아닌 정확히 일치한다")
        void search_byHashtag_exactMatch() throws Exception {
            createTestPost("글1", "작성자1", "내용1", List.of("javascript"));
            createTestPost("글2", "작성자2", "내용2", List.of("java"));

            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "hashtag")
                            .param("keyword", "java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(1)))
                    .andExpect(jsonPath("$.posts[0].title").value("글2"));
        }

        @Test
        @DisplayName("searchType이나 keyword가 없으면 전체 목록을 반환한다")
        void search_withoutParams_returnsAll() throws Exception {
            createTestPost("글1", "작성자1", "내용1", null);
            createTestPost("글2", "작성자2", "내용2", null);

            // searchType만 있을 때
            mockMvc.perform(get(BASE_URL).param("searchType", "title"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)));

            // keyword만 있을 때
            mockMvc.perform(get(BASE_URL).param("keyword", "글1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)));
        }

        @Test
        @DisplayName("검색 결과에도 페이징이 적용된다")
        void search_withPaging() throws Exception {
            for (int i = 0; i < 15; i++) {
                createTestPost("검색대상 " + i, "작성자", "내용", null);
            }
            createTestPost("다른 제목", "작성자", "내용", null);

            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "title")
                            .param("keyword", "검색대상")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements").value(15))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }
    }

    @Nested
    @DisplayName("totalPostCount, totalCommentCount")
    class TotalCounts {

        @Test
        @DisplayName("totalPostCount는 검색 필터와 무관하게 전체 게시글 수를 반환한다")
        void totalPostCount_ignoresFilter() throws Exception {
            createTestPost("Spring 글", "작성자", "내용", null);
            createTestPost("JPA 글", "작성자", "내용", null);
            createTestPost("다른 글", "작성자", "내용", null);

            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "title")
                            .param("keyword", "Spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPostCount").value(3))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("totalCommentCount는 삭제되지 않은 전체 댓글 수를 반환한다")
        void totalCommentCount_excludesDeleted() throws Exception {
            Long postId1 = createTestPost("글1", "작성자", "내용", null);
            Long postId2 = createTestPost("글2", "작성자", "내용", null);

            createComment(postId1, "댓글작성자", "댓글1");
            createComment(postId1, "댓글작성자", "댓글2");
            Long commentToDelete = createComment(postId2, "댓글작성자", "삭제될 댓글");
            createComment(postId2, "댓글작성자", "댓글3");

            // 댓글 하나 삭제
            deleteComment(postId2, commentToDelete);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCommentCount").value(3));
        }

        @Test
        @DisplayName("각 게시글의 commentCount는 해당 게시글의 삭제되지 않은 댓글 수이다")
        void postCommentCount_perPost() throws Exception {
            Long postId1 = createTestPost("글1", "작성자", "내용", null);
            Long postId2 = createTestPost("글2", "작성자", "내용", null);

            createComment(postId1, "댓글작성자", "댓글1");
            createComment(postId1, "댓글작성자", "댓글2");
            Long commentToDelete = createComment(postId1, "댓글작성자", "삭제될 댓글");
            createComment(postId2, "댓글작성자", "댓글3");

            // postId1의 댓글 하나 삭제
            deleteComment(postId1, commentToDelete);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    // 최신순이므로 postId2가 먼저
                    .andExpect(jsonPath("$.posts[0].commentCount").value(1))
                    .andExpect(jsonPath("$.posts[1].commentCount").value(2));
        }
    }

    @Nested
    @DisplayName("isNew 플래그")
    class IsNewFlag {

        @Test
        @DisplayName("방금 작성한 게시글은 isNew가 true이다")
        void isNew_recentPost() throws Exception {
            createTestPost("새 글", "작성자", "내용", null);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts[0].isNew").value(true));
        }
    }

    @Nested
    @DisplayName("정렬")
    class Sorting {

        @Test
        @DisplayName("sort 파라미터가 없으면 기본값(latest)으로 최신순 정렬한다")
        void sort_default_latest() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);
            Long id3 = createTestPost("세번째 글", "작성자3", "내용3", null);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(3)))
                    .andExpect(jsonPath("$.posts[0].id").value(id3))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[2].id").value(id1));
        }

        @Test
        @DisplayName("sort=latest는 최신순으로 정렬한다")
        void sort_latest() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);
            Long id3 = createTestPost("세번째 글", "작성자3", "내용3", null);

            mockMvc.perform(get(BASE_URL).param("sort", "latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(3)))
                    .andExpect(jsonPath("$.posts[0].id").value(id3))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[2].id").value(id1));
        }

        @Test
        @DisplayName("sort=views는 조회수 높은순으로 정렬한다")
        void sort_views() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);
            Long id3 = createTestPost("세번째 글", "작성자3", "내용3", null);

            // id1 조회 3번 (viewCount: 3)
            mockMvc.perform(get(BASE_URL + "/{id}", id1)).andExpect(status().isOk());
            mockMvc.perform(get(BASE_URL + "/{id}", id1)).andExpect(status().isOk());
            mockMvc.perform(get(BASE_URL + "/{id}", id1)).andExpect(status().isOk());

            // id2 조회 1번 (viewCount: 1)
            mockMvc.perform(get(BASE_URL + "/{id}", id2)).andExpect(status().isOk());

            // id3 조회 없음 (viewCount: 0)

            mockMvc.perform(get(BASE_URL).param("sort", "views"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(3)))
                    .andExpect(jsonPath("$.posts[0].id").value(id1))
                    .andExpect(jsonPath("$.posts[0].viewCount").value(3))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[1].viewCount").value(1))
                    .andExpect(jsonPath("$.posts[2].id").value(id3))
                    .andExpect(jsonPath("$.posts[2].viewCount").value(0));
        }

        @Test
        @DisplayName("sort=likes는 좋아요 많은순으로 정렬한다")
        void sort_likes() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);
            Long id3 = createTestPost("세번째 글", "작성자3", "내용3", null);

            // id1에 좋아요 3개
            likePost(id1, "guest1");
            likePost(id1, "guest2");
            likePost(id1, "guest3");

            // id2에 좋아요 1개
            likePost(id2, "guest4");

            // id3에 좋아요 없음

            mockMvc.perform(get(BASE_URL).param("sort", "likes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(3)))
                    .andExpect(jsonPath("$.posts[0].id").value(id1))
                    .andExpect(jsonPath("$.posts[0].likeCount").value(3))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[1].likeCount").value(1))
                    .andExpect(jsonPath("$.posts[2].id").value(id3))
                    .andExpect(jsonPath("$.posts[2].likeCount").value(0));
        }

        @Test
        @DisplayName("sort=views에서 동일 조회수일 때 최신순으로 2차 정렬한다")
        void sort_views_secondaryByLatest() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);
            Long id3 = createTestPost("세번째 글", "작성자3", "내용3", null);

            // 모두 조회수 1
            mockMvc.perform(get(BASE_URL + "/{id}", id1)).andExpect(status().isOk());
            mockMvc.perform(get(BASE_URL + "/{id}", id2)).andExpect(status().isOk());
            mockMvc.perform(get(BASE_URL + "/{id}", id3)).andExpect(status().isOk());

            mockMvc.perform(get(BASE_URL).param("sort", "views"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(3)))
                    // 조회수 동일하므로 최신순 (id3 > id2 > id1)
                    .andExpect(jsonPath("$.posts[0].id").value(id3))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[2].id").value(id1));
        }

        @Test
        @DisplayName("sort=likes에서 동일 좋아요 수일 때 최신순으로 2차 정렬한다")
        void sort_likes_secondaryByLatest() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);
            Long id3 = createTestPost("세번째 글", "작성자3", "내용3", null);

            // 모두 좋아요 1개
            likePost(id1, "guest1");
            likePost(id2, "guest2");
            likePost(id3, "guest3");

            mockMvc.perform(get(BASE_URL).param("sort", "likes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(3)))
                    // 좋아요 동일하므로 최신순 (id3 > id2 > id1)
                    .andExpect(jsonPath("$.posts[0].id").value(id3))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[2].id").value(id1));
        }

        @Test
        @DisplayName("검색과 정렬을 함께 사용할 수 있다")
        void search_with_sort() throws Exception {
            Long id1 = createTestPost("Spring 기초", "작성자1", "내용1", null);
            Long id2 = createTestPost("Spring 심화", "작성자2", "내용2", null);
            Long id3 = createTestPost("JPA 강좌", "작성자3", "내용3", null);

            // id1에 좋아요 2개
            likePost(id1, "guest1");
            likePost(id1, "guest2");

            // id2에 좋아요 1개
            likePost(id2, "guest3");

            // Spring 검색 + likes 정렬
            mockMvc.perform(get(BASE_URL)
                            .param("searchType", "title")
                            .param("keyword", "Spring")
                            .param("sort", "likes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.posts[0].id").value(id1))
                    .andExpect(jsonPath("$.posts[0].likeCount").value(2))
                    .andExpect(jsonPath("$.posts[1].id").value(id2))
                    .andExpect(jsonPath("$.posts[1].likeCount").value(1));
        }

        @Test
        @DisplayName("허용되지 않은 sort 값이면 400을 반환한다")
        void sort_invalid_returns400() throws Exception {
            mockMvc.perform(get(BASE_URL).param("sort", "invalid"))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(BASE_URL).param("sort", "newest"))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(BASE_URL).param("sort", "popular"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("sort 파라미터가 빈 문자열이면 기본값(latest)으로 동작한다")
        void sort_emptyString_defaultsToLatest() throws Exception {
            Long id1 = createTestPost("첫번째 글", "작성자1", "내용1", null);
            Long id2 = createTestPost("두번째 글", "작성자2", "내용2", null);

            mockMvc.perform(get(BASE_URL).param("sort", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(2)))
                    .andExpect(jsonPath("$.posts[0].id").value(id2))
                    .andExpect(jsonPath("$.posts[1].id").value(id1));
        }

        @Test
        @DisplayName("정렬과 페이징을 함께 사용할 수 있다")
        void sort_with_paging() throws Exception {
            // 15개 게시글 생성
            Long[] ids = new Long[15];
            for (int i = 0; i < 15; i++) {
                ids[i] = createTestPost("글 " + i, "작성자", "내용", null);
            }

            // 처음 5개 게시글에 좋아요 (ids[0]~ids[4])
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j <= i; j++) {
                    likePost(ids[i], "guest" + i + "_" + j);
                }
            }
            // ids[0]: 1 like, ids[1]: 2 likes, ids[2]: 3 likes, ids[3]: 4 likes, ids[4]: 5 likes

            // likes 정렬 + 첫 페이지 (size=10)
            mockMvc.perform(get(BASE_URL)
                            .param("sort", "likes")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements").value(15))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    // 좋아요 많은순: ids[4](5) > ids[3](4) > ids[2](3) > ids[1](2) > ids[0](1) > 나머지(0)
                    .andExpect(jsonPath("$.posts[0].id").value(ids[4]))
                    .andExpect(jsonPath("$.posts[0].likeCount").value(5))
                    .andExpect(jsonPath("$.posts[1].id").value(ids[3]))
                    .andExpect(jsonPath("$.posts[1].likeCount").value(4));
        }

        private void likePost(Long postId, String guestId) throws Exception {
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .header("X-Guest-Id", guestId))
                    .andExpect(status().isOk());
        }
    }
}
