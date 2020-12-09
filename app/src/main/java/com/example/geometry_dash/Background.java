package com.example.geometry_dash;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Background {
    int x = 0, y = 0;
    Bitmap background;

    public Background(int screenX, int screenY, Resources res, int drawable) {
        background = BitmapFactory.decodeResource(res, drawable);
        background = Bitmap.createScaledBitmap(background, screenX, screenY, false);

    }
}
