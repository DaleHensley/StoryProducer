package org.sil.storyproducer.model;

import android.graphics.Bitmap;

public class ListFiles {
    public Bitmap icon;
    public String title;
    public String subtitle;

    public ListFiles() {
        super();
    }

    public ListFiles(Bitmap icon, String title, String subtitle) {
        super();
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
    }
}
