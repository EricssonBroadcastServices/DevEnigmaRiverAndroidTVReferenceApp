package com.redbeemedia.enigma.referenceapp;

import static android.graphics.PorterDuff.Mode.CLEAR;
import static android.graphics.PorterDuff.Mode.DARKEN;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.google.android.exoplayer2.ui.CaptionStyleCompat.DEFAULT;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ui.CaptionStyleCompat;
import com.redbeemedia.enigma.core.error.AssetFormatError;
import com.redbeemedia.enigma.core.error.AssetGeoBlockedError;
import com.redbeemedia.enigma.core.error.AssetNotAvailableError;
import com.redbeemedia.enigma.core.error.AssetNotAvailableForDeviceError;
import com.redbeemedia.enigma.core.error.AssetRestrictedError;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NotEntitledToAssetError;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.EnigmaPlayer;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;
import com.redbeemedia.enigma.core.player.IEnigmaPlayer;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.BasePlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.playrequest.PlayRequest;
import com.redbeemedia.enigma.core.playrequest.PlaybackProperties;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.virtualui.BaseVirtualButtonListener;
import com.redbeemedia.enigma.core.virtualui.IVirtualButton;
import com.redbeemedia.enigma.core.virtualui.IVirtualButtonListener;
import com.redbeemedia.enigma.exoplayerintegration.ExoPlayerTech;
import com.redbeemedia.enigma.exoplayerintegration.drift.DriftCorrector;
import com.redbeemedia.enigma.referenceapp.activityutil.ActivityConnector;
import com.redbeemedia.enigma.referenceapp.activityutil.IActivityAction;
import com.redbeemedia.enigma.referenceapp.activityutil.IActivityConnector;
import com.redbeemedia.enigma.referenceapp.assets.AssetMarshaller;
import com.redbeemedia.enigma.referenceapp.assets.IAsset;
import com.redbeemedia.enigma.referenceapp.session.SessionContainer;
import com.redbeemedia.enigma.referenceapp.ui.AbstractTracksSpinner;
import com.redbeemedia.enigma.referenceapp.ui.AssetCoverView;
import com.redbeemedia.enigma.referenceapp.ui.TimelineView;
import com.redbeemedia.enigma.referenceapp.ui.VirtualButtonViewAttacher;

public class PlayerActivity extends AppCompatActivity {
    private static final String EXTRA_ASSET = "asset";
    private IEnigmaPlayer enigmaPlayer;
    private ExoPlayerTech exoPlayerTech;
    private IAsset asset;
    private Handler handler;
    private boolean startedAtLeastOnce = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_player);
        exoPlayerTech = new ExoPlayerTech(this, getString(R.string.app_name));
        exoPlayerTech.addDriftListener(new DriftCorrector()); // For automatic drift correction
        exoPlayerTech.attachView(findViewById(R.id.player_view));
        // exoPlayerTech.hideController();

        final ISession session = SessionContainer.getSession().getValue();
        if (session == null) {
            Toast.makeText(this, getString(R.string.session_not_found), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        SessionContainer.getSession().observe(this, newSessionValue -> {
            if (session != newSessionValue) {
                finish();
            }
        });

        IAsset asset = AssetMarshaller.get(getIntent().getExtras(), EXTRA_ASSET);
        if (asset == null) {
            Toast.makeText(this, getString(R.string.app_error_no_asset_selected), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        this.asset = asset;
        this.enigmaPlayer = new EnigmaPlayer(session, exoPlayerTech).setActivity(this);
        AssetCoverView assetCoverView = findViewById(R.id.assetCover);
        assetCoverView.connectTo(enigmaPlayer);

        TimelineView timelineView = findViewById(R.id.timelineView);
        timelineView.connectTo(enigmaPlayer);
        ((AbstractTracksSpinner) findViewById(R.id.subtitles_spinner)).connectTo(enigmaPlayer);
        bindButtons();
    }

    private void connectPlayPause() {
        ImageView playPauseView = findViewById(R.id.play_pause_button);
        handleFocusOnButtons((ImageView) playPauseView);
        this.enigmaPlayer.addListener(new BaseEnigmaPlayerListener(){
            @Override
            public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
                if (to == EnigmaPlayerState.PLAYING){
                    playPauseView.setImageResource(R.drawable.lb_ic_pause);
                }else if(from == EnigmaPlayerState.PLAYING){
                    playPauseView.setImageResource(R.drawable.lb_ic_play);
                }
            }
        }, handler);
        playPauseView.setOnClickListener(v -> {
            if(enigmaPlayer.getVirtualControls().getPause().isEnabled()) {
                enigmaPlayer.getVirtualControls().getPause().click();
                //playPauseView.setImageResource(R.drawable.lb_ic_play);
            } else {
                enigmaPlayer.getVirtualControls().getPlay().click();
                //playPauseView.setImageResource(R.drawable.lb_ic_pause);
            }
        });
    }

    private void bindButtons() {
        View playButtonView = findViewById(R.id.play_button);
        connect(playButtonView, enigmaPlayer.getVirtualControls().getPlay());
        // Pause
        View pauseButtonView = findViewById(R.id.pause_button);
        connect(pauseButtonView, enigmaPlayer.getVirtualControls().getPause());

        View viewByIdFwd = findViewById(R.id.forward_button);
        connect(viewByIdFwd, enigmaPlayer.getVirtualControls().getFastForward());

        View viewByIdRwd = findViewById(R.id.rewind_button);
        connect(viewByIdRwd, enigmaPlayer.getVirtualControls().getRewind());

        View playPauseContainerView = findViewById(R.id.play_pause_container);

        connectPlayPause();

        playPauseContainerView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // handleButtonsNavigation();
            }
        });
    }

    private void connect(View view, IVirtualButton virtualButton) {
        handleFocusOnButtons((ImageView) view);
        view.setOnClickListener(v -> virtualButton.click());
        final IVirtualButtonListener buttonListener = new BaseVirtualButtonListener() {
            @Override
            public void onStateChanged() {
                view.setVisibility(virtualButton.isEnabled() ? VISIBLE : INVISIBLE);
                // handleButtonsNavigation();
            }
        };
        view.addOnAttachStateChangeListener(new VirtualButtonViewAttacher(virtualButton, buttonListener, handler));
    }

    private void handleButtonsNavigation() {
        View playButtonView = findViewById(R.id.play_button);
        View pauseButtonView = findViewById(R.id.pause_button);
        if (playButtonView.getVisibility() == INVISIBLE) {
            pauseButtonView.requestFocus();
        } else if (pauseButtonView.getVisibility() == INVISIBLE) {
            playButtonView.requestFocus();
        }
    }

    private void handleFocusOnButtons(ImageView playButtonImageView) {
        playButtonImageView.setOnFocusChangeListener((v, event) -> {
            if (event) {
                // when in focus
                playButtonImageView.setColorFilter(Color.argb(80, 50, 50, 0));
                playButtonImageView.setBackgroundTintMode(DARKEN);
            } else {
                playButtonImageView.setBackgroundTintMode(CLEAR);
                playButtonImageView.setColorFilter(Color.argb(0, 0, 0, 0));
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    private void startStream() {
        PlaybackProperties playbackProperties = new PlaybackProperties();
        playbackProperties.setPlayFrom(startedAtLeastOnce ? IPlaybackProperties.PlayFrom.BOOKMARK : IPlaybackProperties.PlayFrom.PLAYER_DEFAULT);
        enigmaPlayer.play(new PlayRequest(asset.getPlayable(), playbackProperties, new PlayRequestResultHandler(new ActivityConnector<>(this))));
    }

    @Override
    protected void onPause() {
        super.onPause();
        enigmaPlayer.getControls().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CaptionStyleCompat captionStyleCompat = new CaptionStyleCompat(DEFAULT.foregroundColor,
                Color.BLUE,
                DEFAULT.windowColor,
                DEFAULT.edgeType,
                DEFAULT.edgeColor,
                DEFAULT.typeface);
        enigmaPlayer.getPlayerSubtitleView().setStyle(captionStyleCompat);
        enigmaPlayer.getPlayerSubtitleView().setFixedTextSize(Dimension.PX, 30);
        enigmaPlayer.getControls().start(new ContinueStreamResultHandler(new ActivityConnector<>(this)));
    }

    private void onPlaybackStarted(IPlaybackSession playbackSession) {
        startedAtLeastOnce = true;
    }

    private void onPlayRequestError(EnigmaError error) {
        String errorMessage = getErrorForUser(error);
        error.printStackTrace();
        PlayerActivity object = this;
        this.runOnUiThread(() -> Toast.makeText(object, errorMessage, Toast.LENGTH_LONG).show());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getErrorForUser(EnigmaError error) {
        if (error instanceof InvalidAssetError) {
            return getString(R.string.asset_error_invalid);
        } else if (error instanceof AssetFormatError) {
            return getString(R.string.asset_error_format);
        } else if (error instanceof AssetNotAvailableForDeviceError) {
            return getString(R.string.asset_error_not_available_for_device);
        } else if (error instanceof NotEntitledToAssetError) {
            return getString(R.string.asset_error_not_entitled);
        } else if (error instanceof AssetGeoBlockedError) {
            return getString(R.string.asset_error_geo_block);
        } else if (error instanceof AssetRestrictedError) {
            return getString(R.string.asset_error_restricted);
        } else if (error instanceof AssetNotAvailableError) {
            return getString(R.string.asset_error_not_available);
        } else {
            return getString(R.string.error_unknown);
        }
    }

    public static Intent getStartIntent(Context context, IAsset asset) {
        Intent intent = new Intent(context, PlayerActivity.class);
        AssetMarshaller.put(intent, EXTRA_ASSET, asset);
        return intent;
    }

    private static class PlayRequestResultHandler extends BasePlayResultHandler {
        private IActivityConnector<PlayerActivity> activity;

        public PlayRequestResultHandler(IActivityConnector<PlayerActivity> activity) {
            this.activity = activity;
        }

        @Override
        public void onStarted(IPlaybackSession playbackSession) {
            activity.perform(activity -> activity.onPlaybackStarted(playbackSession));
        }

        @Override
        public void onError(EnigmaError error) {
            activity.perform(activity -> activity.onPlayRequestError(error));
        }
    }

    private static class ContinueStreamResultHandler implements IControlResultHandler {
        private IActivityConnector<PlayerActivity> activityConnector;
        private final IActivityAction<PlayerActivity> makeNewPlayRequest = new IActivityAction<PlayerActivity>() {
            @Override
            public void execute(PlayerActivity activity) {
                activity.startStream();
            }
        };

        public ContinueStreamResultHandler(IActivityConnector<PlayerActivity> activityConnector) {
            this.activityConnector = activityConnector;
        }

        @Override
        public void onRejected(IRejectReason reason) {
            activityConnector.perform(makeNewPlayRequest);
        }

        @Override
        public void onCancelled() {
        }

        @Override
        public void onError(EnigmaError error) {
            error.printStackTrace();
            activityConnector.perform(makeNewPlayRequest);
        }

        @Override
        public void onDone() {
        }
    }
}
