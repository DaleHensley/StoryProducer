package org.sil.storyproducer.tools.file;

import java.io.File;
import java.util.ArrayList;

/**
 * AudioFiles represents an abstraction of the audio resources for story templates and project files.
 */
public class AudioFiles {
    private static final String SOUNDTRACK_PREFIX = "SoundTrack";
    private static final String SOUNDTRACK_EXTENSION = ".mp3";

    private static final String LWC_EXTENSION = ".wav";

    private static final String PREFER_EXTENSION = ".m4a";

    private static final String LEARN_PRACTICE_PREFIX = "learnPractice";
    private static final String LWC_AUDIO_PREFIX = "narration";
    private static final String DRAFT_TEMP = "draftTemp" + PREFER_EXTENSION;
    private static final String DRAFT_AUDIO_PREFIX = "translation";
    private static final String COMMENT_PREFIX = "comment";
    private static final String DRAMATIZATION_AUDIO_PREFIX = "dramatization";

    public enum RenameCode {
        SUCCESS,
        ERROR_LENGTH,
        ERROR_SPECIAL_CHARS,
        ERROR_CONTAINED_DESIGNATOR,
        ERROR_UNDEFINED,
        ;
    }

    public static File getSoundtrack(String story){
        return getSoundtrack(story, 0);
    }
    //TODO: Some stories have multiple soundtrack files. Is that desired and used?
    public static File getSoundtrack(String story, int i){
        return new File(FileSystem.getTemplatePath(story), SOUNDTRACK_PREFIX + i + SOUNDTRACK_EXTENSION);
    }

    //*** LWC ***

    public static File getLWC(String story, int i){
        return new File(FileSystem.getTemplatePath(story), LWC_AUDIO_PREFIX + i + LWC_EXTENSION);
    }

    //*** Learn ***

    /**
     * Gets the File for the learn practice recording
     * @param story
     * @return
     */
    public static File getLearnPractice(String story){
        return new File(FileSystem.getProjectDirectory(story), LEARN_PRACTICE_PREFIX + PREFER_EXTENSION);
    }

    //*** Draft ***

    public static File getDraft(String story, int i){
        return new File(FileSystem.getProjectDirectory(story), DRAFT_AUDIO_PREFIX + i + PREFER_EXTENSION);
    }

    public static File getDraftTemp(String story) {
        return new File(FileSystem.getHiddenTempDirectory(story), DRAFT_TEMP);
    }

    //*** Community Check ***

    public static File getComment(String story, int slide, String commentTitle) {
        return new File(FileSystem.getProjectDirectory(story), COMMENT_PREFIX+slide+"_"+ commentTitle + PREFER_EXTENSION);
    }

    /**
     * deletes the designated audio comment
     * @param story the story the comment comes from
     * @param slide the slide the comment comes from
     * @param commentTitle the name of the comment in question
     */
    public static void deleteComment(String story, int slide, String commentTitle) {
        File file = getComment(story, slide, commentTitle);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * renames the designated audio comment if the new name is valid and the file exists
     * @param story the story the comment comes from
     * @param slide the slide of the story the comment comes from
     * @param oldTitle the old title of the comment
     * @param newTitle the proposed new title for the comment
     * @return returns success or error code of renaming
     */
    public static RenameCode renameComment(String story, int slide, String oldTitle, String newTitle) {
        // Requirements for file names:
        //        - must be under 20 characters
        //        - must be only contain alphanumeric characters or spaces/underscores
        //        - must not contain the comment designator such as "comment0"
        if (newTitle.length() > 20) {
            return RenameCode.ERROR_LENGTH;
        }
        if (!newTitle.matches("[A-Za-z0-9\\s_]+")) {
            return RenameCode.ERROR_SPECIAL_CHARS;
        }
        if (newTitle.matches("comment[0-9]+")) {
            return RenameCode.ERROR_CONTAINED_DESIGNATOR;
        }
        File file = getComment(story, slide, oldTitle);
        boolean renamed = false;
        if (file.exists()) {
            String newPathName = file.getAbsolutePath().replace(oldTitle + PREFER_EXTENSION, newTitle + PREFER_EXTENSION);
            File newFile = new File(newPathName);
            if (!newFile.exists()) {
                renamed = file.renameTo(newFile);
            }
        }
        if (renamed) {
            return RenameCode.SUCCESS;
        } else {
            return RenameCode.ERROR_UNDEFINED;
        }
    }

    /**
     * Returns a list of comment titles for the story and slide in question
     * @param story the story where the comments come from
     * @param slide the slide where the comments come from
     * @return the array of comment titles
     */
    public static String[] getCommentTitles(String story, int slide) {
        ArrayList<String> commentTitles = new ArrayList<>();
        File storyDirectory = FileSystem.getProjectDirectory(story);
        File[] storyDirectoryFiles = storyDirectory.listFiles();
        String filename;
        for (int i = 0; i < storyDirectoryFiles.length; i++) {
            filename = storyDirectoryFiles[i].getName();
            if (filename.startsWith(COMMENT_PREFIX+slide+"_")) {
                filename = filename.replace(COMMENT_PREFIX+slide+"_", "");
                filename = filename.replace(PREFER_EXTENSION, "");
                commentTitles.add(filename);
            }
        }
        String[] returnTitlesArray = new String[commentTitles.size()];
        return commentTitles.toArray(returnTitlesArray);
    }

    //*** Consultant Check ***

    //*** Dramatization ***

    public static File getDramatization(String story, int i){
        return new File(FileSystem.getProjectDirectory(story), DRAMATIZATION_AUDIO_PREFIX + i + PREFER_EXTENSION);
    }

}
