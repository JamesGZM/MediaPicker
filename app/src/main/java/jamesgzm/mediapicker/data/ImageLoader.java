package jamesgzm.mediapicker.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;


import java.util.ArrayList;

import jamesgzm.mediapicker.R;
import jamesgzm.mediapicker.entity.Folder;
import jamesgzm.mediapicker.entity.Media;

/**
 * Created by dmcBig on 2017/7/3.
 */

public class ImageLoader extends LoaderM implements LoaderManager.LoaderCallbacks {

    String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID};

    Context mContext;
    DataCallback mLoader;

    public ImageLoader(Context context, DataCallback loader) {
        this.mContext = context;
        this.mLoader = loader;
    }

    @Override
    public Loader onCreateLoader(int picker_type, Bundle bundle) {
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        CursorLoader cursorLoader = new CursorLoader(
                mContext,
                queryUri,
                IMAGE_PROJECTION,
                null,
                null, // Selection args (none).
                MediaStore.Images.Media.DATE_ADDED + " DESC" // Sort order.
        );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {
        ArrayList<Folder> folders = new ArrayList<>();
        Folder allFolder = new Folder(mContext.getResources().getString(R.string.all_image));
        folders.add(allFolder);
        Cursor cursor = (Cursor) o;
        while (cursor.moveToNext()) {

            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
            int mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

            if (size < 1) continue;
            String dirName = getParent(path);
            Media media = new Media(path, name, dateTime, mediaType, size, id, dirName);
            allFolder.addMedias(media);

            int index = hasDir(folders, dirName);
            if (index != -1) {
                folders.get(index).addMedias(media);
            } else {
                Folder folder = new Folder(dirName);
                folder.addMedias(media);
                folders.add(folder);
            }
        }
        mLoader.onData(folders);
        try {
            //4.0以上的版本会自动关闭 (4.0--14;; 4.0.3--15)
            if (Build.VERSION.SDK_INT < 14) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }


}