package org.sil.storyproducer.model.logging;

import android.content.Context;

import org.sil.storyproducer.R;
import org.sil.storyproducer.model.StoryState;

public class ComChkEntry extends LogEntry {
    private int slideNum;
    private Type type;
    private static String mPhase;

    public static void init(Context context){
        mPhase = context.getString(R.string.community_check_title);
        Type.init(context);
    }

    private ComChkEntry(long timestamp, Type type, int slideNum) {
        super(timestamp);
        this.type=type;
        this.slideNum=slideNum;
    }

    @Override
    public int getColor(){
        return R.color.comunity_check_phase;
    }

    @Override
    public String getPhase(){
        return mPhase;
    }

    @Override
    public boolean appliesToSlideNum(int slideNum) {
        return slideNum == this.slideNum;
    }

    @Override
    public String getDescription(){
        return type.toString();
    }

    public enum Type{
        COMMENT_PLAYBACK, COMMENT_RECORDING,
        DRAFT_PLAYBACK;

        private String displayName;

        private void setDisplayName(String str){
            this.displayName = str;
        }

        public static void init(Context context){
            COMMENT_PLAYBACK.setDisplayName(context.getString(R.string.COMMENT_PLAYBACK));
            COMMENT_RECORDING.setDisplayName(context.getString(R.string.COMMENT_RECORDING));
            DRAFT_PLAYBACK.setDisplayName(context.getString(R.string.DRAFT_PLAYBACK));
        }

        public ComChkEntry makeEntry(){
            return new ComChkEntry(System.currentTimeMillis(), this,
                    StoryState.getCurrentStorySlide());
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
