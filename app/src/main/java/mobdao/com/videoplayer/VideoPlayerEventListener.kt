package mobdao.com.videoplayer

import com.google.android.exoplayer2.Player

class VideoPlayerEventListener : Player.EventListener {

    var screenOnListener: ScreenOnListener? = null

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        screenOnListener?.onSetKeepScreenOn(!(playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED || !playWhenReady))
    }
}