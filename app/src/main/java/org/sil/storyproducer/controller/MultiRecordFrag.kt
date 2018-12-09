package org.sil.storyproducer.controller

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast

import org.sil.storyproducer.R
import org.sil.storyproducer.controller.adapter.RecordingsList
import org.sil.storyproducer.model.Workspace
import org.sil.storyproducer.tools.file.storyRelPathExists
import org.sil.storyproducer.tools.toolbar.RecordingToolbar
import org.sil.storyproducer.tools.toolbar.RecordingToolbar.RecordingListener
import java.util.*

/**
 * The fragment for the Draft view. This is where a user can draft out the story slide by slide
 */
abstract class MultiRecordFrag : SlidePhaseFrag() {

    protected var recordingToolbar: RecordingToolbar? = null



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootViewToolbar = inflater.inflate(R.layout.toolbar_for_recording, container, false)
        setToolbar()
        return rootView
    }

    /**
     * This function serves to handle page changes and stops the audio streams from
     * continuing.
     *
     * @param isVisibleToUser whether fragment is currently visible to user
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        // Make sure that we are currently visible
        if (this.isVisible) {
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
                recordingToolbar?.onPause()
            }
        }
    }

    /**
     * This function serves to stop the audio streams from continuing after the draft has been
     * put on pause.
     */
    override fun onPause() {
        super.onPause()
        recordingToolbar?.onPause()
    }

    /**
     * Used to hide the play and multiple recordings button.
     */
    fun hideButtonsToolbar() {
        recordingToolbar?.hideButtons()
    }


    /**
     * Stops the toolbar from recording or playing back media.
     * Used in [DraftListRecordingsModal]
     */
    open fun stopPlayBackAndRecording() {
        recordingToolbar?.stopToolbarMedia()
        referenceAudioPlayer.stopAudio()
        referncePlayButton!!.setBackgroundResource(R.drawable.ic_play_arrow_white_36dp)
    }

    /**
     * Initializes the toolbar and toolbar buttons.
     */
    protected open fun setToolbar() {
        val recordingListener = object : RecordingListener {
            override fun onStoppedRecording() {
                //updatePlayBackPath()
            }

            override fun onStartedRecordingOrPlayback(isRecording: Boolean) {
                //not used here
            }
        }
        val rList = RecordingsList(context!!, this)

        recordingToolbar = RecordingToolbar(this.activity!!, rootViewToolbar!!, rootView as RelativeLayout,
                true, false, true, false,  rList , recordingListener, slideNum);
        recordingToolbar!!.keepToolbarVisible()
        recordingToolbar!!.stopToolbarMedia()
    }
}
