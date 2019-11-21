package org.sil.storyproducer.model

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import androidx.core.content.FileProvider.getUriForFile
import androidx.documentfile.provider.DocumentFile
import com.google.firebase.analytics.FirebaseAnalytics
import org.sil.storyproducer.R
import org.sil.storyproducer.tools.file.deleteWorkspaceFile
import org.sil.storyproducer.tools.file.getChildOutputStream
import org.sil.storyproducer.tools.file.workspaceRelPathExists
import java.io.File
import java.io.IOException
import java.util.*


internal const val SLIDE_NUM = "CurrentSlideNum"
internal const val DEMO_FOLDER = "000 Unlocked demo story Storm"

object Workspace{
    var workdocfile = DocumentFile.fromFile(File(""))
        set(value) {
            field = value
            prefs?.edit()?.putString("workspace", field.uri.toString())?.apply()
            storiesUpdated = false
        }
    val Stories: MutableList<Story> = mutableListOf()
    var storiesUpdated = false
    var registration: Registration = Registration()
    var phases: List<Phase> = ArrayList()
    var activePhaseIndex: Int = -1
        private set
    var isInitialized = false
    var prefs: SharedPreferences? = null

    var activeStory: Story = emptyStory()
    set(value){
        field = value
        //You are switching the active story.  Recall the last phase and slide.
        activePhase = Phase(value.lastPhaseType)
        activeSlideNum = value.lastSlideNum
    }
    var activePhase: Phase = Phase(PhaseType.LEARN)
        set(value){
            field = value
            activePhaseIndex = -1
            for((i,p) in phases.withIndex()){
                if(p.phaseType == value.phaseType) activePhaseIndex = i
            }
        }
    val activeDirRoot: String
    get(){return activeStory.title }

    val activeDir: String = PROJECT_DIR
    val activeFilenameRoot: String
    get() {
        return "${activePhase.getShortName()}${ Workspace.activeSlideNum }"
    }

    var activeSlideNum: Int = -1
    set(value){
        field = 0
        if(value >= 0 && value < activeStory.slides.size){
            if(activePhase.checkValidDisplaySlideNum(value))
                field = value
        }
    }
    val activeSlide: Slide?
    get(){
        if(activeStory.title == "") return null
        return activeStory.slides[activeSlideNum]
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    val WORKSPACE_KEY = "org.sil.storyproducer.model.workspace"

    fun initializeWorskpace(context: Context) {
        //first, see if there is already a workspace in shared preferences
        prefs = context.getSharedPreferences(WORKSPACE_KEY, Context.MODE_PRIVATE)
        setDemoWorkspace(context)
        isInitialized = true
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logEvent(context: Context, eventName: String, params: Bundle = Bundle()){
        params.putString("phone_id", Secure.getString(context.contentResolver,
                Secure.ANDROID_ID))
        params.putString("story_number", activeStory.titleNumber)
        params.putString("ethnolog", registration.getString("ethnologue", " "))
        params.putString("lwc", registration.getString("lwc", " "))
        params.putString("translator_email", registration.getString("translator_email", " "))
        params.putString("trainer_email", registration.getString("trainer_email", " "))
        params.putString("consultant_email", registration.getString("consultant_email", " "))
        firebaseAnalytics.logEvent(eventName, params)
    }

    fun setDemoWorkspace(context: Context){
        //set new path
        try {
            File(context.filesDir,"SPWorkspace").mkdir()
            val fileUri = getUriForFile(context,"${context.applicationContext.packageName}.fileprovider",File(context.filesDir,"/SPWorkspace"))
            workdocfile = DocumentFile.fromSingleUri(context,fileUri)!!
            registration.load(context)
        } catch ( e : Exception) {}
        //check if there are any files in there.  If not, add the demo
        if(!workspaceRelPathExists(context,DEMO_FOLDER)){
            //folder is not there, add the demo.
            val assetManager = context.assets
            var files: Array<String>? = null
            try {
                files = assetManager.list(DEMO_FOLDER)
            } catch (e: IOException) {
                Log.e("workspace", "Failed to get demo assets.", e)
                return
            }
            if (files != null) for (filename in files) {
                try {
                    val instream = assetManager.open("$DEMO_FOLDER/$filename")
                    val outstream = getChildOutputStream(context, "$DEMO_FOLDER/$filename")
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (instream.read(buffer).also { read = it } != -1) {
                        outstream!!.write(buffer, 0, read)
                    }
                    outstream?.close()
                    instream.close()
                } catch (e: Exception) {
                    Log.e("workspace", "Failed to copy demo asset file: $filename", e)
                }
            }
        }
        updateStories(context)
    }

    fun setupWorkspacePath(context: Context, uri: Uri){
        try {
            workdocfile = DocumentFile.fromTreeUri(context, uri)!!
            registration.load(context)
        } catch ( e : Exception) {}
        updateStories(context)
    }

    fun clearWorkspace(){
        workdocfile = DocumentFile.fromFile(File(""))
    }

    private fun updateStories(context: Context) {
        //Iterate external files directories.
        //for all files in the workspace, see if they are folders that have templates.
        if(storiesUpdated) return
        if(workdocfile.isDirectory) {
            //find all stories
            Stories.removeAll(Stories)
            val files = workdocfile.listFiles()
            for (storyPath in files) {
                //TODO - check storyPath.name against titles.
                unzipIfNewFolders(context, storyPath, files)
                //deleteWorkspaceFile(context, storyPath!!.name!!)
            }
            //After you unzipped the files, see if there are any new templates that we can read in.
            val newFiles = workdocfile.listFiles()
            for (storyPath in newFiles) {
                if (storyPath in files) continue
                //only read in new folders.
                if (storyPath.isDirectory) {
                    val story = parseStoryIfPresent(context, storyPath)
                    if (story != null) {
                        Stories.add(story)
                    }
                }
            }
        }
        //sort by title.
        Stories.sortBy{it.title}
        //update phases based upon registration selection
        phases = when(registration.getString("consultant_location_type")) {
            "remote" -> Phase.getRemotePhases()
            else -> Phase.getLocalPhases()
        }
        activePhaseIndex = 0
        updateStoryLocalCredits(context)
        storiesUpdated = true
    }

    fun deleteVideo(context: Context, path: String){
        activeStory.outputVideos.remove(path)
        deleteWorkspaceFile(context, "$VIDEO_DIR/$path")
    }

    fun updateStoryLocalCredits(context: Context) {
        for(story in Stories){
            for(slide in story.slides){
                if(slide.slideType == SlideType.LOCALCREDITS) { //local credits
                    if(slide.translatedContent == ""){
                        slide.translatedContent = context.getString(R.string.LC_starting_text)
                    }
                }
            }
        }
    }

    fun isLocalCreditsChanged(context: Context) : Boolean {
        var isChanged = false
        val orgLCText = context.getString(R.string.LC_starting_text)
        for(slide in activeStory.slides){
            if(slide.slideType == SlideType.LOCALCREDITS) { //local credits
                if(slide.translatedContent != orgLCText){
                    isChanged = true
                }
            }
        }
        return isChanged
    }

    fun getSongFilename() : String{
        for (s in activeStory.slides){
            if(s.slideType == SlideType.LOCALSONG){
                if(s.chosenDramatizationFile != "") return s.chosenDramatizationFile
                if(s.chosenDraftFile != "") return s.chosenDraftFile
            }
        }
        return ""
    }

    fun goToNextPhase() : Boolean {
        if(activePhaseIndex == -1) return false //phases not initizialized
        if(activePhaseIndex >= phases.size - 1) {
            activePhaseIndex = phases.size - 1
            return false
        }
        activePhaseIndex++
        activePhase = phases[activePhaseIndex]
        //there was a successful phase change!
        return true
    }

    fun goToPreviousPhase() : Boolean {
        if(activePhaseIndex == -1) return false //phases not initizialized
        if(activePhaseIndex <= 0) {
            activePhaseIndex = 0
            return false
        }
        activePhaseIndex--
        activePhase = phases[activePhaseIndex]
        //there was a successful phase change!
        return true
    }
}


