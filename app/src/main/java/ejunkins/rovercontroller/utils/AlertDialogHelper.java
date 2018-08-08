package ejunkins.rovercontroller.utils;

import android.app.AlertDialog;
import android.content.Context;

import ejunkins.rovercontroller.R;

public class AlertDialogHelper {

    public static AlertDialog showConnectionErrorDialog(Context context, int resId) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.alert_title_connection_error)
                .setMessage(resId)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }
}
