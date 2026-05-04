package soyosa.blog.Service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class BadWordFilter {
    // A basic list of inappropriate words. In a real application, this would be more comprehensive.
    private static final List<String> BAD_WORDS = Arrays.asList("badword1", "badword2", "inappropriate");

    public boolean containsBadWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        return BAD_WORDS.stream().anyMatch(lowerText::contains);
    }
}
