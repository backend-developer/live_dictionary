package uk.ignas.langlearn.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TranslationDaoSqlite extends SQLiteOpenHelper implements TranslationDao {

    public static final String DATABASE_NAME = "MyDBName3.db";
    public static final String TRANSLATIONS_TABLE_NAME = "translations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NATIVE_WORD = "nativeWord";
    public static final String COLUMN_FOREIGN_WORD = "foreignWord";
    public static final String COLUMN_WORD_DIFFICULTY = "difficulty";
    public static final String COLUMN_EASY_LATEST_1 = "marked_as_easy_latest1";
    public static final String COLUMN_EASY_LATEST_2 = "marked_as_easy_latest2";
    public static final String COLUMN_EASY_LATEST_3 = "marked_as_easy_latest3";
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
                        COLUMN_NATIVE_WORD + " text," +
                        COLUMN_FOREIGN_WORD + " text, " +
                        COLUMN_WORD_DIFFICULTY + " text, " +
                        COLUMN_EASY_LATEST_1 + " integer, " +
                        COLUMN_EASY_LATEST_2 + " integer, " +
                        COLUMN_EASY_LATEST_3 + " integer, " +
                        "CONSTRAINT uniqueWT UNIQUE (" + COLUMN_NATIVE_WORD + ", " + COLUMN_FOREIGN_WORD + ")" +
                        ")"
        );

        insertSingleUsingDb(new Translation(new ForeignWord("morado"), new NativeWord("purple")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("verde"), new NativeWord("green")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("rosa"), new NativeWord("pink")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("rojo"), new NativeWord("red")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("plateado"), new NativeWord("silver")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("negro"), new NativeWord("black")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("naranja"), new NativeWord("orange")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("marrón"), new NativeWord("brown")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("gris"), new NativeWord("grey")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("fucsia"), new NativeWord("fuchsia")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("dorado"), new NativeWord("gold")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("blanco"), new NativeWord("white")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("beige"), new NativeWord("beige")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("azul marino"), new NativeWord("navy blue")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("azul"), new NativeWord("blue")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("amarillo"), new NativeWord("yellow")), db);
        insertSingleUsingDb(new Translation(new ForeignWord("Los coroles"), new NativeWord("colors")), db);
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
        return insertSingleUsingDb(translation, db);
    }

    private boolean insertSingleUsingDb(Translation translation, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NATIVE_WORD, translation.getNativeWord().get());
        contentValues.put(COLUMN_FOREIGN_WORD, translation.getForeignWord().get());
        contentValues.put(COLUMN_WORD_DIFFICULTY, Difficulty.EASY.name());
        long id = db.insert(TRANSLATIONS_TABLE_NAME, null, contentValues);
        return id != ERROR_OCURRED;
    }

    @Override
    public int update(int id, ForeignWord foreignWord, NativeWord nativeWord, TranslationMetadata metadata) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_WORD_DIFFICULTY, metadata.getDifficulty().name());
            if (metadata.getRecentMarkingAsEasy().size() == 1) {
                contentValues.put(COLUMN_EASY_LATEST_1, metadata.getRecentMarkingAsEasy().get(0).getTime());
            }
            if (metadata.getRecentMarkingAsEasy().size() == 2) {
                contentValues.put(COLUMN_EASY_LATEST_2, metadata.getRecentMarkingAsEasy().get(1).getTime());
            }
            if (metadata.getRecentMarkingAsEasy().size() == 3) {
                contentValues.put(COLUMN_EASY_LATEST_3, metadata.getRecentMarkingAsEasy().get(2).getTime());
            }
            contentValues.put(COLUMN_NATIVE_WORD, nativeWord.get());
            contentValues.put(COLUMN_FOREIGN_WORD, foreignWord.get());
            return db.update(TRANSLATIONS_TABLE_NAME, contentValues,
                    COLUMN_ID + " = ? ",
                    new String[]{String.valueOf(id)});

        } catch (SQLiteConstraintException e) {
            deleteById(id);
            return 1;
        }
    }

    @Override
    public void delete(Collection<Translation> translations) {
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
                COLUMN_NATIVE_WORD + " = ? AND " + COLUMN_FOREIGN_WORD + " = ? ",
                new String[]{translation.getNativeWord().get(), translation.getForeignWord().get()});
    }

    @Override
    public List<Translation> getAllTranslations() {
        List<Translation> translations = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TRANSLATIONS_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            List<Date> recentLatestDatesWhenMarketAsEasy = new ArrayList<>();
            Date now = new Date();
            long easyLatestTimestamp1 = res.getInt(res.getColumnIndex(COLUMN_EASY_LATEST_1));
            long easyLatestTimestamp2 = res.getInt(res.getColumnIndex(COLUMN_EASY_LATEST_2));
            long easyLatestTimestamp3 = res.getInt(res.getColumnIndex(COLUMN_EASY_LATEST_3));
            if (easyLatestTimestamp1 != 0 && TimeUnit.MILLISECONDS.toHours(now.getTime() - easyLatestTimestamp1) > 1) {
                recentLatestDatesWhenMarketAsEasy.add(new Date(easyLatestTimestamp1));
            }
            if (easyLatestTimestamp2 != 0 && TimeUnit.MILLISECONDS.toHours(now.getTime() - easyLatestTimestamp2) > 1) {
                recentLatestDatesWhenMarketAsEasy.add(new Date(easyLatestTimestamp2));
            }
            if (easyLatestTimestamp3 != 0 && TimeUnit.MILLISECONDS.toHours(now.getTime() - easyLatestTimestamp3) > 1) {
                recentLatestDatesWhenMarketAsEasy.add(new Date(easyLatestTimestamp3));
            }

            translations.add(new Translation(
                    res.getInt(res.getColumnIndex(COLUMN_ID)),
                    new ForeignWord(res.getString(res.getColumnIndex(COLUMN_FOREIGN_WORD))),
                    new NativeWord(res.getString(res.getColumnIndex(COLUMN_NATIVE_WORD))),
                    new TranslationMetadata(Difficulty.valueOf(res.getString(res.getColumnIndex(COLUMN_WORD_DIFFICULTY))),
                            recentLatestDatesWhenMarketAsEasy)));
            res.moveToNext();
        }
        res.close();
        return translations;
    }

    @Override
    public Translation getById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query(TRANSLATIONS_TABLE_NAME,
                null, " id = ? ", new String[] {String.valueOf(id)}, null, null, null);
        res.moveToFirst();

        List<Date> recentLatestDatesWhenMarketAsEasy = new ArrayList<>();
        Date now = new Date();
        long easyLatestTimestamp1 = res.getInt(res.getColumnIndex(COLUMN_EASY_LATEST_1));
        long easyLatestTimestamp2 = res.getInt(res.getColumnIndex(COLUMN_EASY_LATEST_2));
        long easyLatestTimestamp3 = res.getInt(res.getColumnIndex(COLUMN_EASY_LATEST_3));
        if (easyLatestTimestamp1 != 0 && TimeUnit.MILLISECONDS.toHours(now.getTime() - easyLatestTimestamp1) > 1) {
            recentLatestDatesWhenMarketAsEasy.add(new Date(easyLatestTimestamp1));
        }
        if (easyLatestTimestamp2 != 0 && TimeUnit.MILLISECONDS.toHours(now.getTime() - easyLatestTimestamp2) > 1) {
            recentLatestDatesWhenMarketAsEasy.add(new Date(easyLatestTimestamp2));
        }
        if (easyLatestTimestamp3 != 0 && TimeUnit.MILLISECONDS.toHours(now.getTime() - easyLatestTimestamp3) > 1) {
            recentLatestDatesWhenMarketAsEasy.add(new Date(easyLatestTimestamp3));
        }

        Translation translation = new Translation(
                    res.getInt(res.getColumnIndex(COLUMN_ID)),
                    new ForeignWord(res.getString(res.getColumnIndex(COLUMN_FOREIGN_WORD))),
                    new NativeWord(res.getString(res.getColumnIndex(COLUMN_NATIVE_WORD))),
                    new TranslationMetadata(Difficulty.valueOf(res.getString(res.getColumnIndex(COLUMN_WORD_DIFFICULTY))),
                            recentLatestDatesWhenMarketAsEasy));
        res.close();
        return translation;
    }
}