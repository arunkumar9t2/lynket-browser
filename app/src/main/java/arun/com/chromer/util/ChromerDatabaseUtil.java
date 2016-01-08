package arun.com.chromer.util;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by Arun on 06/01/2016.
 */
public class ChromerDatabaseUtil {
    public static final String DATABASE_NAME = "database.db";
    public static final String OLD_DATABASE_NAME = "Chromer_database.db";
    public static final String ColorTABLE = "Chromer_database.db";

    public static final String CONTACTS_COLUMN_URL = "url";
    public static final String COLUMN_COLOR = "color";

    private DBHelper dbHelper;

    public ChromerDatabaseUtil(Context context) {
        dbHelper = new DBHelper(context);
    }

    void deinit() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public int getColor(String url) {
        Cursor cursor = dbHelper.getColorForClass(url);
        if (cursor == null) {
            return -1;
        }
        if (cursor.isLast()) {
            return -1;
        }
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

        return color;
    }

    public void insertColor(int color, String url) {
        if (dbHelper != null) {
            dbHelper.insertColor(color, url);
        }
    }
}

