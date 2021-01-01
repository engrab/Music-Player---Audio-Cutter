package com.naman14.timber.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.naman14.timber.R;

public class StartActivity extends AppCompatActivity{

    InterstitialAd mInterstitialAd;
    AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAdView = this.findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
        mAdView.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener()
        {
            @Override
            public void onAdClosed()
            {
                super.onAdClosed();

                startActivity(new Intent(StartActivity.this, MainActivity.class));

            }
        });

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd!= null && mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                }
                else {
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(StartActivity.this)
                .setTitle("Confirmation")
                .setMessage("Do you want to exit?")
                .setNeutralButton("Rate us", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            Intent intentMore = new Intent(Intent.ACTION_VIEW);
                            intentMore.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=en"));
                            if (intentMore.resolveActivity(getPackageManager()) != null) {
                                startActivity(intentMore);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create().show();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }


}
