package com.naman14.timber.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.naman14.timber.R;

import cn.refactor.lib.colordialog.PromptDialog;
import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

public class SplashActivity extends ActivityManagePermission
{
    InterstitialAd mInterstitialAd;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.app_id));

//        PurchasePref purchasePref = new PurchasePref(getApplicationContext());
//        if (purchasePref.getItemDetail().equals("") && purchasePref.getProductId().equals("")) {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                startActivity(new Intent(SplashActivity.this, StartActivity.class));
                finish();
            }
        });
//        }


        askCompactPermissions(new String[]{PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils
                .Manifest_WRITE_EXTERNAL_STORAGE}, new PermissionResult() {
            @Override
            public void permissionGranted() {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            startActivity(new Intent(SplashActivity.this, StartActivity.class));
                            finish();
                        }
                    }
                }, 4000);
            }

            @Override
            public void permissionDenied() {
                permissionWarningDialog();
            }
            @Override
            public void permissionForeverDenied() {
                permissionWarningDialog();
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
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
}
