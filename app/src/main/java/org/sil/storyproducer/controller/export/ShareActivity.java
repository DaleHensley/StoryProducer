package org.sil.storyproducer.controller.export;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.sil.storyproducer.R;
import org.sil.storyproducer.controller.phase.PhaseBaseActivity;
import org.sil.storyproducer.model.StoryState;
import org.sil.storyproducer.tools.StorySharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by annmcostantino on 10/1/2017.
 */
//TODO: Cleanup all the useless stuff here
public class ShareActivity extends PhaseBaseActivity {

    private static final String PREF_FILE = "Share_Config";

    private TextView mShareHeader;
    private LinearLayout mShareSection;
    private TextView mNoVideosText;
    private ListView mVideosListView;

    private ExportedVideosAdapter videosAdapter;
    private String mStory;


    //accordion variables
    private final int [] sectionIds = {R.id.share_section};
    private final int [] headerIds = {R.id.share_header};
    private View[] sectionViews = new View[sectionIds.length];
    private View[] headerViews = new View[headerIds.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStory = StoryState.getStoryName();     //needs to be set first because some of the views use it
        boolean phaseUnlocked = StorySharedPreferences.isApproved(mStory, this);
        setContentView(R.layout.activity_share);
        mStory = StoryState.getStoryName();
        setupViews();
        invalidateOptionsMenu();
        if (phaseUnlocked) {
            findViewById(R.id.lock_overlay).setVisibility(View.INVISIBLE);
        } else {
            View mainLayout = findViewById(R.id.main_linear_layout);
            PhaseBaseActivity.disableViewAndChildren(mainLayout);
        }
        loadPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        savePreferences();

        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item =  menu.getItem(0);
        item.setIcon(R.drawable.ic_create); //TODO: new share icon
        return true;
    }

    /**
     * Get handles to all necessary views and add some listeners.
     */
    //TODO: make this function work with new activity_share layout
    private void setupViews() {

        //Initialize sectionViews[] with the integer id's of the various LinearLayouts
        //Add the listeners to the LinearLayouts's header section.
        for (int i = 0; i < sectionIds.length; i++) {
            sectionViews[i] = findViewById(sectionIds[i]);
            headerViews[i] = findViewById(headerIds[i]);
            setAccordionListener(findViewById(headerIds[i]), sectionViews[i]);
        }


        //share view
        mShareHeader = (TextView) findViewById(R.id.share_header);
        mShareSection = (LinearLayout) findViewById(R.id.share_section);
        videosAdapter = new ExportedVideosAdapter(this);
        mVideosListView = (ListView) findViewById(R.id.videos_list);
        mVideosListView.setAdapter(videosAdapter);
        mNoVideosText = (TextView)findViewById(R.id.no_videos_text);
        setVideoAdapterPaths();

    }

    /**
     * This function sets the click listeners to implement the accordion functionality
     * for each section of the registration page
     *
     * @param headerView  a variable of type View denoting the field the user will click to open up
     *                    a section of the registration
     * @param sectionView a variable of type View denoting the section that will open up
     */
    private void setAccordionListener(final View headerView, final View sectionView) {
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sectionView.getVisibility() == View.GONE) {
                    setSectionsClosedExceptView(sectionView);
                } else {
                    sectionView.setVisibility(View.GONE);
                    headerView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.gray, null));
                }
            }
        });
    }

    /**
     * sets all the accordion sections closed except for the one passed
     * @param sectionView that is wanted to be made open
     */
    private void setSectionsClosedExceptView(View sectionView) {
        for(int k = 0; k < sectionViews.length; k++) {
            if(sectionViews[k] == sectionView) {
                sectionViews[k].setVisibility(View.VISIBLE);
                headerViews[k].setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
            } else {
                sectionViews[k].setVisibility(View.GONE);
                headerViews[k].setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.gray, null));
            }
        }

    }

    /**
     * sets the videos for the list adapter
     */
    private void setVideoAdapterPaths() {
        List<String> actualPaths = getExportedVideosForStory();
        if(actualPaths.size() > 0) {
            mNoVideosText.setVisibility(View.GONE);
        }
        videosAdapter.setVideoPaths(actualPaths);
    }

    /**
     * Returns the the video paths that are saved in preferences and then checks to see that they actually are files that exist
     * @return Array list of video paths
     */
    private List<String> getExportedVideosForStory() {
        List<String> actualPaths = new ArrayList<>();
        List<String> videoPaths = StorySharedPreferences.getExportedVideosForStory(mStory);
        for(String path : videoPaths) {          //make sure the file actually exists
            File file = new File(path);
            if(file.exists() && !actualPaths.contains(path)) {
                actualPaths.add(path);
            }
            else {
                //If the file doesn't exist or we encountered it a second time in the list, remove it.
                StorySharedPreferences.removeExportedVideoForStory(path, mStory);
            }
        }
        return actualPaths;
    }

    /**
     * Save current configuration options to shared preferences.
     */
    //TODO PROB DONT NEED THESE ANYMORE
    private void savePreferences() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
        editor.apply();
    }

    /**
     * Load configuration options from shared preferences.
     */
    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
    }


}
