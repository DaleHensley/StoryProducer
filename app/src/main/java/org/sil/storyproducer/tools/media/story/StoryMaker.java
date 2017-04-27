package org.sil.storyproducer.tools.media.story;

import android.media.MediaFormat;
import android.util.Log;

import org.sil.storyproducer.tools.media.pipe.PipedAudioConcatenator;
import org.sil.storyproducer.tools.media.pipe.PipedAudioMixer;
import org.sil.storyproducer.tools.media.pipe.PipedMediaEncoder;
import org.sil.storyproducer.tools.media.pipe.PipedMediaMuxer;
import org.sil.storyproducer.tools.media.pipe.PipedVideoSurfaceEncoder;

import java.io.Closeable;
import java.io.File;

/**
 * StoryMaker handles all the brunt work of constructing a media pipeline for a given set of StoryPages.
 */
public class StoryMaker implements Closeable {
    private static final String TAG = "StoryMaker";

    private float mSoundtrackVolumeModifier = 0.5f;
    private static final long SOUNDTRACK_FADE_OUT_US = 1000000;

    private final File mOutputFile;
    private final int mOutputFormat;

    private final MediaFormat mVideoFormat;
    private final MediaFormat mAudioFormat;
    private final StoryPage[] mPages;

    private final long mAudioTransitionUs;
    private final long mSlideCrossFadeUs;

    private final int mSampleRate;
    private final int mChannelCount;

    private final long mDurationUs;

    private PipedMediaMuxer mMuxer;
    private boolean mIsDone = false;

    /**
     * Create StoryMaker.
     * @param output output video file.
     * @param outputFormat the format of the output media file
     *               (from {@link android.media.MediaMuxer.OutputFormat}).
     * @param videoFormat desired output video format.
     * @param audioFormat desired output audio format.
     * @param pages pages of this story.
     * @param audioTransitionUs transition duration, in microseconds, between narration segments.
     *                          Note: this helps drive length of video.
     * @param slideCrossFadeUs cross-fade duration, in microseconds, between page images.
     */
    public StoryMaker(File output, int outputFormat, MediaFormat videoFormat, MediaFormat audioFormat,
                      StoryPage[] pages, long audioTransitionUs, long slideCrossFadeUs) {
        mOutputFile = output;
        mOutputFormat = outputFormat;
        mVideoFormat = videoFormat;
        mAudioFormat = audioFormat;
        mPages = pages;

        mAudioTransitionUs = audioTransitionUs;
        mSlideCrossFadeUs = slideCrossFadeUs;

        mSampleRate = mAudioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        mChannelCount = mAudioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        mDurationUs = getStoryDuration(mPages, mAudioTransitionUs);
    }

    /**
     * Set the relative volume of the soundtrack to the narration for the video.
     * @param modifier between 0 (silent) and 1 (original volume)
     */
    public void setSoundtrackVolumeModifier(float modifier) {
        mSoundtrackVolumeModifier = modifier;
    }

    public boolean isDone() {
        return mIsDone;
    }

    /**
     * Set StoryMaker in motion. It is advisable to run this method from a separate thread.
     * @return whether the video creation process finished.
     */
    public boolean churn() {
        if(mIsDone) {
            Log.e(TAG, "StoryMaker already finished!");
        }

        PipedAudioConcatenator soundtrackConcatenator = new PipedAudioConcatenator(0, mSampleRate, mChannelCount);
        soundtrackConcatenator.setFadeOut(SOUNDTRACK_FADE_OUT_US);
        PipedAudioConcatenator narrationConcatenator = new PipedAudioConcatenator(mAudioTransitionUs, mSampleRate, mChannelCount);
        PipedAudioMixer audioMixer = new PipedAudioMixer();
        PipedMediaEncoder audioEncoder = new PipedMediaEncoder(mAudioFormat);
        StoryFrameDrawer videoDrawer = null;
        PipedVideoSurfaceEncoder videoEncoder = null;
        if(mVideoFormat != null) {
            videoDrawer = new StoryFrameDrawer(mVideoFormat, mPages, mAudioTransitionUs, mSlideCrossFadeUs);
            videoEncoder = new PipedVideoSurfaceEncoder();
        }
        mMuxer = new PipedMediaMuxer(mOutputFile.getAbsolutePath(), mOutputFormat);

        boolean success = false;

        try {
            mMuxer.addSource(audioEncoder);

            audioEncoder.addSource(audioMixer);
            audioMixer.addSource(soundtrackConcatenator, mSoundtrackVolumeModifier);
            audioMixer.addSource(narrationConcatenator);

            long soundtrackDuration = 0;
            File lastSoundtrack = null;
            for (StoryPage page : mPages) {
                File narration = page.getNarrationAudio();
                long audioDuration = page.getAudioDuration();

                File soundtrack = page.getSoundtrackAudio();
                long pageDuration = page.getDuration(mAudioTransitionUs);

                //If we encounter a new soundtrack, stop the current one and start the new one.
                //Otherwise, continue playing last soundtrack.
                if(soundtrack == null || !soundtrack.equals(lastSoundtrack)) {
                    if(lastSoundtrack != null) {
                        soundtrackConcatenator.addSourcePath(lastSoundtrack.getAbsolutePath(), soundtrackDuration);
                    }
                    else if(soundtrackDuration > 0) {
                        soundtrackConcatenator.addSource(null, soundtrackDuration);
                    }

                    lastSoundtrack = soundtrack;
                    soundtrackDuration = pageDuration;
                }
                else {
                    soundtrackDuration += pageDuration;
                }

                String path = narration != null ? narration.getAbsolutePath() : null;
                narrationConcatenator.addSourcePath(path, audioDuration);
            }

            //Add last soundtrack
            if(lastSoundtrack != null) {
                soundtrackConcatenator.addLoopingSourcePath(lastSoundtrack.getAbsolutePath(), soundtrackDuration);
            }


            if(mVideoFormat != null) {
                mMuxer.addSource(videoEncoder);

                videoEncoder.addSource(videoDrawer);
            }
            success = mMuxer.crunch();
            Log.i(TAG, "Video saved to " + mOutputFile);
        }
        catch (Exception e) {
            Log.e(TAG, "Error in story making", e);
        }
        finally {
            //Everything should be closed automatically, but close everything just in case.
            soundtrackConcatenator.close();
            narrationConcatenator.close();
            audioMixer.close();
            audioEncoder.close();
            if(mVideoFormat != null) {
                videoDrawer.close();
                videoEncoder.close();
            }
            mMuxer.close();
        }

        mIsDone = true;

        return success;
    }

    public long getStoryDuration() {
        return mDurationUs;
    }

    /**
     * Get the expected duration, in microseconds, of the produced video.
     * This value should be accurate to a few milliseconds for arbitrarily long stories.
     * @param pages pages of this story.
     * @param audioTransitionUs transition duration, in microseconds, between narration segments.
     * @return expected duration of the produced video in microseconds.
     */
    public static long getStoryDuration(StoryPage[] pages, long audioTransitionUs) {
        long durationUs = 0;

        for(StoryPage page : pages) {
            durationUs += page.getDuration(audioTransitionUs);
        }

        return durationUs;
    }

    public double getProgress() {
        if(mIsDone) {
            return 1;
        }
        else if(mMuxer != null) {
            long audioProgress = mMuxer.getAudioProgress();
            long videoProgress = mMuxer.getVideoProgress();

            long minProgress = Math.min(audioProgress, videoProgress);

            return minProgress / (double) mDurationUs;
        }
        return 0;
    }

    public double getAudioProgress() {
        if(mMuxer != null) {
            long audioProgress = mMuxer.getAudioProgress();
            return audioProgress / (double) mDurationUs;
        }
        return 0;
    }

    public double getVideoProgress() {
        if(mMuxer != null) {
            long videoProgress = mMuxer.getVideoProgress();
            return videoProgress / (double) mDurationUs;
        }
        return 0;
    }

    @Override
    public void close() {
        if(mMuxer != null) {
            Log.i(TAG, "Closing media pipeline. Subsequent logged errors may not be cause for concern.");
            mMuxer.close();
        }
        mIsDone = true;
    }
}
