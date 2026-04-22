package soyosa.blog.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soyosa.blog.Domain.Comment;
import soyosa.blog.Service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "http://localhost:4200")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.getAllComments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all-by-blog/{blogId}")
    public List<Comment> getAllCommentsByBlog(@PathVariable Long blogId) {
        return commentService.getAllByBlog(blogId);
    }

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody Comment comment) {
        try {
            return ResponseEntity.ok(commentService.createComment(comment));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("inappropriate language")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody Comment commentDetails) {
        try {
            return ResponseEntity.ok(commentService.updateComment(id, commentDetails));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("inappropriate language")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Comment> likeComment(
            @PathVariable Long id,
            @RequestParam String userId
    ) {
        try {
            return ResponseEntity.ok(
                    commentService.likeComment(id, userId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/dislike")
    public ResponseEntity<Comment> dislikeComment(
            @PathVariable Long id,
            @RequestParam String userId
    ) {
        try {
            return ResponseEntity.ok(
                    commentService.dislikeComment(id, userId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/report")
    public ResponseEntity<Comment> reportComment(@PathVariable Long id, @RequestParam String reason) {
        try {
            return ResponseEntity.ok(commentService.reportComment(id, reason));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/check_like/{id}/{userId}")
    public ResponseEntity<Boolean> checkLike(@PathVariable Long id, @PathVariable String userId) {
        return ResponseEntity.ok(commentService.checkLike(id, userId));
    }
}
