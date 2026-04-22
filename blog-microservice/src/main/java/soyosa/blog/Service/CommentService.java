package soyosa.blog.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soyosa.blog.Domain.Comment;
import soyosa.blog.Repository.CommentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BadWordFilter badWordFilter;

    @Autowired
    public CommentService(CommentRepository commentRepository, BadWordFilter badWordFilter) {
        this.commentRepository = commentRepository;
        this.badWordFilter = badWordFilter;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public List<Comment> getAllByBlog(Long blogId){
        return commentRepository.findByBlogBlogId(blogId);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment likeComment(Long id, String userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getLikedByUsers().contains(userId)) {
            comment.getLikedByUsers().add(userId);
            comment.setLikes(comment.getLikedByUsers().size());
        }

        return commentRepository.save(comment);
    }

    public Comment dislikeComment(Long id, String userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getLikedByUsers().contains(userId)) {
            comment.getLikedByUsers().remove(userId);
            comment.setLikes(comment.getLikedByUsers().size());
        }

        return commentRepository.save(comment);
    }

    public Comment reportComment(Long id, String reason) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setReports(comment.getReports() + 1);
        comment.setReportReason(reason);

        return commentRepository.save(comment);
    }

    public Comment createComment(Comment comment) {
        if (badWordFilter.containsBadWords(comment.getContent())) {
            throw new RuntimeException("Content contains inappropriate language.");
        }
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long id, Comment commentDetails) {
        if (badWordFilter.containsBadWords(commentDetails.getContent())) {
            throw new RuntimeException("Content contains inappropriate language.");
        }
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found for id: " + id));
        
        comment.setUserId(commentDetails.getUserId());
        comment.setContent(commentDetails.getContent());
        comment.setLikes(commentDetails.getLikes());
        comment.setReports(commentDetails.getReports());
        comment.setReportReason(commentDetails.getReportReason());
        comment.setCreatedAt(commentDetails.getCreatedAt());
        comment.setBlog(commentDetails.getBlog());
        
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found for id: " + id));
        commentRepository.delete(comment);
    }

    public boolean checkLike(Long id, String userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return comment.getLikedByUsers().contains(userId);
    }
}
