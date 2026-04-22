package com.example.gameservice.config;

import com.example.gameservice.entities.*;
import com.example.gameservice.reposotories.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GameRepository gameRepository;

    @Override
    public void run(String... args) throws Exception {
        if (gameRepository.count() == 0) {
            
            // QUIZ GAMES

            Game quizEasy = Game.builder().title("Easy Grammar Rules").type(GameType.QUIZ).level("EASY").contents(new ArrayList<>()).build();
            quizEasy.getContents().add(GameContent.builder().question("What is the plural of 'child'?").optionA("childs").optionB("children").optionC("childrens").optionD("child").correctAnswer("children").build());
            quizEasy.getContents().add(GameContent.builder().question("Which article goes before 'apple'?").optionA("a").optionB("an").optionC("the").optionD("none").correctAnswer("an").build());
            quizEasy.getContents().add(GameContent.builder().question("I ___ going to the store.").optionA("is").optionB("are").optionC("am").optionD("be").correctAnswer("am").build());
            quizEasy.getContents().add(GameContent.builder().question("He ___ a book every day.").optionA("read").optionB("reads").optionC("reading").optionD("is read").correctAnswer("reads").build());
            quizEasy.getContents().add(GameContent.builder().question("___ you like pizza?").optionA("Do").optionB("Does").optionC("Are").optionD("Is").correctAnswer("Do").build());
            quizEasy.getContents().forEach(c -> c.setGame(quizEasy));
            gameRepository.save(quizEasy);

            Game quizMedium = Game.builder().title("Medium Tense Test").type(GameType.QUIZ).level("MEDIUM").contents(new ArrayList<>()).build();
            quizMedium.getContents().add(GameContent.builder().question("By the time you arrive, I ___ left.").optionA("will have").optionB("would have").optionC("have").optionD("had").correctAnswer("will have").build());
            quizMedium.getContents().add(GameContent.builder().question("If I ___ you, I would study harder.").optionA("was").optionB("am").optionC("were").optionD("be").correctAnswer("were").build());
            quizMedium.getContents().add(GameContent.builder().question("She insisted that he ___ the party.").optionA("attends").optionB("attend").optionC("attending").optionD("attended").correctAnswer("attend").build());
            quizMedium.getContents().add(GameContent.builder().question("Despite ___ tired, he kept working.").optionA("he was").optionB("being").optionC("of being").optionD("was").correctAnswer("being").build());
            quizMedium.getContents().add(GameContent.builder().question("Hardly ___ entered when the phone rang.").optionA("had I").optionB("I had").optionC("have I").optionD("I have").correctAnswer("had I").build());
            quizMedium.getContents().forEach(c -> c.setGame(quizMedium));
            gameRepository.save(quizMedium);

            Game quizHard = Game.builder().title("Hard Vocabulary Quiz").type(GameType.QUIZ).level("HARD").contents(new ArrayList<>()).build();
            quizHard.getContents().add(GameContent.builder().question("Choose the synonym for 'Ephemeral'").optionA("Endless").optionB("Fleeting").optionC("Solid").optionD("Bright").correctAnswer("Fleeting").build());
            quizHard.getContents().add(GameContent.builder().question("'To let the cat out of the bag' means:").optionA("Free a pet").optionB("Reveal a secret").optionC("Make a mistake").optionD("Be clumsy").correctAnswer("Reveal a secret").build());
            quizHard.getContents().add(GameContent.builder().question("Which sentence is grammatically correct?").optionA("Whom did you say was calling?").optionB("Who did you say was calling?").optionC("Who did you say were calling?").optionD("Whom did you said was calling?").correctAnswer("Who did you say was calling?").build());
            quizHard.getContents().add(GameContent.builder().question("He is the person ___ we think is best suited.").optionA("who").optionB("whom").optionC("which").optionD("whose").correctAnswer("who").build());
            quizHard.getContents().add(GameContent.builder().question("What does 'ubiquitous' mean?").optionA("Rare").optionB("Expensive").optionC("Present everywhere").optionD("Hidden").correctAnswer("Present everywhere").build());
            quizHard.getContents().forEach(c -> c.setGame(quizHard));
            gameRepository.save(quizHard);

            // WORD SCRAMBLE GAMES

            Game wsEasy = Game.builder().title("Easy Vocabulary Scramble").type(GameType.WORD_SCRAMBLE).level("EASY").contents(new ArrayList<>()).build();
            wsEasy.getContents().add(GameContent.builder().word("APPLE").build());
            wsEasy.getContents().add(GameContent.builder().word("HOUSE").build());
            wsEasy.getContents().add(GameContent.builder().word("WATER").build());
            wsEasy.getContents().add(GameContent.builder().word("CHAIR").build());
            wsEasy.getContents().add(GameContent.builder().word("HAPPY").build());
            wsEasy.getContents().forEach(c -> c.setGame(wsEasy));
            gameRepository.save(wsEasy);

            Game wsMedium = Game.builder().title("Medium Nouns Scramble").type(GameType.WORD_SCRAMBLE).level("MEDIUM").contents(new ArrayList<>()).build();
            wsMedium.getContents().add(GameContent.builder().word("TEACHER").build());
            wsMedium.getContents().add(GameContent.builder().word("STUDENT").build());
            wsMedium.getContents().add(GameContent.builder().word("MORNING").build());
            wsMedium.getContents().add(GameContent.builder().word("COUNTRY").build());
            wsMedium.getContents().add(GameContent.builder().word("PICTURE").build());
            wsMedium.getContents().forEach(c -> c.setGame(wsMedium));
            gameRepository.save(wsMedium);

            Game wsHard = Game.builder().title("Hard Advanced Scramble").type(GameType.WORD_SCRAMBLE).level("HARD").contents(new ArrayList<>()).build();
            wsHard.getContents().add(GameContent.builder().word("DICTIONARY").build());
            wsHard.getContents().add(GameContent.builder().word("VOCABULARY").build());
            wsHard.getContents().add(GameContent.builder().word("LITERATURE").build());
            wsHard.getContents().add(GameContent.builder().word("EXPERIENCE").build());
            wsHard.getContents().add(GameContent.builder().word("KNOWLEDGE").build());
            wsHard.getContents().forEach(c -> c.setGame(wsHard));
            gameRepository.save(wsHard);

            // CROSSWORD GAMES

            Game cwEasy = Game.builder().title("Easy Reading Clues").type(GameType.CROSSWORD).level("EASY").contents(new ArrayList<>()).build();
            cwEasy.getContents().add(GameContent.builder().clue("Color of the sky").answer("BLUE").build());
            cwEasy.getContents().add(GameContent.builder().clue("Opposite of hot").answer("COLD").build());
            cwEasy.getContents().add(GameContent.builder().clue("A domestic feline").answer("CAT").build());
            cwEasy.getContents().add(GameContent.builder().clue("You read this").answer("BOOK").build());
            cwEasy.getContents().add(GameContent.builder().clue("Opposite of day").answer("NIGHT").build());
            cwEasy.getContents().forEach(c -> c.setGame(cwEasy));
            gameRepository.save(cwEasy);

            Game cwMedium = Game.builder().title("Medium Deduction").type(GameType.CROSSWORD).level("MEDIUM").contents(new ArrayList<>()).build();
            cwMedium.getContents().add(GameContent.builder().clue("A place to study").answer("LIBRARY").build());
            cwMedium.getContents().add(GameContent.builder().clue("Used to cut paper").answer("SCISSORS").build());
            cwMedium.getContents().add(GameContent.builder().clue("A season with snow").answer("WINTER").build());
            cwMedium.getContents().add(GameContent.builder().clue("Yellow fruit").answer("BANANA").build());
            cwMedium.getContents().add(GameContent.builder().clue("Used to unlock doors").answer("KEYS").build());
            cwMedium.getContents().forEach(c -> c.setGame(cwMedium));
            gameRepository.save(cwMedium);

            Game cwHard = Game.builder().title("Hard Complex Clues").type(GameType.CROSSWORD).level("HARD").contents(new ArrayList<>()).build();
            cwHard.getContents().add(GameContent.builder().clue("An optical illusion").answer("MIRAGE").build());
            cwHard.getContents().add(GameContent.builder().clue("Scientific study of life").answer("BIOLOGY").build());
            cwHard.getContents().add(GameContent.builder().clue("Extremely beautiful").answer("GORGEOUS").build());
            cwHard.getContents().add(GameContent.builder().clue("Lacking flavor").answer("BLAND").build());
            cwHard.getContents().add(GameContent.builder().clue("A person's face or expression").answer("COUNTENANCE").build());
            cwHard.getContents().forEach(c -> c.setGame(cwHard));
            gameRepository.save(cwHard);

            System.out.println("Database seeded successfully.");
        }
    }
}
