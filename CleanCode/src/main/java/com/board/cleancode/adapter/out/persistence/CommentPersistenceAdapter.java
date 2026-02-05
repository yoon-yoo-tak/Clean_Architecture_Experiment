package com.board.cleancode.adapter.out.persistence;

import com.board.cleancode.domain.model.Comment;
import com.board.cleancode.domain.port.out.CommentRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommentPersistenceAdapter implements CommentRepositoryPort {

    private final CommentJpaRepository jpaRepository;

    public CommentPersistenceAdapter(CommentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Comment save(Comment comment) {
        CommentJpaEntity entity = CommentJpaEntity.fromDomain(comment);
        CommentJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return jpaRepository.findById(id).map(CommentJpaEntity::toDomain);
    }

    @Override
    public List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, int page, int size) {
        Page<CommentJpaEntity> result = jpaRepository.findByPostIdOrderByCreatedAtDesc(
                postId, PageRequest.of(page, size));
        return result.getContent().stream()
                .map(CommentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Comment> findRootCommentsByPostIdOrderByCreatedAtDesc(Long postId, int page, int size) {
        Page<CommentJpaEntity> result = jpaRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(
                postId, PageRequest.of(page, size));
        return result.getContent().stream()
                .map(CommentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Comment> findRepliesByParentIdOrderByCreatedAtAsc(Long parentId, int page, int size) {
        Page<CommentJpaEntity> result = jpaRepository.findByParentIdOrderByCreatedAtAsc(
                parentId, PageRequest.of(page, size));
        return result.getContent().stream()
                .map(CommentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countByPostId(Long postId) {
        return jpaRepository.countByPostId(postId);
    }

    @Override
    public long countByPostIdAndDeletedFalse(Long postId) {
        return jpaRepository.countByPostIdAndDeletedFalse(postId);
    }

    @Override
    public long countRootCommentsByPostId(Long postId) {
        return jpaRepository.countByPostIdAndParentIdIsNull(postId);
    }

    @Override
    public long countRepliesByParentId(Long parentId) {
        return jpaRepository.countByParentId(parentId);
    }

    @Override
    public long countRepliesByParentIdAndDeletedFalse(Long parentId) {
        return jpaRepository.countByParentIdAndDeletedFalse(parentId);
    }

    @Override
    public void deleteByPostId(Long postId) {
        jpaRepository.deleteByPostId(postId);
    }

    @Override
    public long countAllByDeletedFalse() {
        return jpaRepository.countByDeletedFalse();
    }
}
