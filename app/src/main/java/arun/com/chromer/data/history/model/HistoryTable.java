package arun.com.chromer.data.history.model;

/**
 * Created by Arunkumar on 03-03-2017.
 */
public class HistoryTable {
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_URL = "URL";
    public static final String COLUMN_TITLE = "TITLE";
    public static final String COLUMN_FAVICON = "FAVICON";
    public static final String COLUMN_CANONICAL = "CANONICAL";
    public static final String COLUMN_COLOR = "COLOR";
    public static final String COLUMN_AMP = "AMP";
    public static final String COLUMN_BOOKMARKED = "BOOKMARKED";
    public static final String COLUMN_CREATED_AT = "CREATED";
    public static final String COLUMN_VISITED = "VISITED";

    public static final String TABLE_NAME = "History";

    public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " ( " +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_URL + " TEXT NOT NULL ," +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_FAVICON + " TEXT, " +
            COLUMN_CANONICAL + " TEXT, " +
            COLUMN_COLOR + " TEXT, " +
            COLUMN_AMP + " TEXT, " +
            COLUMN_BOOKMARKED + " INTEGER, " +
            COLUMN_CREATED_AT + " TEXT, " +
            COLUMN_VISITED + " INTEGER" +
            ");";

    public static final String[] ALL_COLUMN_PROJECTION = new String[]{
            COLUMN_URL,
            COLUMN_TITLE,
            COLUMN_FAVICON,
            COLUMN_CANONICAL,
            COLUMN_COLOR,
            COLUMN_AMP,
            COLUMN_BOOKMARKED,
            COLUMN_CREATED_AT,
            COLUMN_VISITED
    };

    public static final String ORDER_BY_TIME_DESC = " CREATED DESC";
}
