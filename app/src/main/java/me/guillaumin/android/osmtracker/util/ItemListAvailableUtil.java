package me.guillaumin.android.osmtracker.util;

/**
 * Created by adma9717 on 19/04/18.
 */

public class ItemListAvailableUtil {
    private String availableLayoutName;
    private String availableLayoutDescription;

    public ItemListAvailableUtil(String layoutName, String layoutDescription){
        this.availableLayoutName = layoutName;
        this.availableLayoutDescription = layoutDescription;
    }

    public String getAvailableLayoutName() {
        return availableLayoutName;
    }

    public String getAvailableLayoutDescription() {
        return availableLayoutDescription;
    }
}
