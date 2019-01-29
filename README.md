# Story Producer (SP app)

Purpose: to enable end users to translate still-picture audio-video stories with community help, then dramatize, publish and digitally share new local language videos. 

End users and target audience: speakers of (minority) languages, some of whom are bilingual in a major language

End recipients: members of local language communities

Developers:  
   * Vision and design by Robin Rempel, SIL
   * Logo by Jordan Skomer and Mackenzie Sanders
   * Prototype app: LeTourneau University engineering students (Jordan Skomer, Hannah Brown, Caleb Phillips)
   * Software engineers for v1.2: Cedarville University Bible Story Team (Noah Bragg, Grant Dennison, Robert Jacubec, Andrew Lockridge, Michael Baxter and Abigail Riffle guided by Dr. Seth Hamman)
   * Software engineers for Remote Oral Consultant Checker website and app v1.4 and v2.0: Cedarville University student team #2 (Blake Lasky, Daniel Weaver, Ann Costantino and Brendon Burns guided by Dr. Seth Hamman)
   * Software engineer for v2.3 and later: John Lambert

Translate and produce stories (starting with templates in a major language made up of images, audio and text) by working through these phases:  
* [REGISTER project and team information (one time)]
* LEARN or internalize the story
* TRANSLATE/DRAFT an audio translation
* COMMUNITY CHECK the draft for naturalness and clarity
* CONSULTANT CHECK for accuracy and approval
* DRAMATIZE the final audio
* FINALIZE/CREATE new videos
* SHARE the videos

## Installing the application
* Minimum Requirements:  
    * Android 4.4.x (API 19) and 1GB storage available
    * Android file browser: [ASTRO File Manager](https://play.google.com/store/apps/details?id=com.metago.astro&hl=en) or [File Manager](https://play.google.com/store/apps/details?id=com.asus.filemanager&hl=en) by ZenUI  
* Prepare your Android device: 
    * If you have installed Story Producer before, delete the previous version. If the version of the app that you delete is pre-2.3, deleting the app will delete all the template folders and any work you did on the translations.  After 2.3, the data is stored in a different location and nothing is lost.
    * Enable a manual install of apk file: 
        * Settings(gear icon) -> Security -> (scroll down) Enable "Unknown sources"; disable "Verify apps"
    * Insert SD card if desired
         * You will need approximately 300 MB of storage for each story you wish to produce.
    * Connect to your device via USB (it will show as a drive on your computer)
        * If it does not show up as a drive, swipe down from the top.  You should see a notification: USB for charging.  Touch it.  Select USB for file transfer.
    * Download StoryProducer_v[most recent version].apk and copy the file onto your phone or tablet. Use your file manager on your Android device to open the apk and install it onto your device.
    * Download the [template folders](https://drive.google.com/drive/folders/0Bw7whhMtjqJTVkljWlY0akZXeDg?usp=sharing) to your computer
    * Copy the unzipped templates folder to your usb card.  A good directory would be **\[thumb drive\]/SPWorkspace**
    * Open Story Producer and select the "workspace" which is the folder that all the templates are in.  (Not the folder of a specific template, but the folder that contains the templates.)
    * Continue with registration and use the app!

## Unit Tests
#### Organization
* All unit tests are located in the `app\src\test\java\org\sil\storyproducer\test` directory.
* Test files are named for the class that they test, plus the word "Test" prepended.
    * >**Example:** `TestRegistrationActivity` contains tests for the `RegistrationActivity` class.
* Folders in the unit test directory correspond to folders in the source directory.
    * >**Example:** `org.sil.storyproducer.test/controller/` contains tests for code that lives in `org.sil.storyproducer/controller/`
* Individual tests are named according to the following format: `FunctionName_When_[Condition]_Should_[DoExpectedBehavior]`
    * >**Example:** `OnCreate_When_WorkspaceDirectoryIsAlreadySet_Should_NotStartFileTreeActivity`

#### Running the Tests
##### From the command line:
1. Navigate to the root directory of the repository.
2. Run `./gradlew test` (on Linux) or `gradlew.bat test`(on Windows).
*Note:* You may need to run the gradle wrapper with sudo or make the gradle wrapper executable with `sudo chmod +x ./gradlew`
##### From Android Studio:
1. Open the Story Producer project (StoryProducer.iml) in Android Studio.
2. Set the "Project" tool window to show "Local Unit Tests" (or just navigate a the folder containing unit tests).
3. Right click on one of the files or directories that contains some unit tests (this can be the "app" directory, a specific folder, or a single test file.).
4. Click "Run 'All Tests'" (or a more specific option if you chose a folder or file).
5. The "Run" tool window shows the results of the tests.
*Note:* If no tests appear in the "Run" window, you may need to toggle the visibility of passing tests. Currently, the toggle button looks like green checkmark inside of a circle.