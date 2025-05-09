/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.naman14.timber.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;
import com.google.android.gms.cast.framework.media.widget.MiniControllerFragment;
import com.naman14.timber.MusicPlayer;
import com.naman14.timber.R;
import com.naman14.timber.cast.ExpandedControlsActivity;
import com.naman14.timber.cast.SimpleSessionManagerListener;
import com.naman14.timber.cast.WebServer;
import com.naman14.timber.fragments.AlbumDetailFragment;
import com.naman14.timber.fragments.ArtistDetailFragment;
import com.naman14.timber.fragments.FoldersFragment;
import com.naman14.timber.fragments.MainFragment;
import com.naman14.timber.fragments.PlaylistFragment;
import com.naman14.timber.fragments.QueueFragment;
import com.naman14.timber.music_cutter.SplashActivity;
import com.naman14.timber.permissions.Nammu;
import com.naman14.timber.permissions.PermissionCallback;
import com.naman14.timber.slidinguppanel.SlidingUpPanelLayout;
import com.naman14.timber.subfragments.LyricsFragment;
import com.naman14.timber.utils.Constants;
import com.naman14.timber.utils.Helpers;
import com.naman14.timber.utils.NavigationUtils;
import com.naman14.timber.utils.TimberUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    private SlidingUpPanelLayout panelLayout;
    private NavigationView navigationView;
    private TextView songtitle, songartist;
    private ImageView albumart;
    private String action;
    private Map<String, Runnable> navigationMap = new HashMap<>();
    private Handler navDrawerRunnable = new Handler();
    private Runnable runnable;
    private DrawerLayout mDrawerLayout;
    private boolean isDarkTheme;
    private InterstitialAd mInterstitialAd;


    private Runnable navigateLibrary = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_library);
            Fragment fragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();

        }
    };

    private Runnable navigatePlaylist = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_playlists);
            Fragment fragment = new PlaylistFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };

    private Runnable navigateFolder = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_folders);
            Fragment fragment = new FoldersFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };

    private Runnable navigateQueue = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_queue);
            Fragment fragment = new QueueFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };

    private Runnable navigateAlbum = new Runnable() {
        public void run() {
            long albumID = getIntent().getExtras().getLong(Constants.ALBUM_ID);
            Fragment fragment = AlbumDetailFragment.newInstance(albumID, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };

    private Runnable navigateArtist = new Runnable() {
        public void run() {
            long artistID = getIntent().getExtras().getLong(Constants.ARTIST_ID);
            Fragment fragment = ArtistDetailFragment.newInstance(artistID, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };

    private Runnable navigateLyrics = new Runnable() {
        public void run() {
            Fragment fragment = new LyricsFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };

    private Runnable navigateNowplaying = new Runnable() {
        public void run() {
            navigateLibrary.run();
            startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
        }
    };

    private final PermissionCallback permissionReadstorageCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            loadEverything();
        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {

        action = getIntent().getAction();

        isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();

                mInterstitialAd.loadAd(new AdRequest.Builder().build());

            }
        });


        navigationMap.put(Constants.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(Constants.NAVIGATE_PLAYLIST, navigatePlaylist);
        navigationMap.put(Constants.NAVIGATE_QUEUE, navigateQueue);
        navigationMap.put(Constants.NAVIGATE_NOWPLAYING, navigateNowplaying);
        navigationMap.put(Constants.NAVIGATE_ALBUM, navigateAlbum);
        navigationMap.put(Constants.NAVIGATE_ARTIST, navigateArtist);
        navigationMap.put(Constants.NAVIGATE_LYRICS, navigateLyrics);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        panelLayout = findViewById(R.id.sliding_layout);

        navigationView = findViewById(R.id.nav_view);
        View header = navigationView.inflateHeaderView(R.layout.nav_header);

        albumart = header.findViewById(R.id.album_art);
        songtitle = header.findViewById(R.id.song_title);
        songartist = header.findViewById(R.id.song_artist);

        setPanelSlideListeners(panelLayout);

        navDrawerRunnable.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupDrawerContent(navigationView);
                setupNavigationIcons(navigationView);
            }
        }, 700);


        if (TimberUtils.isMarshmallow()) {
            checkPermissionAndThenLoad();
            //checkWritePermissions();
        } else {
            loadEverything();
        }

        addBackstackListener();

        if (Intent.ACTION_VIEW.equals(action)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.clearQueue();
                    MusicPlayer.openFile(getIntent().getData().getPath());
                    MusicPlayer.playOrPause();
                    navigateNowplaying.run();
                }
            }, 350);
        }

        if (!panelLayout.isPanelHidden() && MusicPlayer.getTrackName() == null) {
            panelLayout.hidePanel();
        }

        if (playServicesAvailable) {

            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;

            FrameLayout contentRoot = findViewById(R.id.content_root);
            contentRoot.addView(LayoutInflater.from(this)
                    .inflate(R.layout.fragment_cast_mini_controller, null), params);

            findViewById(R.id.castMiniController).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, ExpandedControllerActivity.class));
                }
            });
        }

    }

    private void loadEverything() {
        Runnable navigation = navigationMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            navigateLibrary.run();
        }

        new initQuickControls().execute("");
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (Nammu.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            loadEverything();
        } else {
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(panelLayout, "Timber will need to read external storage to display songs on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallback);
                            }
                        }).show();
            } else {
                Nammu.askForPermission(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallback);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (isNavigatingMain()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else super.onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (panelLayout.isPanelExpanded()) {
            panelLayout.collapsePanel();
        } else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);

            if (mInterstitialAd!= null && mInterstitialAd.isLoaded()){
                mInterstitialAd.show();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
//                        updatePosition(menuItem);

                        runnable = null;

                        switch (menuItem.getItemId()) {
                            case R.id.nav_library:

                                runnable = navigateLibrary;
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }

                                break;
                            case R.id.nav_playlists:
                                runnable = navigatePlaylist;
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }

                                break;
                            case R.id.nav_folders:
                                runnable = navigateFolder;
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }

                                break;
                            case R.id.nav_nowplaying:
                                if (getCastSession() != null) {
                                    startActivity(new Intent(MainActivity.this, ExpandedControlsActivity.class));
                                } else {
                                    NavigationUtils.navigateToNowplaying(MainActivity.this, false);
                                }
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }
                                break;
                            case R.id.nav_queue:
                                runnable = navigateQueue;
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }
                                break;
                            case R.id.nav_settings:
                                NavigationUtils.navigateToSettings(MainActivity.this);
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }
                                break;
                            case R.id.music_cutter:

                                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }

                                break;

                            case R.id.privacy_policy:

                                startActivity(new Intent(MainActivity.this, PrivacyPolicy.class));
                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                }

                                break;
                            case R.id.rate_us:

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                                break;

                            case R.id.share_app:
                                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
                                startActivity(Intent.createChooser(sharingIntent, "Share App via"));
                                break;
                        }

                        if (runnable != null) {
                            mDrawerLayout.closeDrawers();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    runnable.run();
                                }
                            }, 350);
                        }
                        return true;

                    }
                });
    }

    private void setupNavigationIcons(NavigationView navigationView) {

        //material-icon-lib currently doesn't work with navigationview of design support library 22.2.0+
        //set icons manually for now
        //https://github.com/code-mc/material-icon-lib/issues/15

        if (!isDarkTheme) {
            navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.library_music);
            navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play);
            navigationView.getMenu().findItem(R.id.nav_queue).setIcon(R.drawable.music_note);
            navigationView.getMenu().findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_open_black_24dp);
            navigationView.getMenu().findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music);
            navigationView.getMenu().findItem(R.id.music_cutter).setIcon(R.drawable.ic_content_cut_black);
            navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.settings);
            navigationView.getMenu().findItem(R.id.rate_us).setIcon(R.drawable.ic_rate_review_black);
            navigationView.getMenu().findItem(R.id.share_app).setIcon(R.drawable.ic_share_black);
            navigationView.getMenu().findItem(R.id.privacy_policy).setIcon(R.drawable.ic_verified_user_black);
        } else {
            navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.library_music_white);
            navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play_white);
            navigationView.getMenu().findItem(R.id.nav_queue).setIcon(R.drawable.music_note_white);
            navigationView.getMenu().findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_open_white_24dp);
            navigationView.getMenu().findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music_white);
            navigationView.getMenu().findItem(R.id.music_cutter).setIcon(R.drawable.ic_content_cut_white);
            navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.settings_white);
            navigationView.getMenu().findItem(R.id.rate_us).setIcon(R.drawable.ic_rate_review_white);
            navigationView.getMenu().findItem(R.id.share_app).setIcon(R.drawable.ic_share_white);
            navigationView.getMenu().findItem(R.id.privacy_policy).setIcon(R.drawable.ic_verified_user_white);
        }


    }

    private void updatePosition(final MenuItem menuItem) {
        runnable = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_library:

                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                    runnable = navigateLibrary;
                }

                break;
            case R.id.nav_playlists:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                    runnable = navigatePlaylist;
                }

                break;
            case R.id.nav_folders:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                    runnable = navigateFolder;
                }

                break;
            case R.id.nav_nowplaying:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                    if (getCastSession() != null) {
                        startActivity(new Intent(MainActivity.this, ExpandedControlsActivity.class));
                    } else {
                        NavigationUtils.navigateToNowplaying(MainActivity.this, false);
                    }
                }
                break;
            case R.id.nav_queue:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                    runnable = navigateQueue;
                }

                break;
            case R.id.nav_settings:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                    NavigationUtils.navigateToSettings(MainActivity.this);
                }
                break;
            case R.id.music_cutter:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {

                    startActivity(new Intent(MainActivity.this, SplashActivity.class));
                }

                break;
            case R.id.rate_us:

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;

            case R.id.share_app:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(sharingIntent, "Share App via"));
                break;
        }

        if (runnable != null) {

            mDrawerLayout.closeDrawers();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }, 350);
        }
    }

    public void setDetailsToHeader() {
        String name = MusicPlayer.getTrackName();
        String artist = MusicPlayer.getArtistName();

        if (name != null && artist != null) {
            songtitle.setText(name);
            songartist.setText(artist);
        }
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build());
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        setDetailsToHeader();

        if (panelLayout.isPanelHidden() && MusicPlayer.getTrackName() != null) {
            panelLayout.showPanel();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isNavigatingMain() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return (currentFragment instanceof MainFragment || currentFragment instanceof QueueFragment
                || currentFragment instanceof PlaylistFragment || currentFragment instanceof FoldersFragment);
    }

    private void addBackstackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportFragmentManager().findFragmentById(R.id.fragment_container).onResume();
            }
        });
    }


    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void showCastMiniController() {
        findViewById(R.id.castMiniController).setVisibility(View.VISIBLE);
        findViewById(R.id.quickcontrols_container).setVisibility(View.GONE);
        panelLayout.hidePanel();
    }

    @Override
    public void hideCastMiniController() {

        findViewById(R.id.castMiniController).setVisibility(View.GONE);
        findViewById(R.id.quickcontrols_container).setVisibility(View.VISIBLE);

        panelLayout.showPanel();
    }
}


