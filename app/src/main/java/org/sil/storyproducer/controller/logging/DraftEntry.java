package org.sil.storyproducer.controller.logging;

import android.content.Context;

import org.sil.storyproducer.R;
import org.sil.storyproducer.model.StoryState;

/**
 * Created by Michael D. Baxter on 1/16/2017.
 *
 */

public class DraftEntry extends LogEntry {

    private int slideNum;
    private Type type;

    private DraftEntry(long dateTime, Type type, int slideNum) {
        super(dateTime, Phase.Draft, R.color.draft_phase);
        this.slideNum=slideNum;
        this.type=type;
    }

    @Override
    public int getSlideNum() {
        return slideNum;
    }

    @Override
    public String getDescription(){
        return type.toString();
    }

    public enum Type {
        LWC_PLAYBACK, DRAFT_RECORDING,
        DRAFT_PLAYBACK;

        private String displayName;

        public DraftEntry makeEntry(){
            return new DraftEntry(System.currentTimeMillis(), this,
                    StoryState.getCurrentStorySlide());
        }

        private void setDisplayName(String str){
            this.displayName = str;
        }

        public static void init(Context context){
            LWC_PLAYBACK.setDisplayName(context.getString(R.string.LWC_PLAYBACK));
            DRAFT_RECORDING.setDisplayName(context.getString(R.string.DRAFT_RECORDING));
            DRAFT_PLAYBACK.setDisplayName(context.getString(R.string.DRAFT_PLAYBACK));
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
