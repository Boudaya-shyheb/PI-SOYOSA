package soyosa.blog.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import soyosa.blog.Domain.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Page<Blog> findByTitleContainingIgnoreCaseOrUserIdContainingIgnoreCase(String title, String userId, Pageable pageable);

    @Query("""
    SELECT b FROM Blog b
    LEFT JOIN Comment c ON c.blog = b
    GROUP BY b
    ORDER BY COUNT(c) DESC
""")
    Page<Blog> findAllOrderByCommentCountDesc(Pageable pageable);

}
