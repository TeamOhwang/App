package com.example.backend.domain.comment;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend.domain.comment.DTO.CommentRequestDto;
import com.example.backend.domain.post.Post;
import com.example.backend.entity.Users;
import com.example.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
     private final CommentRepository commentRepository;
     private final PostRepository postRepository;

     // 댓글 또는 대댓글 작성
    @Transactional
    public void createComment(Long postId, CommentRequestDto dto, Users user) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다"));

        Comment parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다"));
        }
        Comment comment = Comment.builder()
            .content(dto.getContent())
            .user(user)
            .post(post)
            .parent(parent)
            .build();
        commentRepository.save(comment);
    }


    // 댓글 삭제 (본인만 가능)
    @Transactional
    public void deleteComment(Long commentId, Users user) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인 댓글만 삭제할 수 있습니다");
        }
        commentRepository.delete(comment);
    }


    // 게시글의 댓글 및 대댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentIsNull(postId);

        comments.forEach(c -> c.getChildren().size());

        return comments;
    }

}




