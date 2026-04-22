package soyosa.blog.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import soyosa.blog.Domain.Blog;
import soyosa.blog.Repository.BlogRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final BadWordFilter badWordFilter;

    @Autowired
    public BlogService(BlogRepository blogRepository, BadWordFilter badWordFilter) {
        this.blogRepository = blogRepository;
        this.badWordFilter = badWordFilter;
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    public Blog createBlog(String title, String userId, String content,List<String> imageUrls) {
        if (badWordFilter.containsBadWords(title) || badWordFilter.containsBadWords(content)) {
            throw new RuntimeException("Content contains inappropriate language.");
        }

        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setUserId(userId);
        blog.setContent(content);
        blog.setCreatedAt(java.time.LocalDateTime.now());
        blog.setLikes(0);
        blog.setImages(imageUrls);

        return blogRepository.save(blog);
    }

    public Blog updateBlog(Long id, Blog blogDetails) {
        if (badWordFilter.containsBadWords(blogDetails.getTitle()) || badWordFilter.containsBadWords(blogDetails.getContent())) {
            throw new RuntimeException("Content contains inappropriate language.");
        }
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found for id: " + id));
        
        blog.setUserId(blogDetails.getUserId());
        blog.setTitle(blogDetails.getTitle());
        blog.setCreatedAt(blogDetails.getCreatedAt());
        blog.setLikes(blogDetails.getLikes());
        blog.setImages(blogDetails.getImages());
        blog.setContent(blogDetails.getContent());
        
        return blogRepository.save(blog);
    }

    public Blog likeBlog(Long id, String userId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getLikedByUsers().contains(userId)) {
            blog.getLikedByUsers().add(userId);
            blog.setLikes(blog.getLikedByUsers().size());
        }

        return blogRepository.save(blog);
    }

    public Blog dislikeBlog(Long id, String userId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        if (blog.getLikedByUsers().contains(userId)) {
            blog.getLikedByUsers().remove(userId);
            blog.setLikes(blog.getLikedByUsers().size());
        }
        
        return blogRepository.save(blog);
    }

    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found for id: " + id));
        blogRepository.delete(blog);
    }

    public boolean checkLike(Long id, String userId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        return blog.getLikedByUsers().contains(userId);
    }

    public Page<Blog> getBlogs(int page, int size, String search, String sort) {

        if ("mostCommented".equals(sort)) {
            return blogRepository.findAllOrderByCommentCountDesc(
                    PageRequest.of(page, size)
            );
        }

        Sort sorting;

        switch (sort) {
            case "mostLiked":
                sorting = Sort.by("likes").descending();
                break;
            default:
                sorting = Sort.by("createdAt").descending();
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        if (search != null && !search.trim().isEmpty()) {
            return blogRepository
                    .findByTitleContainingIgnoreCaseOrUserIdContainingIgnoreCase(
                            search,
                            search,
                            pageable
                    );
        }

        return blogRepository.findAll(pageable);
    }

}
