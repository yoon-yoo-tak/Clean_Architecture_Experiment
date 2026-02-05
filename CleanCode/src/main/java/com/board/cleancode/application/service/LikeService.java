package com.board.cleancode.application.service;

import com.board.cleancode.domain.exception.AlreadyLikedException;
import com.board.cleancode.domain.exception.NotLikedException;
import com.board.cleancode.domain.exception.PostNotFoundException;
import com.board.cleancode.domain.model.Like;
import com.board.cleancode.domain.port.in.GetLikeInfoUseCase;
import com.board.cleancode.domain.port.in.LikePostUseCase;
import com.board.cleancode.domain.port.in.UnlikePostUseCase;
import com.board.cleancode.domain.port.out.LikeRepositoryPort;
import com.board.cleancode.domain.port.out.PostRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikeService implements LikePostUseCase, UnlikePostUseCase, GetLikeInfoUseCase {

    private final LikeRepositoryPort likeRepository;
    private final PostRepositoryPort postRepository;

    public LikeService(LikeRepositoryPort likeRepository, PostRepositoryPort postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    @Override
    public LikeResult likePost(Long postId, String guestId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (likeRepository.existsByPostIdAndGuestId(postId, guestId)) {
            throw new AlreadyLikedException();
        }

        likeRepository.save(Like.create(postId, guestId));
        int likeCount = likeRepository.countByPostId(postId);
        return new LikeResult(likeCount, true);
    }

    @Override
    public LikeResult unlikePost(Long postId, String guestId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!likeRepository.existsByPostIdAndGuestId(postId, guestId)) {
            throw new NotLikedException();
        }

        likeRepository.deleteByPostIdAndGuestId(postId, guestId);
        int likeCount = likeRepository.countByPostId(postId);
        return new LikeResult(likeCount, false);
    }

    @Override
    @Transactional(readOnly = true)
    public int getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLikedBy(Long postId, String guestId) {
        if (guestId == null || guestId.isEmpty()) {
            return false;
        }
        return likeRepository.existsByPostIdAndGuestId(postId, guestId);
    }
}
