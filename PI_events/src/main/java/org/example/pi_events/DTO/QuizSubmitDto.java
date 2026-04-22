package org.example.pi_events.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitDto {
    private List<Integer> answers;
    private List<Long> questionIds;
}
