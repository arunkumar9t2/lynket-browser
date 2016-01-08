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


    public DBHelper(Context context) {
        super(context, ChromerDatabaseUtil.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table ExtractedColors (url text primary key, color integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ExtractedColors");
        onCreate(db);
    }

    public boolean insertColor(int color, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("color", color);
        contentValues.put("url", url);
        db.insert("ExtractedColors", null, contentValues);
        return true;
    }

    public Cursor getColorForClass(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from ExtractedColors where url = ?", new String[]{url});
        return res;
    }
}
