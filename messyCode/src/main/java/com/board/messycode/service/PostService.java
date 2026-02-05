package com.board.messycode.service;

import com.board.messycode.entity.Post;
import com.board.messycode.entity.PostLike;
import com.board.messycode.repository.CommentRepository;
import com.board.messycode.repository.PostLikeRepository;
import com.board.messycode.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    public Map<String, Object> getPostList(int page, int size, String searchType, String keyword, String sort) {
        Page<Post> postPage;

        if (searchType != null && keyword != null && !keyword.trim().isEmpty()) {
            if ("title".equals(searchType)) {
                if ("views".equals(sort)) {
                    postPage = postRepository.findByTitleContainingOrderByViewCountDescCreatedAtDesc(keyword, PageRequest.of(page, size));
                } else if ("likes".equals(sort)) {
                    postPage = postRepository.findByTitleContainingOrderByLikeCountDescCreatedAtDesc(keyword, PageRequest.of(page, size));
                } else {
                    postPage = postRepository.findByTitleContainingOrderByCreatedAtDesc(keyword, PageRequest.of(page, size));
                }
            } else if ("author".equals(searchType)) {
                if ("views".equals(sort)) {
                    postPage = postRepository.findByAuthorContainingOrderByViewCountDescCreatedAtDesc(keyword, PageRequest.of(page, size));
                } else if ("likes".equals(sort)) {
                    postPage = postRepository.findByAuthorContainingOrderByLikeCountDescCreatedAtDesc(keyword, PageRequest.of(page, size));
                } else {
                    postPage = postRepository.findByAuthorContainingOrderByCreatedAtDesc(keyword, PageRequest.of(page, size));
                }
            } else if ("content".equals(searchType)) {
                if ("views".equals(sort)) {
                    postPage = postRepository.findByContentContainingOrderByViewCountDescCreatedAtDesc(keyword, PageRequest.of(page, size));
                } else if ("likes".equals(sort)) {
                    postPage = postRepository.findByContentContainingOrderByLikeCountDescCreatedAtDesc(keyword, PageRequest.of(page, size));
                } else {
                    postPage = postRepository.findByContentContainingOrderByCreatedAtDesc(keyword, PageRequest.of(page, size));
                }
            } else if ("hashtag".equals(searchType)) {
                if ("views".equals(sort)) {
                    postPage = postRepository.findByHashtagOrderByViewCountDesc(keyword, PageRequest.of(page, size));
                } else if ("likes".equals(sort)) {
                    postPage = postRepository.findByHashtagOrderByLikeCountDesc(keyword, PageRequest.of(page, size));
                } else {
                    postPage = postRepository.findByHashtag(keyword, PageRequest.of(page, size));
                }
            } else {
                if ("views".equals(sort)) {
                    postPage = postRepository.findAllByOrderByViewCountDescCreatedAtDesc(PageRequest.of(page, size));
                } else if ("likes".equals(sort)) {
                    postPage = postRepository.findAllByOrderByLikeCountDescCreatedAtDesc(PageRequest.of(page, size));
                } else {
                    postPage = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
                }
            }
        } else {
            if ("views".equals(sort)) {
                postPage = postRepository.findAllByOrderByViewCountDescCreatedAtDesc(PageRequest.of(page, size));
            } else if ("likes".equals(sort)) {
                postPage = postRepository.findAllByOrderByLikeCountDescCreatedAtDesc(PageRequest.of(page, size));
            } else {
                postPage = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
            }
        }

        List<Map<String, Object>> posts = new ArrayList<>();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        for (Post p : postPage.getContent()) {
            Map<String, Object> postMap = new LinkedHashMap<>();
            postMap.put("id", p.getId());
            postMap.put("title", p.getTitle());
            postMap.put("author", p.getAuthor());
            postMap.put("createdAt", p.getCreatedAt());
            postMap.put("commentCount", commentRepository.countByPostIdAndDeletedFalse(p.getId()));
            postMap.put("viewCount", p.getViewCount());
            postMap.put("likeCount", p.getLikeCount());
            postMap.put("isNew", p.getCreatedAt().isAfter(threeDaysAgo));
            posts.add(postMap);
        }

        long totalPostCount = postRepository.count();
        long totalCommentCount = commentRepository.countByDeletedFalse();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPostCount", totalPostCount);
        result.put("totalCommentCount", totalCommentCount);
        result.put("posts", posts);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", postPage.getTotalPages());
        result.put("totalElements", postPage.getTotalElements());

        return result;
    }

    @Transactional
    public Post createPost(Post post) {
        // 유효성 검증
        if (post.getTitle() == null || post.getTitle().trim().isEmpty() || post.getTitle().length() > 200) {
            throw new RuntimeException("제목은 1~200자여야 합니다.");
        }
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new RuntimeException("내용은 필수입니다.");
        }
        if (post.getAuthor() == null || post.getAuthor().trim().isEmpty() || post.getAuthor().length() > 50) {
            throw new RuntimeException("작성자는 1~50자여야 합니다.");
        }
        if (post.getPassword() == null || post.getPassword().length() < 4) {
            throw new RuntimeException("비밀번호는 4자 이상이어야 합니다.");
        }
        if (post.getHashtags() != null && post.getHashtags().size() > 5) {
            throw new RuntimeException("해시태그는 최대 5개까지 가능합니다.");
        }
        if (post.getHashtags() != null) {
            for (String tag : post.getHashtags()) {
                if (tag == null || tag.trim().isEmpty() || tag.length() > 30) {
                    throw new RuntimeException("해시태그는 1~30자여야 합니다.");
                }
            }
        }

        // BCrypt 암호화
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        post.setPassword(encoder.encode(post.getPassword()));

        // 초기값 설정
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        if (post.getHashtags() == null) {
            post.setHashtags(new ArrayList<>());
        }
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    @Transactional
    public Post getPostAndIncreaseViewCount(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Transactional
    public Post updatePost(Long id, String title, String content, java.util.List<String> hashtags) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        post.setTitle(title);
        post.setContent(content);
        if (hashtags != null) {
            post.setHashtags(hashtags);
        } else {
            post.setHashtags(new ArrayList<>());
        }
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> likePost(Long postId, String guestId) {
        if (postLikeRepository.existsByPostIdAndGuestId(postId, guestId)) {
            return null; // 이미 좋아요한 상태
        }
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setGuestId(guestId);
        postLikeRepository.save(like);

        long likeCount = postLikeRepository.countByPostId(postId);
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null) {
            post.setLikeCount((int) likeCount);
            postRepository.save(post);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("likeCount", likeCount);
        result.put("liked", true);
        return result;
    }

    @Transactional
    public Map<String, Object> unlikePost(Long postId, String guestId) {
        if (!postLikeRepository.existsByPostIdAndGuestId(postId, guestId)) {
            return null; // 좋아요하지 않은 상태
        }
        postLikeRepository.deleteByPostIdAndGuestId(postId, guestId);

        long likeCount = postLikeRepository.countByPostId(postId);
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null) {
            post.setLikeCount((int) likeCount);
            postRepository.save(post);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("likeCount", likeCount);
        result.put("liked", false);
        return result;
    }

    public boolean hasLiked(Long postId, String guestId) {
        return postLikeRepository.existsByPostIdAndGuestId(postId, guestId);
    }

    public long getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public void deleteLikesByPostId(Long postId) {
        postLikeRepository.deleteByPostId(postId);
    }

    @Transactional
    public void changePassword(Long id, String newPassword) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            post.setPassword(encoder.encode(newPassword));
            postRepository.save(post);
        }
    }
}
