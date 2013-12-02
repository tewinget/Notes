package com.foobar.notes;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.database.Cursor;

import java.text.DateFormat;
import java.util.Date;


/**
 * Created by tom on 12/1/13.
 */
public class NotesDB extends SQLiteOpenHelper
{
    private static final String DB_NAME = "notes";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "notes";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_LAST_UPDATE = "last_update";

    private static final String[] COLUMNS = new String[] {
            BaseColumns._ID,
            COLUMN_TITLE,
            COLUMN_CONTENT,
            COLUMN_LAST_UPDATE
    };

    private static final String DB_CREATE = "create table " + TABLE_NAME + "( "
        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_TITLE + " STRING,"
        + COLUMN_CONTENT + " STRING,"
        + COLUMN_LAST_UPDATE + " INTEGER"
        + " );";

    private static final String SORT_ORDER = COLUMN_LAST_UPDATE + " DESC";

    public static final class Note
    {
        public int id = -1;
        public String title = "";
        public String content = "";
        public long last_update = 0;

        public String time()
        {
            DateFormat df = DateFormat.getDateTimeInstance();
            return df.format(new Date(last_update));
        }
    }

    private SQLiteDatabase db;

    public NotesDB(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DB_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    public void delete(int id)
    {
        if (db != null)
        {
            db.delete(TABLE_NAME, "where " + BaseColumns._ID + "= " + id, null);
        }
    }

    public void open()
    {
        db = getWritableDatabase();
    }

    public void close()
    {
        db.close();
    }

    public long save(Note toSave)
    {
        toSave.last_update = System.currentTimeMillis();

        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, toSave.title);
        values.put(COLUMN_CONTENT, toSave.content);
        values.put(COLUMN_LAST_UPDATE, toSave.last_update);

        long result = -1;

        if (db != null)
        {
            if (toSave.id < 0)
            {
                result = db.insert(TABLE_NAME, null, values);
            }
            else
            {
                int res = db.update(
                                        TABLE_NAME,
                                        values,
                                        "where " + BaseColumns._ID + "= " + toSave.id,
                                        null
                                    );
                if (res > 0)
                {
                    result = toSave.id;
                }
            }
        }
        return result;
    }

    public Note load(int id)
    {
        if (db == null) return null;

        Cursor cursor = db.query(TABLE_NAME, COLUMNS, BaseColumns._ID + " = ?", new String[] { String.valueOf(id)}, null, null, null, "1");

        if (!cursor.moveToFirst()) return null;

        Note note = new Note();

        note.id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        note.content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
        note.title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
        note.last_update = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_UPDATE));

        cursor.close();
        return note;
    }

    public Cursor loadAll()
    {
        if (db == null) return null;

        return db.query(TABLE_NAME, COLUMNS, null, null, null, null, SORT_ORDER);
    }
}
