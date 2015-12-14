package uk.ignas.langlearn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Questionnaire {
    private List<Translation> questions;
    private Random random = new Random();

    public Questionnaire(QuestionBase questionBase, Random random) {
        this(questionBase);
        this.random = random;
    }

    public Questionnaire(QuestionBase questions) {
        this.questions = new ArrayList(questions.getQuestions().keySet());
    }

    public String drawQuestion() {
        if (questions.size() == 0) {
            throw new QuestionnaireException("no questions found");
        }
        return questions.get(random.nextInt(questions.size())).getOriginalWord();
    }
}
