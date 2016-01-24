package arun.com.chromer.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Arun on 08/01/2016.
 */
class DBHelper extends SQLiteOpenHelper {

    private static DBHelper mInstance;


    public DBHelper(Context context) {
        super(context, ChromerDatabaseUtil.DATABASE_NAME, null, 1);
    }

    public static DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table ExtractedColors (url text primary key, color integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists ExtractedColors");
        onCreate(db);
    }

    public boolean insertColor(int color, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChromerDatabaseUtil.COLUMN_COLOR, color);
        contentValues.put(ChromerDatabaseUtil.COLUMN_URL, url);
        db.insert("ExtractedColors", null, contentValues);
        // Jan 24, 2016 - Close connection
        db.close();
        return true;
    }

    public Cursor getColorForUrl(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from ExtractedColors where url = ?", new String[]{url});
        return res;
    }

}
