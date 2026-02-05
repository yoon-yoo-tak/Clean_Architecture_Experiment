package com.board.cleancode.adapter.out.persistence;

import com.board.cleancode.domain.model.Like;
import com.board.cleancode.domain.port.out.LikeRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class LikePersistenceAdapter implements LikeRepositoryPort {

    private final LikeJpaRepository likeJpaRepository;

    public LikePersistenceAdapter(LikeJpaRepository likeJpaRepository) {
        this.likeJpaRepository = likeJpaRepository;
    }

    @Override
    public Like save(Like like) {
        LikeJpaEntity entity = LikeJpaEntity.fromDomain(like);
        return likeJpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteByPostIdAndGuestId(Long postId, String guestId) {
        likeJpaRepository.deleteByPostIdAndGuestId(postId, guestId);
    }

    @Override
    public boolean existsByPostIdAndGuestId(Long postId, String guestId) {
        return likeJpaRepository.existsByPostIdAndGuestId(postId, guestId);
    }

    @Override
    public int countByPostId(Long postId) {
        return likeJpaRepository.countByPostId(postId);
    }

    @Override
    public void deleteByPostId(Long postId) {
        likeJpaRepository.deleteByPostId(postId);
    }
}
