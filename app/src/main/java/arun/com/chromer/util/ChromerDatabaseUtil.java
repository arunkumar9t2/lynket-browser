package arun.com.chromer.util;

import android.content.Context;
import android.database.Cursor;

import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class ChromerDatabaseUtil {
    public static final String DATABASE_NAME = "database.db";

    // TODO To run clean up
    public static final String OLD_DATABASE_NAME = "Chromer_database.db";

    public static final String COLUMN_URL = "url";
    public static final String COLUMN_COLOR = "color";

    private DBHelper mDBHelper;

    public ChromerDatabaseUtil(Context context) {
        mDBHelper = DBHelper.getInstance(context);
    }

    void deInit() {
        if (mDBHelper != null) {
            Timber.d("Disposing");
            mDBHelper.close();
            mDBHelper = null;
        }
    }

    public int getColor(String url) {
        Cursor cursor = mDBHelper.getColorForUrl(url);
        if (cursor == null) return -1;
        if (cursor.getCount() == 0) return -1;
        int color = -1;
        try {
            cursor.moveToFirst();
            color = cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR));
            if (!cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // dispose
        deInit();
        return color;
    }

    public void insertColor(int color, String url) {
        if (mDBHelper != null) {
            mDBHelper.insertColor(color, url);
        }
        // dispose
        deInit();
    }
}

