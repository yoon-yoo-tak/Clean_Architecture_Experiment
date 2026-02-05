package com.board.cleancode.domain.port.out;

import com.board.cleancode.domain.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepositoryPort {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, int page, int size);

    List<Comment> findRootCommentsByPostIdOrderByCreatedAtDesc(Long postId, int page, int size);

    List<Comment> findRepliesByParentIdOrderByCreatedAtAsc(Long parentId, int page, int size);

    long countByPostId(Long postId);

    long countByPostIdAndDeletedFalse(Long postId);

    long countRootCommentsByPostId(Long postId);

    long countRepliesByParentId(Long parentId);

    long countRepliesByParentIdAndDeletedFalse(Long parentId);

    void deleteByPostId(Long postId);

    long countAllByDeletedFalse();
}
