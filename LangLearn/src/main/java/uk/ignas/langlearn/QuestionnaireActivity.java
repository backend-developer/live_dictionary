package uk.ignas.langlearn;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

public class QuestionnaireActivity extends BaseActivity {

    @Inject
    QuestionsStorage questionsStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QuestionnaireApplication application = (QuestionnaireApplication) getApplication();

        TextView questionLabel = (TextView) findViewById(R.id.question_label);
        try {
            questionLabel.setText(questionsStorage.getQuestions().keySet().iterator().next().getOriginalWord());
        } catch (RuntimeException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("LangLearn")
                    .setMessage("Invalid CSV format: No Title")
                    .setPositiveButton("button", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        final TextView correctAnswerView = (TextView) findViewById(R.id.correct_answer);
        Button button = (Button) findViewById(R.id.submit_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correctAnswerView.setText(questionsStorage.getQuestions().keySet().iterator().next().getTranslatedWord());
            }
        });

        TextView answerInput = (TextView) findViewById(R.id.answer_input);
    //    answerInput.setText("");
    }
}
