package org.sil.storyproducer.tools.media.pipe;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import org.sil.storyproducer.tools.media.MediaHelper;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * <p>This abstract media pipeline component provides a base for audio components
 * which care about touching every output short.</p>
 *
 * <p>The most important methods for a class overriding this class are {@link #loadSamplesForTime(long)}
 * and {@link #getSampleForChannel(int)}. {@link #loadSamplesForTime(long)} will be called exactly
 * once, in order, for each time step according to {@link #mSampleRate}. After each of these calls,
 * {@link #getSampleForChannel(int)} will be called exactly once, in order, for each channel
 * according to {@link #mChannelCount}.</p>
 *
 * <p>As a note on implementation, we are generally trying to use arrays when manipulating the shorts
 * rather than using buffers directly. We hypothesize that doing so gives us significant performance
 * gains on some physical devices.</p>
 */
public abstract class PipedAudioShortManipulator implements PipedMediaByteBufferSource {
    private static final String TAG = "PipedAudioShortMan";

    protected abstract String getComponentName();

    private Thread mThread;

    //Any caller of isDone needs to be immediately aware of changes to the mIsDone variable,
    //even in another thread.
    private volatile boolean mIsDone = false;
    private boolean mNonvolatileIsDone = false;

    //Although this is cross-thread, it isn't important for the input thread to immediately stop;
    //so no volatile keyword.
    protected State mComponentState = State.UNINITIALIZED;

    private static final int BUFFER_COUNT = 4;
    private final ByteBufferQueue mBufferQueue = new ByteBufferQueue(BUFFER_COUNT);

    private static final int MAX_BUFFER_CAPACITY = MediaHelper.MAX_INPUT_BUFFER_SIZE;
    private final short[] mShortBuffer = new short[MAX_BUFFER_CAPACITY / 2]; //short = 2 bytes

    protected int mSampleRate;
    protected int mChannelCount;
    private long mSeekTime = 0;

    private int mAbsoluteSampleIndex = 0;

    @Override
    public MediaHelper.MediaType getMediaType() {
        return MediaHelper.MediaType.AUDIO;
    }

    @Override
    public final boolean isDone() {
        return (mIsDone && mBufferQueue.isEmpty()) || mComponentState == State.CLOSED;
    }

    @Override
    public void fillBuffer(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        ByteBuffer myBuffer = mBufferQueue.getFilledBuffer(info);
        buffer.clear();
        buffer.put(myBuffer);
        mBufferQueue.releaseUsedBuffer(myBuffer);
    }

    @Override
    public ByteBuffer getBuffer(MediaCodec.BufferInfo info) {
        return mBufferQueue.getFilledBuffer(info);
    }

    @Override
    public void releaseBuffer(ByteBuffer buffer) throws InvalidBufferException {
        mBufferQueue.releaseUsedBuffer(buffer);
    }

    protected void start() throws SourceUnacceptableException {
        if(mSampleRate == 0) {
            throw new SourceUnacceptableException(getComponentName() + ": Sample rate not specified!");
        }
        if(mChannelCount == 0) {
            throw new SourceUnacceptableException(getComponentName() + ": Channel count not specified!");
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    spinInput();
                } catch (SourceClosedException e) {
                    Log.w(TAG, "spinInput stopped prematurely", e);
                }
            }
        });
        mComponentState = State.RUNNING;
        mThread.start();
    }

    private void spinInput() throws SourceClosedException {
        if(MediaHelper.VERBOSE) Log.v(TAG, getComponentName() + ".spinInput starting...");

        mNonvolatileIsDone = !loadSamplesForTime(mSeekTime);

        while(mComponentState != State.CLOSED && !mIsDone) {
            long durationNs = 0;
            if (MediaHelper.DEBUG) {
                durationNs = -System.nanoTime();
            }
            ByteBuffer outBuffer = mBufferQueue.getEmptyBuffer(MediaHelper.TIMEOUT_USEC);

            if(outBuffer == null) {
                if(MediaHelper.VERBOSE)
                    Log.d(TAG, getComponentName() + ".spinInput: empty buffer unavailable");
                continue;
            }

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            //reset output buffer
            outBuffer.clear();

            //reset output buffer info
            info.set(0, 0, mSeekTime, 0);

            //prepare a ShortBuffer view of the output buffer
            ShortBuffer outShortBuffer = MediaHelper.getShortBuffer(outBuffer);

            int length = outShortBuffer.remaining();
            int pos = 0;

            outShortBuffer.get(mShortBuffer, pos, length);
            outShortBuffer.clear();

            int iSample;
            for(iSample = 0; iSample < length; iSample += mChannelCount) {
                //interleave channels
                //N.B. Always put all samples (of different channels) of the same time in the same buffer.
                for (int i = 0; i < mChannelCount; i++) {
                    mShortBuffer[pos++] = getSampleForChannel(i);
                }

                //Keep track of the current presentation time in the output audio stream.
                mAbsoluteSampleIndex++;
                mSeekTime = getTimeFromIndex(mSampleRate, mAbsoluteSampleIndex);

                //Give warning about new time
                mNonvolatileIsDone = !loadSamplesForTime(mSeekTime);

                //Break out only in exception case of exhausting source.
                if(mNonvolatileIsDone) {
                    //Since iSample is used outside the loop, count this last iteration.
                    iSample += mChannelCount;
                    break;
                }
            }

            info.size = iSample * 2; //short = 2 bytes

            outShortBuffer.put(mShortBuffer, 0, length);

            //just to be sure
            outBuffer.position(info.offset);
            outBuffer.limit(info.offset + info.size);

            if (mNonvolatileIsDone) {
                //Sync the volatile version of the isDone variable.
                mIsDone = true;

                info.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            }

            mBufferQueue.sendFilledBuffer(outBuffer, info);

            if (MediaHelper.DEBUG) {
                durationNs += System.nanoTime();
                double sec = durationNs / 1E9;
                Log.d(TAG, getComponentName() + ".spinInput: return output buffer after "
                        + MediaHelper.getDecimal(sec) + " seconds: size " + info.size
                        + " for time " + info.presentationTimeUs);
            }
        }
        if(MediaHelper.VERBOSE) Log.v(TAG, getComponentName() + ".spinInput complete!");
    }

    /**
     * <p>Get a sample for given a channel from the source media pipeline component using linear interpolation.</p>
     *
     * <p>Note: No two calls to this function will elicit information for the same state.</p>
     * @param channel
     * @return
     */
    protected abstract short getSampleForChannel(int channel);

    /**
     * <p>Instruct the callee to prepare to provide samples for a given time.
     * Only this abstract base class should call this function.</p>
     *
     * <p>Note: Sequential calls to this function will provide strictly increasing times.</p>
     * @param time
     * @return true if the component has more source input to process and false if {@link #spinInput()} should finish
     */
    protected abstract boolean loadSamplesForTime(long time) throws SourceClosedException;

    /**
     * <p>Get the sample time from the sample index given a sample rate.</p>
     *
     * <p>Note: This method provides more accurate timestamps than simply keeping track
     * of the current timestamp and deltas.</p>
     * @param sampleRate
     * @param index
     * @return timestamp associated with index (in microseconds)
     */
    protected final long getTimeFromIndex(long sampleRate, int index) {
        return index * 1000000L / sampleRate;
    }

    /**
     * Validate the source as raw audio against channel count and sample rate of this component.
     * @param source to be validated
     * @throws SourceUnacceptableException if source is not raw audio or doesn't match specs
     */
    protected void validateSource(PipedMediaByteBufferSource source) throws SourceUnacceptableException {
        validateSource(source, mChannelCount, mSampleRate);
    }

    /**
     * Validate the source as raw audio against specified channel count and sample rate.
     * @param source to be validated
     * @param channelCount required source channel count (or 0 for any channel count)
     * @param sampleRate required source sample rate (or 0 for any sample rate)
     * @throws SourceUnacceptableException if source is not raw audio or doesn't match specs
     */
    protected void validateSource(PipedMediaByteBufferSource source, int channelCount, int sampleRate)
            throws SourceUnacceptableException {
        MediaFormat format = source.getOutputFormat();

        if (source.getMediaType() != MediaHelper.MediaType.AUDIO) {
            throw new SourceUnacceptableException("Source must be audio!");
        }

        if(!format.getString(MediaFormat.KEY_MIME).equals(MediaHelper.MIMETYPE_RAW_AUDIO)) {
            throw new SourceUnacceptableException("Source audio must be a raw audio stream!");
        }

        int sourceChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        if(sourceChannelCount != 1 && sourceChannelCount != 2) {
            throw new SourceUnacceptableException("Source audio is neither mono nor stereo!");
        }
        else if (channelCount != 0 && channelCount != sourceChannelCount) {
            throw new SourceUnacceptableException("Source audio channel counts don't match!");
        }

        if (sampleRate != 0 && sampleRate != format.getInteger(MediaFormat.KEY_SAMPLE_RATE)) {
            throw new SourceUnacceptableException("Source audio sample rates don't match!");
        }
    }

    @Override
    public void close() {
        //Force the spinInput thread to shutdown.
        mComponentState = State.CLOSED;
        if(mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, getComponentName() + ": Failed to stop input thread!", e);
            }
            mThread = null;
        }
    }
}
