package uk.ignas.langlearn.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TranslationDaoSqlite extends SQLiteOpenHelper implements TranslationDao {

    public static final String DATABASE_NAME = "MyDBName3.db";
    public static final String TRANSLATIONS_TABLE_NAME = "translations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ORIGINAL_WORD = "originalWord";
    public static final String COLUMN_TRANSLATED_WORD = "translatedWord";
    public static final String COLUMN_WORD_DIFFICULTY = "difficulty";
    public static final int ERROR_OCURRED = -1;

    public TranslationDaoSqlite(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TRANSLATIONS_TABLE_NAME + " " +
                        "(" +
                        COLUMN_ID + " integer primary key, " +
                        COLUMN_ORIGINAL_WORD + " text," +
                        COLUMN_TRANSLATED_WORD + " text, " +
                        COLUMN_WORD_DIFFICULTY + " text, " +
                        "CONSTRAINT uniqueWT UNIQUE (" + COLUMN_ORIGINAL_WORD + ", " + COLUMN_TRANSLATED_WORD + ")" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TRANSLATIONS_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void insert(List<Translation> translations) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (Translation translation: translations) {
                if (!this.insertSingle(translation)) {
                    throw new RuntimeException("could not insert all values");
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean insertSingle(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ORIGINAL_WORD, translation.getOriginalWord());
        contentValues.put(COLUMN_TRANSLATED_WORD, translation.getTranslatedWord());
        contentValues.put(COLUMN_WORD_DIFFICULTY, Difficulty.EASY.name());
        long id = db.insert(TRANSLATIONS_TABLE_NAME, null, contentValues);
        return id != ERROR_OCURRED;
    }

    @Override
    public int update(int id, String originalWord, String translatedWord, Difficulty difficulty) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_WORD_DIFFICULTY, difficulty.name());
            contentValues.put(COLUMN_ORIGINAL_WORD, originalWord);
            contentValues.put(COLUMN_TRANSLATED_WORD, translatedWord);
            return db.update(TRANSLATIONS_TABLE_NAME, contentValues,
                    COLUMN_ID + " = ? ",
                    new String[]{String.valueOf(id)});

        } catch (SQLiteConstraintException e) {
            deleteById(id);
            return 1;
        }
    }

    @Override
    public void delete(Set<Translation> translations) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (Translation translation: translations) {
                this.deleteSingle(translation);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private Integer deleteById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TRANSLATIONS_TABLE_NAME,
                COLUMN_ID + " = ? ",
                new String[]{String.valueOf(id)});
    }

    private Integer deleteSingle(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TRANSLATIONS_TABLE_NAME,
                COLUMN_ORIGINAL_WORD + " = ? AND " + COLUMN_TRANSLATED_WORD + " = ? ",
                new String[]{translation.getOriginalWord(), translation.getTranslatedWord()});
    }

    @Override
    public LinkedHashMap<Translation, Difficulty> getAllTranslations() {
        LinkedHashMap<Translation, Difficulty> translations = new LinkedHashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TRANSLATIONS_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            translations.put(new Translation(
                    res.getInt(res.getColumnIndex(COLUMN_ID)),
                    res.getString(res.getColumnIndex(COLUMN_ORIGINAL_WORD)),
                    res.getString(res.getColumnIndex(COLUMN_TRANSLATED_WORD))),
                    Difficulty.valueOf(res.getString(res.getColumnIndex(COLUMN_WORD_DIFFICULTY))));
            res.moveToNext();
        }
        res.close();
        return translations;
    }
}