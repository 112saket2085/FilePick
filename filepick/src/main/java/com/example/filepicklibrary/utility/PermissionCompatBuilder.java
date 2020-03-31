package com.example.filepicklibrary.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.filepicklibrary.R;

@SuppressWarnings("unused")
public final class PermissionCompatBuilder {

    /**
     * Define Generic REQUEST CODES for PERMISSIONS
     */
    public interface Code {
        int REQ_CODE_READ_SMS_PERMISSION  = 101;
        int REQ_CODE_READ_EXTERNAL_STORAGE_PERMISSION  = 102;
        int REQ_CODE_CALL_PHONE = 103;
        int REQ_CODE_WRITE_STORAGE = 104;
        int REQ_CODE_READ_PHONE_STATE_PERMISSION = 105;
        int REQ_CODE_CAMERA = 106;
    }

    /**
     * Determine whether <em>AppBuilder</em> have been granted a particular permission.
     * @param permission The name of the permission being checked.
     * @return {@link PackageManager#PERMISSION_GRANTED} if you have the
     * permission, or {@link PackageManager#PERMISSION_DENIED} if not.
     */
    public static int checkSelfPermission(Context context, String permission) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context == null || permission == null) {
                return PackageManager.PERMISSION_GRANTED;
            }
            return ContextCompat.checkSelfPermission(context, permission);
        }
        return PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests permissions to be granted to this application. These permissions
     * must be requested in your manifest, they should not be granted to your app,
     * and they should have protection level {@link android.content.pm.PermissionInfo
     * #PROTECTION_DANGEROUS dangerous}, regardless whether they are declared by
     * the platform or a third-party app.
     *
     * @param activity The target activity.
     * @param permissions The requested permissions.
     * @param requestCode Application specific request code to match with a result
     */
    public static void requestPermissions(final @NonNull Activity activity,
                                          final @NonNull String[] permissions, final int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * Requests permissions to be granted to this application. These permissions
     * must be requested in your manifest, they should not be granted to your app,
     * and they should have protection level {@link android.content.pm.PermissionInfo
     * #PROTECTION_DANGEROUS dangerous}, regardless whether they are declared by
     * the platform or a third-party app.
     *
     * Permission result will be delivered to {@link Fragment#onRequestPermissionsResult}
     *
     * @param fragment The target fragment.
     * @param permissions The requested permissions.
     * @param requestCode Application specific request code to match with a result
     */
    public static void requestPermissions(final @NonNull Fragment fragment,
                                          final @NonNull String[] permissions, final int requestCode) {
        fragment.requestPermissions(permissions, requestCode);
    }

    /**
     * Gets whether you should show UI with rationale for requesting a permission.
     * You should do this only if you do not have the permission and the context in
     * which the permission is requested does not clearly communicate to the user
     * what would be the benefit from granting this permission.
     *
     * @param activity calling activity
     * @param permission permission required
     * @return Whether you can show permission rationale UI.
     */
    public static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,
                                                               @NonNull String permission) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Gets whether you should show UI with rationale for requesting a permission.
     * You should do this only if you do not have the permission and the context in
     * which the permission is requested does not clearly communicate to the user
     * what would be the benefit from granting this permission.
     *
     * @param fragment calling fragment
     * @param permission permission required
     * @return Whether you can show permission rationale UI.
     */
    public static boolean shouldShowRequestPermissionRationale(@NonNull Fragment fragment,
                                                               @NonNull String permission) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return fragment.shouldShowRequestPermissionRationale(permission);
        } else {
            return false;
        }
    }

    /**
     * Show Rational message dialog with callback for permission
     * @param activity calling context
     * @param permission permission requested
     * @param message dialog message to display
     * @param callback callback
     */
    public static void showRequestPermissionRationaleDialog(Activity activity, final String permission, String message, final RationalDialogCallback callback, boolean isCancellable) {
        if(activity != null && !activity.isFinishing()) {
            try {
                String title = activity.getString(R.string.dialog_title_permission_rational);
                String positiveButtonAllow = activity.getString(R.string.permisson_allow_button_text);
                String negativeButtonDeny = activity.getString(R.string.dialog_cancel_btn_text);
                DialogBuilder.showGenericDialog(activity, isCancellable, title, message, positiveButtonAllow, negativeButtonDeny, new DialogBuilder.DialogCallback(){
                    @Override
                    public void onPositiveButtonClick(View view) {
                        if(callback != null) {
                            callback.allowedRequest(permission);
                        }
                    }

                    @Override
                    public void onNegativeButtonClick(View view) {
                        if(callback != null) {
                            callback.deniedRequest(permission);
                        }
                    }
                });
            } catch (Exception ignore) {}
        }
    }

    /**
     * Show permission denied dialog from where user go to the setting page
     * to grant access to particular permission
     * @param activity calling context
     * @param message dialog message to display
     */
    public static void showPermissionDeniedDialog(final Activity activity, final int requestCode, String message) {
        if (activity != null && !activity.isFinishing()) {
            try {
                String title = activity.getString(R.string.dialog_title_permission_rational);
                String positiveButtonAllow = activity.getString(R.string.dialog_btn_ok_text);
                String negativeButtonDeny = activity.getString(R.string.dialog_cancel_btn_text);
                DialogBuilder.showGenericDialog(activity, true, title, message, positiveButtonAllow, negativeButtonDeny, new DialogBuilder.DialogCallback() {
                    @Override
                    public void onPositiveButtonClick(View view) {
                        showAppPermissionSettings(activity, requestCode);
                    }

                    @Override
                    public void onNegativeButtonClick(View view) {
                    }
                });
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Launch AppBuilder Permission setting screen from activity with current package.
     * @param activity caller activity.
     */
    public static void showAppPermissionSettings(Activity activity, final int requestCode) {
        Intent intent = getAppPermissionSettingIntent(activity.getPackageName());
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Launch AppBuilder Permission setting screen from fragment with current package.
     * @param fragment caller fragment.
     */
    public static void showAppPermissionSettings(Fragment fragment, final int requestCode) {
        Intent intent = getAppPermissionSettingIntent(fragment.getContext().getPackageName());
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * Return AppBuilder Setting intent
     * @param packageName calling package
     * @return Intent
     */
    private static Intent getAppPermissionSettingIntent(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        return intent;
    }

    public static class RationalDialogCallback {
        public void allowedRequest(String permission) {}
        public void deniedRequest(String permission) {}
    }
}
