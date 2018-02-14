package me.guillaumin.android.osmtracker.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;

import me.guillaumin.android.osmtracker.OSMTracker;
import me.guillaumin.android.osmtracker.R;
import me.guillaumin.android.osmtracker.layout.DownloadCustomLayoutTask;
import me.guillaumin.android.osmtracker.util.CustomLayoutsUtils;
import me.guillaumin.android.osmtracker.util.FileSystemUtils;

/**
 * Created by emmanuel on 20/10/17.
 */

public class ButtonsPresets extends Activity {

    private CheckBox checkboxHeld;
    private CheckBoxChangedListener listener;
    private CheckBox selected;
    private CheckBox defaultCheckBox;
    private SharedPreferences prefs;
    //Container for the file names and the presentation names
    private static Hashtable<String, String> layoutsFileNames;
    private static String storageDir;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        initializeAttributes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActivity();
    }

    public void refreshActivity(){
        LinearLayout downloadedLayouts = (LinearLayout) findViewById(R.id.list_layouts);
        //main layout for the default layout checkbox
        LinearLayout defaultSection = (LinearLayout) findViewById(R.id.buttons_presets);
        listLayouts(downloadedLayouts);
        checkCurrentLayout(downloadedLayouts, defaultSection);
    }

    private void initializeAttributes(){
        setTitle("Buttons Presets");
        setContentView(R.layout.buttons_presets);
        listener = new CheckBoxChangedListener();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        layoutsFileNames = new Hashtable<String, String>();
        storageDir = File.separator + prefs.getString(OSMTracker.Preferences.KEY_STORAGE_DIR,
                                                 OSMTracker.Preferences.VAL_STORAGE_DIR);
    }

    private void listLayouts(LinearLayout rootLayout){
        File layoutsDir = new File(Environment.getExternalStorageDirectory(), storageDir + File.separator + Preferences.LAYOUTS_SUBDIR + File.separator);
        int AT_START = 0; //the position to insert the view at
        int fontSize = 20;
        if (layoutsDir.exists() && layoutsDir.canRead()) {
            //Ask for the layout's filenames
            String[] layoutFiles = layoutsDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(Preferences.LAYOUT_FILE_EXTENSION);
                }
            });
            //Remove all the layouts
            while(rootLayout.getChildAt(0) instanceof CheckBox){
                rootLayout.removeViewAt(0);
            }
            //Fill with the new ones
            for(String name : layoutFiles) {
                CheckBox newCheckBox = new CheckBox(this);
                newCheckBox.setTextSize((float) fontSize);
                String newName = CustomLayoutsUtils.convertFileName(name, true);
                layoutsFileNames.put(newName, name);
                newCheckBox.setText(newName);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(60, 0, 0, 0);
                newCheckBox.setLayoutParams(layoutParams);
                newCheckBox.setPadding(10,20,10,20);
                newCheckBox.setOnClickListener(listener);
                registerForContextMenu(newCheckBox);
                rootLayout.addView(newCheckBox, AT_START);
            }
        }

        defaultCheckBox = (CheckBox) findViewById(R.id.def_layout);
        defaultCheckBox.setOnClickListener(listener);
        //this is the maping default->default
        layoutsFileNames.put(OSMTracker.Preferences.VAL_UI_BUTTONS_LAYOUT,OSMTracker.Preferences.VAL_UI_BUTTONS_LAYOUT);
        //verify the size of the layoutsFileNames, if it is greater than 1, we put invisible the message (in the downloaded layouts section)
        if(layoutsFileNames.size() > 1){
            TextView empyText = (TextView) findViewById(R.id.btnpre_empty);
            empyText.setVisibility(View.INVISIBLE);
        }else{
            TextView empyText = (TextView) findViewById(R.id.btnpre_empty);
            empyText.setVisibility(View.VISIBLE);
        }
    }


    /**
     * @param downloadedLayouts: this linear layout contains the downloaded custom layouts representation
     * @param defaultSection: it contains the default layout representation
     * It asks for the layout being used and checks it in the list
     */
    private void checkCurrentLayout(LinearLayout downloadedLayouts, LinearLayout defaultSection){
        String activeLayoutName = prefs.getString(OSMTracker.Preferences.KEY_UI_BUTTONS_LAYOUT, OSMTracker.Preferences.VAL_UI_BUTTONS_LAYOUT);
        boolean defLayout = false;

        //first, we check if the default layout is activated
        View defCheck = defaultSection.getChildAt(1); //the default checkbox in the activity
        if(defCheck instanceof CheckBox){
            CheckBox defCheckCast = (CheckBox) defCheck;
            String defCheckName = layoutsFileNames.get(defCheckCast.getText());
            if (activeLayoutName.equals(defCheckName)) {
                selected = defCheckCast;
                defLayout = true;
            }
        }

        boolean found = false;
        //then, if the default layout isn't activated, we verify the other layouts
        if (!defLayout) {
            for (int i = 0; i < downloadedLayouts.getChildCount(); i++) {
                View current = downloadedLayouts.getChildAt(i);
                if (current instanceof CheckBox) {
                    CheckBox currentCast = (CheckBox) current;
                    String currentName = layoutsFileNames.get(currentCast.getText());
                    if (activeLayoutName.equals(currentName)) {
                        selected = currentCast;
                        found = true;
                        break;
                    }
                }
            }
            //if not found the active layout then set the default
            if(!found){
                selected = (CheckBox) defCheck;
                String targetLayout = layoutsFileNames.get(selected.getText());
                prefs.edit().putString(OSMTracker.Preferences.KEY_UI_BUTTONS_LAYOUT,
                        targetLayout).commit();
                //reload the activity
                refreshActivity();
            }
        }

        selected.setChecked(true);
    }

    //Class that manages the changes on the selected layout
    private class CheckBoxChangedListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CheckBox pressed = (CheckBox)view;
            selected.setChecked(false);
            pressed.setChecked(true);
            selected=pressed;
            String targetLayout = layoutsFileNames.get(pressed.getText());
            prefs.edit().putString(OSMTracker.Preferences.KEY_UI_BUTTONS_LAYOUT,
                    targetLayout).commit();
        }
    }

    //methods for the context menu for each checkbox
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //inflate the menu for the view selected
        getMenuInflater().inflate(R.menu.btnprecb_context_menu, menu);
        checkboxHeld = (CheckBox) v;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //this case download again the layout held and install it
            case R.id.cb_update_and_install:
                String layoutName = checkboxHeld.getText().toString();
                String iso = getIso(layoutsFileNames.get(checkboxHeld.getText()));
                String info[]= {layoutName, iso};
                new DownloadCustomLayoutTask(this){
                    protected void onPostExecute(Boolean status){
                        //if the download is correct we activate it
                        if (status) {
                            String targetLayout = layoutsFileNames.get(checkboxHeld.getText());
                            prefs.edit().putString(OSMTracker.Preferences.KEY_UI_BUTTONS_LAYOUT,
                                    targetLayout).commit();
                            //re-load the activity
                            refreshActivity();
                            Toast.makeText(getApplicationContext(), "Layout was updated successfully", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Layout was not updated, try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute(info);
                break;
            //this case open a new confirm dialog to delete a layout, also, if the layout have a icon directory, it is deleted
            case R.id.cb_delete:
                new AlertDialog.Builder(this).
                setTitle(checkboxHeld.getText())
                .setMessage("Are you sure to delete the " + checkboxHeld.getText() + " layout?")
                .setCancelable(true)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = layoutsFileNames.get(checkboxHeld.getText());
                        String rootDir = storageDir + File.separator + Preferences.LAYOUTS_SUBDIR + File.separator;
                        File fileToDelete = new File(Environment.getExternalStorageDirectory(), rootDir + fileName);

                        if(FileSystemUtils.delete(fileToDelete, false)){
                            Toast.makeText(getApplicationContext(), "The file was deleted successfully", Toast.LENGTH_SHORT).show();

                            String iconDirName = fileName.substring(0, fileName.length() - CustomLayoutsUtils.LAYOUT_EXTENSION_ISO.length());
                            File iconDirToDelete = new File(Environment.getExternalStorageDirectory(), rootDir + iconDirName);

                            if(FileSystemUtils.delete(iconDirToDelete, true)){
                                Toast.makeText(getApplicationContext(), "The icon directory was deleted successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "This file didn't have any icon directory associated", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(getApplicationContext(), "The file could not be delete", Toast.LENGTH_SHORT).show();
                        }
                        //reload the activity
                        refreshActivity();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    //this method obtain the iso of any layout file name
    private String getIso(String layoutName){
        String tmp = layoutName.substring(0, layoutName.length() - Preferences.LAYOUT_FILE_EXTENSION.length());
        String iso = "";
        for (int i=tmp.length() - AvailableLayouts.ISO_CHARACTER_LENGTH; i<tmp.length(); i++){
                iso += tmp.charAt(i);
        }
        return iso;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.launch_available_layouts_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.launch_available){
            startActivity(new Intent(this,AvailableLayouts.class));
        }
        return super.onOptionsItemSelected(item);
    }
}