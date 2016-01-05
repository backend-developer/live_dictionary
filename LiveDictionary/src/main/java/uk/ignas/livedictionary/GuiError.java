package uk.ignas.livedictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class GuiError {
    private Activity activity;

    public GuiError(Activity activity) {
        this.activity = activity;
    }

    public void showErrorDialogAndExitActivity(String message) {
        showErrorDialog(message, true);
    }

    public void showErrorDialogAndContinue(String message) {
        showErrorDialog(message, false);
    }

    public void showErrorDialog(String message, final boolean shouldExitActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Live Dictionary")
                .setMessage("Error occured:" + message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (shouldExitActivity) {
                            activity.finish();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
