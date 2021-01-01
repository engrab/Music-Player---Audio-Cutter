package com.naman14.timber.music_cutter;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.naman14.timber.R;

import cn.refactor.lib.colordialog.PromptDialog;
import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;


public class SplashActivity extends ActivityManagePermission
{
    final int SPLASH_TIME_OUT = 10;
    private boolean denay = false;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash);


        askCompactPermissions(new String[]{
                        PermissionUtils.Manifest_READ_EXTERNAL_STORAGE,
                        PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                        PermissionUtils.Manifest_READ_CONTACTS,
                        PermissionUtils.Manifest_WRITE_CONTACTS,
                        PermissionUtils.Manifest_RECORD_AUDIO,
                },
                new PermissionResult()
                {
                    @Override
                    public void permissionGranted()
                    {
                        //permission granted

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            if (hasPermission())
                            {
                                jaumpToActivity();
                            }
                            else
                            {
                                startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                        .setData(Uri.parse("package:" + getPackageName())), MY_PERMISSIONS_REQUEST_WRITE_SETTINGS);
                            }
                        }
                        else
                        {
                            jaumpToActivity();
                        }
                        denay = false;
                    }

                    @Override
                    public void permissionDenied()
                    {
                        denay = true;
                        permissionWarningDialog();
                    }

                    @Override
                    public void permissionForeverDenied()
                    {
                        denay = true;
                        permissionWarningDialog();
                    }

                });


    }

    private void jaumpToActivity()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                startActivity(new Intent(SplashActivity.this, RingdroidSelectActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onBackPressed()
    {
        if (denay)
        {
            super.onBackPressed();
        }
    }

    public void permissionWarningDialog()
    {
        new PromptDialog(SplashActivity.this)
                .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.pdialog_title))
                .setContentText(getString(R.string.pdialog_text))
                .setPositiveListener(getString(R.string.pdialog_setting_btn), new PromptDialog.OnPositiveListener()
                {
                    @Override
                    public void onClick(PromptDialog dialog)
                    {
                        openSettingsApp(SplashActivity.this);
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (Settings.System.canWrite(getApplicationContext()))
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        startActivity(new Intent(SplashActivity.this, RingdroidSelectActivity.class));
                        finish();
                    }
                }, SPLASH_TIME_OUT);
            }
            else
            {
                Toast.makeText(this, "Enable the setting first!", Toast.LENGTH_SHORT).show();
                denay = true;
            }
        }
    }

    private boolean hasPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            return appOps.checkOpNoThrow(AppOpsManager.OPSTR_WRITE_SETTINGS,
                    android.os.Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("MainActivity", "resultCode " + resultCode);
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_WRITE_SETTINGS:
                jaumpToActivity();
                break;
        }
    }
}
