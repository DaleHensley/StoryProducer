package org.sil.storyproducer.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class FileSystem {
    private static String language = "ENG"; //ethnologue code for english

    private static Context context;
    private static final String TEMPLATES_DIR = "templates",
                                NARRATION_PREFIX = "narration",
                                PROJECT_DIR = "projects",
                                SOUNDTRACK_PREFIX = "SoundTrack",
                                TRANSLATION_PREFIX = "translation";

    //Paths to template directories from language and story name
    private static Map<String, Map<String, String>> storyPaths;
    private static Map<String, String> projectPaths;

    private static final FilenameFilter directoryFilter = new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
        }
    };

    public static void init(Context con) {
        context = con;
        loadStories();
    }

    //Populate storyPaths from files in system
    public static void loadStories() {
        //Reset storyPaths
        storyPaths = new HashMap<>();
        projectPaths=new HashMap<>();

        File[] storeDirs = getStorageDirs();
        for (int storeIndex = 0; storeIndex < storeDirs.length; storeIndex++) {
            File sDir = storeDirs[storeIndex];

            if (sDir != null) {
                File templateDir = new File(sDir, TEMPLATES_DIR);

                //If there is no template directory, move on from this storage device.
                if(!templateDir.exists() || !templateDir.isDirectory()) {
                    continue;
                }

                File[] langDirs = getLanguageDirs(templateDir);
                for (int langIndex = 0; langIndex < langDirs.length; langIndex++) {
                    File lDir = langDirs[langIndex];
                    String lang = lDir.getName();

                    if (!storyPaths.containsKey(lang)) {
                        storyPaths.put(lang, new HashMap<String, String>());
                    }
                    Map<String, String> storyMap = storyPaths.get(lang);

                    File[] storyDirs = getStoryDirs(lDir);
                    for (int storyIndex = 0; storyIndex < storyDirs.length; storyIndex++) {
                        File storyDir = storyDirs[storyIndex];
                        String storyName = storyDir.getName();
                        String storyPath = storyDir.getPath();
                        storyMap.put(storyName, storyPath);

                        //Make sure the corresponding projects directory exists.
                        File storyWriteDir = new File(new File(sDir, PROJECT_DIR), storyName);
                        if(!storyWriteDir.isDirectory()) {
                            storyWriteDir.mkdir();
                        }
                    }
                }

                File projectDir  = new File(sDir, PROJECT_DIR);

                //Make the project directory if it does not exist.
                //The template creator shouldn't have to remember this step.
                if(!projectDir.isDirectory()) {
                    projectDir.mkdir();
                }

                File[] storyDirs = getStoryDirs(projectDir);
                for (int storyIndex = 0; storyIndex < storyDirs.length; storyIndex++) {
                    File storyDir = storyDirs[storyIndex];
                    String storyName = storyDir.getName();
                    String storyPath = storyDir.getPath();
                    projectPaths.put(storyName, storyPath);
                }
            }
        }
    }

    public static void changeLanguage(String lang) {
        language = lang;
    }

    private static File[] getStorageDirs() {
        return ContextCompat.getExternalFilesDirs(context, null);
    }

    private static File[] getLanguageDirs(File storageDir) {
        return storageDir.listFiles(directoryFilter);
    }
    private static File[] getStoryDirs(File dir) {
        return dir.listFiles(directoryFilter);
    }

    private static String getStoryPath(String story) {
        Map<String, String> storyMap = storyPaths.get(language);
        if (storyMap != null) {
            return storyMap.get(story);
        }
        return null;
    }

    public static File getNarrationAudio(String story, int i){
        return new File(getStoryPath(story)+"/"+NARRATION_PREFIX+i+".wav");
    }
    public static File getTranslationAudio(String story, int i){
        return new File(getStoryPath(story)+"/"+TRANSLATION_PREFIX+i+".mp3");
    }

    public static File getSoundtrack(String story){
        return new File(getStoryPath(story)+"/"+SOUNDTRACK_PREFIX+0+".mp3");
    }

    /**
     * Gets the directory of a particular story in the <b>projects</b> directory.
     * @param story
     * @return
     */
    public static File getProjectDirectory(String story) {
        String path = projectPaths.get(story);
        return new File(path); //will throw a null pointer exception if path is null
    }

    public static String[] getStoryNames() {
        Map<String, String> storyMap = storyPaths.get(language);
        if (storyMap != null) {
            Set<String> keys = storyMap.keySet();
            return keys.toArray(new String[keys.size()]);
        }
        return new String[0];
    }

    public static File getImageFile(String story, int number) {
        return new File(getStoryPath(story), number + ".jpg");
    }

    public static Bitmap getImage(String story, int number) {
        String path = getStoryPath(story);
        File f = new File(path);
        File file[] = f.listFiles();

        for (int i = 0; i < file.length; i++) {
            if (file[i].getName().equals(number + ".jpg")) {
                return BitmapFactory.decodeFile(path + "/" + file[i].getName());
            }
        }
        return null;
    }

//    public static String getAudioPath(String story, int number) {
//        String path = getStoryPath(story);
//        File f = new File(path);
//        File file[] = f.listFiles();
//        String audioName = "narration" + number;
//
//        for (int i = 0; i < file.length; i++) {
//            String[] audioExtensions = {".wav", ".mp3", ".wma"};
//            for (String extension : audioExtensions) {
//                if (file[i].getName().equals(audioName + extension)) {
//                    return file[i].getAbsolutePath();
//                }
//            }
//        }
//        return null;
//    }
    public static int getImageAmount(String storyName) {
        String path = getStoryPath(storyName);
        File f = new File(path);
        File file[] = f.listFiles();
        int count = 0;
        for (int i = 0; i < file.length; i++) {
            if (!file[i].isHidden() && file[i].getName().contains(".jpg")) {
                count++;
            }
        }
        return count;
    }

    private static String[] content;

    public static void loadSlideContent(String storyName, int slideNum) {
        File file = new File(getStoryPath(storyName), (slideNum + ".txt"));
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }


        String text1 = text.toString();
        byte[] temp = text1.getBytes();
        for (int i = 0; i < temp.length - 2; i++) {
            //Swap out curly apostrophe with ASCII single quote
            if (temp[i] == -17 && temp[i + 1] == -65 && temp[i + 2] == -67) {
                text = text.replace(i, i + 1, "'");
                text1 = text.toString();
                temp = text1.getBytes();
            }
        }
        content = text.toString().split(Pattern.quote("~"));
    }

    public static String getTitle() {
        return content[0];
    }

    public static String getSubTitle() {
        return content[1];
    }

    public static String getSlideVerse() {
        return content[2];
    }

    public static String getSlideContent() {
        return content[3];
    }

    public static String[] getLanguages() {
        return storyPaths.keySet().toArray(new String[storyPaths.size()]);
    }

    /**
     * This function searches the directory of the story and finds the total number of
     * slides associated with the story. The total number of slides will be determined by
     * the number of .jpg and .txt extensions. The smaller number of .jpg or .txt will be returned.
     *
     * @param storyName The story name that needs to find total number of slides.
     * @return The number of slides total for the story. The smaller number of .txt or .jpg files
     * found in the directory.
     */
    public static int getTotalSlideNum(String storyName) {
        String rootDirectory = getStoryPath(storyName);
        File[] files = new File(rootDirectory).listFiles();
        int totalPics = 0;
        int totalTexts = 0;

        for (File aFile : files) {
            String tempNumber;
            String fileName = aFile.toString();
            if (fileName.contains(".jpg") || fileName.contains(".txt")) {
                String extension = (fileName.contains(".jpg")) ? ".jpg" : ".txt";
                tempNumber = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf(extension));
                if (tempNumber.matches("^([0-9]+)$")) {
                    int checkingNumber = Integer.valueOf(tempNumber);
                    if (extension.equals(".txt")) {
                        totalTexts = (checkingNumber > totalTexts) ? checkingNumber : totalTexts;
                    } else {
                        totalPics = (checkingNumber > totalPics) ? checkingNumber : totalPics;
                    }
                }
            }
        }

        return (totalPics < totalTexts) ? totalPics : totalTexts;
    }
}
