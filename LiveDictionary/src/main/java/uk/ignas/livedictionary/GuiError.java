package uk.ignas.livedictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

public class GuiError {
    private Activity activity;

    public GuiError(Activity activity) {
        this.activity = activity;
    }

    public void showErrorDialogAndExitActivity(Exception e) {
        showErrorDialog(e, true);
    }

    public void showErrorDialogAndContinue(Exception e) {
        showErrorDialog(e, false);
    }

    private void showErrorDialog(Exception e, final boolean shouldExitActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        Log.e(GuiError.class.getName(), "Error ocurred", e);
        builder.setTitle("Live Dictionary")
                .setMessage("Error occured:" + e.getMessage())
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
