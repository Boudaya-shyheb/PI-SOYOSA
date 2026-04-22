package soyosa.blog.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soyosa.blog.Cloudinary.CloudinaryService;
import soyosa.blog.Domain.Blog;
import soyosa.blog.Service.BlogService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/blogs")
@CrossOrigin(origins = "http://localhost:4200")
public class BlogController {

    private final BlogService blogService;
    private final CloudinaryService cloudinaryService;

    public BlogController(BlogService blogService, CloudinaryService cloudinaryService) {
        this.blogService = blogService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public Page<Blog> getBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        return blogService.getBlogs(page, size, search, sort);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBlog(@RequestParam String title, @RequestParam String content, @RequestParam String userId, @RequestParam("images") List<MultipartFile> images) {

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : images) {
            String url = cloudinaryService.uploadImage(file);
            System.out.println(url);
            imageUrls.add(url);
        }
        try {
            return ResponseEntity.ok(blogService.createBlog(title, content, userId, imageUrls));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("inappropriate language")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlog(@PathVariable Long id, @RequestBody Blog blogDetails) {
        try {
            return ResponseEntity.ok(blogService.updateBlog(id, blogDetails));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("inappropriate language")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Blog> likeBlog(@PathVariable Long id, @RequestParam String userId) {
        return ResponseEntity.ok(blogService.likeBlog(id, userId));
    }


    @PutMapping("/{id}/dislike")
    public ResponseEntity<Blog> unlikeBlog(@PathVariable Long id, @RequestParam String userId) {
        return ResponseEntity.ok(blogService.dislikeBlog(id, userId));
    }

    @GetMapping("/check_like/{id}/{userId}")
    public ResponseEntity<Boolean> checkLike(@PathVariable Long id, @PathVariable String userId) {
        return ResponseEntity.ok(blogService.checkLike(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        try {
            blogService.deleteBlog(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
