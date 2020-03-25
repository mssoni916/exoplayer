package com.example.exoplayerdemo

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {

    companion object {
        val ARG_VIDEO_POSITION: String = "MainActivity.PLAYBACK_POSITION"
    }

    public lateinit var playerView: PlayerView
    public lateinit var player: SimpleExoPlayer
    private var playWhenReady: Boolean = true
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private var isInPIPMode: Boolean = false
    private var isPIPEnabled: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.player_view)
        Toast.makeText(this, "Started", Toast.LENGTH_LONG)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onPause() {
        playbackPosition = player.currentPosition
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (playbackPosition > 0L && !isInPIPMode) {
            player.seekTo(playbackPosition)
        }
        playerView.useController = true
    }

    override fun onStop() {
        super.onStop()
        playerView.player = null
        player.release()
//        releasePlayer()
    }

    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView.player = player
        var uri: Uri = Uri.parse(getString(R.string.mp4_media_url))
        var mediaSource = buildMediaSource(uri)
//        var returnResultOnce = true
//        player.addListener(object : Player.EventListener{
//            override fun onPlayerError(error: ExoPlaybackException?) {
//                setResult(Activity.RESULT_CANCELED)
//                finishAndRemoveTask()
//            }
//
//            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//                if (playbackState == Player.STATE_READY && returnResultOnce) {
//                    setResult(Activity.RESULT_OK)
//                    returnResultOnce = false
//                }
//            }
//        })

        player.playWhenReady = playWhenReady
        player.seekTo(currentWindow, playbackPosition)
        player.prepare(mediaSource)

    }

    private fun releasePlayer() {
        if(player != null) {
            playWhenReady = player.playWhenReady
            playbackPosition = player.currentPosition
            currentWindow = player.currentWindowIndex
            player.release()
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        var dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(
            this, applicationInfo.loadLabel(packageManager).toString()))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState.apply {
//            this?.putLong(ARG_VIDEO_POSITION, player.currentPosition)
//        })
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
//        super.onRestoreInstanceState(savedInstanceState)
//        playbackPosition = savedInstanceState!!.getLong(ARG_VIDEO_POSITION)
//    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            && isPIPEnabled) {
            enterPIPMode()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPIPMode()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (newConfig != null) {
            playbackPosition = player.currentPosition
            isInPIPMode = !isInPictureInPictureMode
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    @Suppress("DEPRECATION")
    fun enterPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            playerView.useController = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                this.enterPictureInPictureMode(params.build())
            }
            else {
                this.enterPictureInPictureMode()
            }

//            Handler().postDelayed({
//                isPIPEnabled = isInPictureInPictureMode
//                if (!isInPictureInPictureMode)
//                    onBackPressed()
//            }, 30)
        }
    }
}
