package com.board.cleancode.adapter.out.persistence;

import com.board.cleancode.domain.model.Post;
import com.board.cleancode.domain.port.out.PostRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final PostJpaRepository jpaRepository;

    public PostPersistenceAdapter(PostJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Post save(Post post) {
        PostJpaEntity entity = PostJpaEntity.fromDomain(post);
        PostJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jpaRepository.findById(id).map(PostJpaEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public PostPage findAll(int page, int size, String sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PostJpaEntity> result;
        if ("likes".equals(sort)) {
            result = jpaRepository.findAllOrderByLikeCountDesc(pageRequest);
        } else {
            Sort sorting = getSortOrder(sort);
            result = jpaRepository.findAll(PageRequest.of(page, size, sorting));
        }
        return toPostPage(result);
    }

    @Override
    public PostPage searchByTitle(String keyword, int page, int size, String sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PostJpaEntity> result;
        if ("likes".equals(sort)) {
            result = jpaRepository.findByTitleContainingIgnoreCaseOrderByLikeCountDesc(keyword, pageRequest);
        } else {
            Sort sorting = getSortOrder(sort);
            result = jpaRepository.findByTitleContainingIgnoreCase(keyword, PageRequest.of(page, size, sorting));
        }
        return toPostPage(result);
    }

    @Override
    public PostPage searchByAuthor(String keyword, int page, int size, String sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PostJpaEntity> result;
        if ("likes".equals(sort)) {
            result = jpaRepository.findByAuthorContainingIgnoreCaseOrderByLikeCountDesc(keyword, pageRequest);
        } else {
            Sort sorting = getSortOrder(sort);
            result = jpaRepository.findByAuthorContainingIgnoreCase(keyword, PageRequest.of(page, size, sorting));
        }
        return toPostPage(result);
    }

    @Override
    public PostPage searchByContent(String keyword, int page, int size, String sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PostJpaEntity> result;
        if ("likes".equals(sort)) {
            result = jpaRepository.findByContentContainingIgnoreCaseOrderByLikeCountDesc(keyword, pageRequest);
        } else {
            Sort sorting = getSortOrder(sort);
            result = jpaRepository.findByContentContainingIgnoreCase(keyword, PageRequest.of(page, size, sorting));
        }
        return toPostPage(result);
    }

    @Override
    public PostPage searchByHashtag(String keyword, int page, int size, String sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PostJpaEntity> result;
        if ("likes".equals(sort)) {
            result = jpaRepository.findByHashtagOrderByLikeCountDesc(keyword, pageRequest);
        } else {
            Sort sorting = getSortOrder(sort);
            result = jpaRepository.findByHashtag(keyword, PageRequest.of(page, size, sorting));
        }
        return toPostPage(result);
    }

    private Sort getSortOrder(String sort) {
        return switch (sort) {
            case "views" -> Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("createdAt"));
            default -> Sort.by(Sort.Order.desc("createdAt")); // latest
        };
    }

    private PostPage toPostPage(Page<PostJpaEntity> page) {
        List<Post> posts = page.getContent().stream()
                .map(PostJpaEntity::toDomain)
                .toList();
        return new PostPage(posts, page.getTotalElements());
    }
}
