package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.ClubJoinRequestDto;
import org.example.pi_events.DTO.QuizQuestionDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ClubJoinQuizService {

    private final ClubMemberService clubMemberService;
    private static final int QUIZ_SIZE = 10;
    private int passingScore = 7;

    private static class Question {
        final Long id;
        final String text;
        final List<String> options;
        final int correctIndex;

        Question(Long id, String text, List<String> options, int correctIndex) {
            this.id = id;
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }

    private static final List<Question> QUESTIONS = Arrays.asList(
            new Question(1L, "What is the main goal of an associative club?", Arrays.asList("Making profit", "Sharing common interests", "Selling products", "Enforcing laws"), 1),
            new Question(2L, "What does volunteering mean?", Arrays.asList("Working for money", "Giving your time for free", "A mandatory job", "Working under pressure"), 1),
            new Question(3L, "What does NGO stand for?", Arrays.asList("National Group Organization", "Non-Governmental Organization", "New Global Order", "Non-Government Office"), 1),
            new Question(4L, "Why is teamwork important in a club?", Arrays.asList("To compete against each other", "To achieve goals effectively together", "To make one person do all the work", "To avoid doing any work"), 1),
            new Question(5L, "What is fundraising?", Arrays.asList("Collecting money for a cause", "Running in a marathon", "Spending club money", "Writing a book"), 0),
            new Question(6L, "How does joining a club benefit you?", Arrays.asList("It gives you a salary", "It guarantees graduation", "It develops soft skills and network", "It helps avoid classes"), 2),
            new Question(7L, "What is the typical role of a club president?", Arrays.asList("To do all the tasks alone", "To guide and manage the club's activities", "To never attend meetings", "To block new members"), 1),
            new Question(8L, "What does 'community service' mean?", Arrays.asList("Voluntary work to help people in a community", "A paid government job", "Cleaning your own room", "An exclusive club for elites"), 0),
            new Question(9L, "Which of these is a key trait of a good club member?", Arrays.asList("Selfishness", "Commitment", "Laziness", "Dishonesty"), 1),
            new Question(10L, "What should you do if you disagree with a club decision?", Arrays.asList("Leave immediately", "Start a fight", "Discuss it respectfully", "Ignore everyone"), 2),
            new Question(11L, "What is active listening during a club meeting?", Arrays.asList("Waiting to speak only", "Checking your phone while others talk", "Paying attention and asking clear questions", "Interrupting to correct everyone"), 2),
            new Question(12L, "Why should club members respect deadlines?", Arrays.asList("To avoid responsibilities", "To keep projects organized and reliable", "To make meetings longer", "To reduce collaboration"), 1),
            new Question(13L, "What is the best way to handle a conflict between members?", Arrays.asList("Spread rumors", "Ignore the issue", "Discuss calmly and find common ground", "Publicly shame one side"), 2),
            new Question(14L, "Which action shows leadership in a student club?", Arrays.asList("Blaming others for mistakes", "Encouraging teamwork and accountability", "Avoiding decisions", "Keeping information secret"), 1),
            new Question(15L, "What is the purpose of a club action plan?", Arrays.asList("To define goals, tasks, and timelines", "To punish inactive members", "To remove all creativity", "To replace communication"), 0),
            new Question(16L, "What does inclusiveness mean in a club context?", Arrays.asList("Accepting only close friends", "Giving equal chances to different members", "Selecting members by popularity", "Ignoring new members"), 1),
            new Question(17L, "How can a club improve event attendance?", Arrays.asList("Announce details early and communicate clearly", "Share event information after it ends", "Limit announcements to one person", "Avoid social media and posters"), 0),
            new Question(18L, "What is a realistic way to measure a club project's impact?", Arrays.asList("Counting only social media likes", "Collecting feedback and tracking outcomes", "Asking only organizers", "Ignoring participant opinions"), 1),
            new Question(19L, "What is the best behavior when representing your club publicly?", Arrays.asList("Use respectful communication and professionalism", "Argue aggressively", "Share private data about members", "Criticize partners in public"), 0),
            new Question(20L, "Why is transparency important in club finances?", Arrays.asList("It builds trust and accountability", "It makes budgeting impossible", "It allows hidden spending", "It reduces member engagement"), 0),
            new Question(21L, "What should you do before proposing a new club project?", Arrays.asList("Check feasibility, resources, and member interest", "Start spending immediately", "Skip team discussion", "Avoid setting objectives"), 0),
            new Question(22L, "How should tasks be assigned in a strong club team?", Arrays.asList("Based on favoritism", "Randomly without explanation", "Based on skills and availability", "Only to new members"), 2),
            new Question(23L, "What is constructive feedback?", Arrays.asList("Personal attacks", "Clear suggestions focused on improvement", "Silence after mistakes", "Public humiliation"), 1),
            new Question(24L, "Which practice improves collaboration in a club committee?", Arrays.asList("Documenting decisions and sharing updates", "Changing plans without notice", "Avoiding regular follow-ups", "Ignoring unresolved issues"), 0),
            new Question(25L, "What is the most ethical way to use club funds?", Arrays.asList("Spend based on personal preference", "Use them only for approved club objectives", "Keep records optional", "Prioritize private benefits"), 1)
    );

    private static final Map<Long, Question> QUESTION_BY_ID = QUESTIONS.stream()
            .collect(Collectors.toMap(question -> question.id, question -> question));

    public List<QuizQuestionDto> getQuizQuestions() {
        if (QUESTIONS.size() < QUIZ_SIZE) {
            throw new IllegalStateException("Quiz question bank is too small.");
        }
        List<Question> shuffledQuestions = QUESTIONS.stream().collect(Collectors.toList());
        Collections.shuffle(shuffledQuestions);
        return shuffledQuestions.stream()
                .limit(QUIZ_SIZE)
                .map(q -> new QuizQuestionDto(q.id, q.text, q.options))
                .collect(Collectors.toList());
    }

    public String evaluateQuizAndJoin(Long clubId, String userEmail, List<Integer> answers, List<Long> questionIds) {
        if (answers == null || questionIds == null || answers.size() != QUIZ_SIZE || questionIds.size() != QUIZ_SIZE || answers.size() != questionIds.size()) {
            throw new IllegalArgumentException("You must submit exactly " + QUIZ_SIZE + " answers.");
        }

        List<Question> selectedQuestions = questionIds.stream()
                .map(this::resolveQuestionById)
                .collect(Collectors.toList());

        int score = (int) IntStream.range(0, QUIZ_SIZE)
                .filter(i -> isCorrectAnswer(selectedQuestions.get(i), answers.get(i)))
                .count();

        ClubJoinRequestDto pendingRequest = null;
        try {
            pendingRequest = clubMemberService.joinClub(clubId, userEmail);
        } catch (Exception e) {
            // Check if already a member
            if (e.getMessage() != null && e.getMessage().contains("already a member")) {
                return "You are already a member of this club.";
            }
            
            // Try to find an existing request (e.g. if already pending)
            List<ClubJoinRequestDto> myRequests = clubMemberService.getMyJoinRequestStatuses(userEmail);
            pendingRequest = myRequests.stream()
                    .filter(r -> r.getClubId().equals(clubId))
                    .findFirst()
                    .orElse(null);
            
            if (pendingRequest == null) {
                throw e; // Rethrow if we REALLY can't create or find a request
            }
        }

        if (score >= passingScore) {
            clubMemberService.approveJoinRequest(pendingRequest.getId());
            return "Congratulations! You scored " + score + "/" + QUIZ_SIZE + " and your join request has been approved.";
        }
        clubMemberService.rejectJoinRequest(pendingRequest.getId());
        return "Sorry, you scored " + score + "/" + QUIZ_SIZE + ". Your join request has been rejected.";
    }

    public int getPassingScore() {
        return passingScore;
    }

    public void updatePassingScore(int passingScore) {
        if (passingScore < 1 || passingScore > QUIZ_SIZE) {
            throw new IllegalArgumentException("Passing score must be between 1 and " + QUIZ_SIZE + ".");
        }
        this.passingScore = passingScore;
    }

    private Question resolveQuestionById(Long questionId) {
        Question question = QUESTION_BY_ID.get(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Invalid quiz question selection.");
        }
        return question;
    }

    private boolean isCorrectAnswer(Question question, Integer answerIndex) {
        if (answerIndex == null || answerIndex < 0 || answerIndex >= question.options.size()) {
            return false;
        }
        return answerIndex.equals(question.correctIndex);
    }
}
