package uk.ignas.langlearn.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;

import java.util.LinkedHashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName2.db";
    public static final String TRANSLATIONS_TABLE_NAME = "translations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ORIGINAL_WORD = "originalWord";
    public static final String COLUMN_TRANSLATED_WORD = "translatedWord";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TRANSLATIONS_TABLE_NAME + " " +
                        "(" + COLUMN_ID + " integer primary key, " + COLUMN_ORIGINAL_WORD + " text," + COLUMN_TRANSLATED_WORD + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TRANSLATIONS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertContact(String originalWord, String translatedWord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ORIGINAL_WORD, originalWord);
        contentValues.put(COLUMN_TRANSLATED_WORD, translatedWord);
        db.insert(TRANSLATIONS_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + TRANSLATIONS_TABLE_NAME + " where id=" + id + "", null);
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TRANSLATIONS_TABLE_NAME);
    }

    public Integer deleteContact(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TRANSLATIONS_TABLE_NAME,
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TRANSLATIONS_TABLE_NAME);
    }

    public LinkedHashMap<Translation, Difficulty> getAllTranslations() {
        LinkedHashMap<Translation, Difficulty> translations = new LinkedHashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TRANSLATIONS_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            translations.put(new Translation(res.getString(res.getColumnIndex(COLUMN_ORIGINAL_WORD)), res.getString(res.getColumnIndex(COLUMN_TRANSLATED_WORD))), Difficulty.EASY);
            res.moveToNext();
        }
        res.close();
        return translations;
    }
}