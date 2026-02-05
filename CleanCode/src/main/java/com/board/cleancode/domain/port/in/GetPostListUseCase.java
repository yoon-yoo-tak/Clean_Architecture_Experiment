package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Post;

import java.util.List;

public interface GetPostListUseCase {

    PostListResult getPostList(PostListQuery query);

    record PostListQuery(int page, int size, String searchType, String keyword, String sort) {
        public PostListQuery(int page, int size, String searchType, String keyword) {
            this(page, size, searchType, keyword, "latest");
        }
    }

    record PostSummary(Post post, int commentCount, int likeCount) {
    }

    record PostListResult(
            List<PostSummary> posts,
            int page,
            int size,
            long totalElements,
            int totalPages,
            long totalPostCount,
            long totalCommentCount
    ) {
    }
}
