package com.esprit.microservice.trainingservice.controllers;

import com.esprit.microservice.trainingservice.dto.PlacementTestSubmissionDTO;
import com.esprit.microservice.trainingservice.entities.Level;
import com.esprit.microservice.trainingservice.entities.PlacementTestQuestion;
import com.esprit.microservice.trainingservice.entities.UserPlacementResult;
import com.esprit.microservice.trainingservice.repositories.PlacementTestQuestionRepository;
import com.esprit.microservice.trainingservice.repositories.UserPlacementResultRepository;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/placement-test")
public class PlacementTestController {

    @Autowired
    private PlacementTestQuestionRepository questionRepository;

    @Autowired
    private UserPlacementResultRepository resultRepository;

    @GetMapping("/questions")
    public List<PlacementTestQuestion> getQuestions() {
        List<PlacementTestQuestion> all = questionRepository.findAll();
        Collections.shuffle(all);
        return all.stream().limit(10).collect(Collectors.toList());
    }

    @PostMapping("/submit")
    public UserPlacementResult submitTest(@RequestBody PlacementTestSubmissionDTO submission, @AuthenticationPrincipal SecurityUser user) {
        List<PlacementTestQuestion> questions = questionRepository.findAllById(submission.getAnswers().keySet());
        int score = 0;
        
        for (PlacementTestQuestion q : questions) {
            Integer selectedIndex = submission.getAnswers().get(q.getId());
            if (selectedIndex != null && selectedIndex == q.getCorrectOptionIndex()) {
                score++;
            }
        }

        // Determine Level based on score (Simplified mapping for 10 questions)
        Level level;
        if (score <= 2) level = Level.A1;
        else if (score <= 4) level = Level.A2;
        else if (score <= 6) level = Level.B1;
        else if (score <= 8) level = Level.B2;
        else if (score <= 9) level = Level.C1;
        else level = Level.C2;

        UserPlacementResult result = resultRepository.findByStudentId(user.getId()).orElse(new UserPlacementResult());
        result.setStudentId(user.getId());
        result.setDeterminedLevel(level);
        result.setScore(score);
        
        return resultRepository.save(result);
    }

    @GetMapping("/result")
    public UserPlacementResult getMyResult(@AuthenticationPrincipal SecurityUser user) {
        return resultRepository.findByStudentId(user.getId()).orElse(null);
    }
}
