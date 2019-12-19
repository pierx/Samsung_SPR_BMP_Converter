package com.convert.spr;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

public class MainActivity extends Activity {
    Button button;
    ImageView image;

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /*
     * Bitmap.CompressFormat can be PNG,JPEG or WEBP.
     *
     * quality goes from 1 to 100. (Percentage).
     *
     * dir you can get from many places like Environment.getExternalStorageDirectory() or mContext.getFilesDir()
     * depending on where you want to save the image.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        addListenerOnButton();
    }

    public void saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                 Bitmap.CompressFormat format, int quality) {
        if (bm == null) {
            Log.e("spr", "bm is null");
            return;
        }
        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format, quality, fos);

            fos.close();

        } catch (IOException e) {
            Log.e("app", Objects.requireNonNull(e.getMessage()));
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void addListenerOnButton() {

        image = findViewById(R.id.imageView1);
        button = findViewById(R.id.btnChangeImage);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                /* Loop through all drawables to convert them */
                final R.drawable drawableResources = new R.drawable();
                final Class<R.drawable> c = R.drawable.class;
                final Field[] fields = c.getDeclaredFields();

                for (Field field : fields) {
                    Log.i("spr", "Dumping: " + field);
                    final int resourceId;
                    try {
                        resourceId = field.getInt(drawableResources);
                    } catch (Exception e) {
                        continue;
                    }
                    /* make use of resourceId for accessing Drawables here */
                    @SuppressWarnings("deprecation") Drawable d = getResources().getDrawable(resourceId);
                    image.setImageDrawable(d);
                    Bitmap bm = drawableToBitmap(d);
                    // Bitmap bm = BitmapFactory.decodeResource(image.getResources(), resourceId);
                    String name = image.getResources().getResourceEntryName(resourceId);


                    File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "convert");

                    boolean doSave = true;
                    if (!dir.exists()) {
                        doSave = dir.mkdirs();
                    }

                    if (doSave) {
                        saveBitmapToFile(dir, name + ".png", bm, Bitmap.CompressFormat.PNG, 100);
                        Toast.makeText(MainActivity.this, "Done. \nCheck out the convert folder on storage", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("app", "Couldn't create target directory.");
                    }
                }
            }

        });
    }
}
