package mobdao.com.videoplayer

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.custom_playback_control.*

/*
TODO:
- improve orientation changes:
    https://yetitcompiles.com/picture-in-picture-video-with-motionlayout/
 */

class VideoPlayerActivity: AppCompatActivity(), ScreenOnListener {

    private var player: SimpleExoPlayer? = null

    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playWhenReady = true
    private var showInFullScreen = false
    private val listener = VideoPlayerEventListener()

    //region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        setupView()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) releasePlayer()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && showInFullScreen) enterFullScreen()
        else exitFullScreen()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            enterFullScreen()
            exitFullScreenImageButton?.visibility = GONE
        } else if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (showInFullScreen) enterFullScreen()
            else exitFullScreen()
        }
    }


    //endregion

    //region private

    private fun setupView() {
        enterFullScreenImageButton?.setOnClickListener {
            showInFullScreen = true
            enterFullScreen()
        }

        exitFullScreenImageButton?.setOnClickListener {
            showInFullScreen = false
            exitFullScreen()
        }
    }

    private fun enterFullScreen() {
        topBarLayout?.visibility = GONE
        enterFullScreenImageButton?.visibility = GONE
        exitFullScreenImageButton?.visibility = VISIBLE
        playerView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun exitFullScreen() {
        topBarLayout?.visibility = VISIBLE
        enterFullScreenImageButton?.visibility = VISIBLE
        exitFullScreenImageButton?.visibility = GONE
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                    this,
                    DefaultRenderersFactory(this),
                    DefaultTrackSelector(), DefaultLoadControl()
            )
            player?.run {
                listener.screenOnListener = this@VideoPlayerActivity
                addListener(listener)
                playerView?.player = this
                playWhenReady = this@VideoPlayerActivity.playWhenReady
                seekTo(currentWindow, playbackPosition)
            }
        }
        val mediaSource =
                buildMediaSource(Uri.parse("https://sendbird-us-1.s3.amazonaws.com/DC67A365-2FFD-4E1F-A47C-ECA8B94BA9C7/upload/n/6b0c9be06b724358bc3d4d59b60c6c2b.mp4"))
        player?.prepare(mediaSource, false, true)
    }

    private fun releasePlayer() {
        player?.run {
            listener.screenOnListener = null
            removeListener(listener)
            playbackPosition = currentPosition
            currentWindow = currentWindowIndex
            this@VideoPlayerActivity.playWhenReady = playWhenReady
            release()
        }
        player = null
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val manifestDataSourceFactory = DefaultHttpDataSourceFactory("ua")
        return ExtractorMediaSource.Factory(manifestDataSourceFactory).createMediaSource(uri)
    }

    //endregion

    //region ScreenOnListener

    override fun onSetKeepScreenOn(keepOn: Boolean) {
        playerView?.keepScreenOn = keepOn
    }

    //endregion
}