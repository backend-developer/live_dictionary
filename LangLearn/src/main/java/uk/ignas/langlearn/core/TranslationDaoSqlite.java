package uk.ignas.langlearn.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.*;

public class TranslationDaoSqlite extends SQLiteOpenHelper implements TranslationDao {

    public static final String DATABASE_NAME = "MyDBName3.db";
    public static final String TRANSLATIONS_TABLE_NAME = "translations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NATIVE_WORD = "nativeWord";
    public static final String COLUMN_FOREIGN_WORD = "foreignWord";
    public static final String COLUMN_TRANSLATION_DIFFICULTY = "difficulty";

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
                        COLUMN_TRANSLATION_DIFFICULTY + " text, " +
                        "CONSTRAINT uniqueWT UNIQUE (" + COLUMN_NATIVE_WORD + ", " + COLUMN_FOREIGN_WORD + ")" +
                        ")"
        );
        onCreateAnswers(db);
        insertSeedData(db);
    }

    private void insertSeedData(SQLiteDatabase db) {
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
        onUpgradeAnswers(db, oldVersion, newVersion);
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
        contentValues.put(COLUMN_TRANSLATION_DIFFICULTY, Difficulty.EASY.name());
        long id = db.insert(TRANSLATIONS_TABLE_NAME, null, contentValues);
        return id != ERROR_OCURRED;
    }

    @Override
    public int update(int id, ForeignWord foreignWord, NativeWord nativeWord) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_TRANSLATION_DIFFICULTY, Difficulty.EASY.name());
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
        String thisTranslationId = "this_translation_id";
        Cursor res = null;
        try {
            res = db.rawQuery("select " +
                    TRANSLATIONS_TABLE_NAME + "." + COLUMN_ID + " as " + thisTranslationId + ", " +
                    COLUMN_FOREIGN_WORD + ", " +
                    COLUMN_NATIVE_WORD + ", " +
                    COLUMN_TIME_ANSWERED + ", " +
                    COLUMN_TRANSLATION_DIFFICULTY
                    + " from " + TRANSLATIONS_TABLE_NAME + " LEFT JOIN " + ANSWERS_LOG_TABLE_NAME + " ON " + TRANSLATIONS_TABLE_NAME + "." + COLUMN_ID + " = " + ANSWERS_LOG_TABLE_NAME + "." + COLUMN_TRANSLATION_ID + " ORDER BY " + thisTranslationId, null);
            res.moveToFirst();
            Translation currentTranslation = null;
            int lastTranslation = -1;
            while (!res.isAfterLast()) {
                int newTranslationId = res.getInt(res.getColumnIndex(thisTranslationId));
                Difficulty difficulty = Difficulty.valueOf(res.getString(res.getColumnIndex(COLUMN_TRANSLATION_DIFFICULTY)));
                if (newTranslationId != lastTranslation) {
                    currentTranslation = new Translation(
                            newTranslationId,
                            new ForeignWord(res.getString(res.getColumnIndex(COLUMN_FOREIGN_WORD))),
                            new NativeWord(res.getString(res.getColumnIndex(COLUMN_NATIVE_WORD))),
                            new TranslationMetadata(difficulty,
                                    new ArrayList<DifficultyAtTime>()));
                    translations.add(currentTranslation);
                    lastTranslation = newTranslationId;
                }

                long timeOfAnswer = res.getLong(res.getColumnIndex(COLUMN_TIME_ANSWERED));

                currentTranslation.getMetadata().getRecentDifficulty().add(new DifficultyAtTime(
                        difficulty, new Date(timeOfAnswer)
                ));

                res.moveToNext();
            }
        } finally {
             if (res != null) {
                 res.close();
             }
        }
        return translations;
    }


    public static final String ANSWERS_LOG_TABLE_NAME = "answer_log";

    public static final String COLUMN_TRANSLATION_ID = "translation_id";
    public static final String COLUMN_TIME_ANSWERED = "time_answered";
    public static final String COLUMN_IS_CORRECT = "is_correct";

    public void onCreateAnswers(SQLiteDatabase db) {
        db.execSQL(
                "create table " + ANSWERS_LOG_TABLE_NAME + " " +
                        "(" +
                        COLUMN_ID + " integer primary key, " +
                        COLUMN_TRANSLATION_ID + " integer," +
                        COLUMN_TIME_ANSWERED + " integer, " +
                        COLUMN_IS_CORRECT + " integer" +
                        ")"
        );
    }


    public void onUpgradeAnswers(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ANSWERS_LOG_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public boolean logAnswer(Translation translation, Difficulty difficulty, Date time) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (difficulty == Difficulty.EASY) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_TRANSLATION_ID, translation.getId());
            contentValues.put(COLUMN_TIME_ANSWERED, time.getTime());
            contentValues.put(COLUMN_IS_CORRECT, true);
            long id = db.insert(ANSWERS_LOG_TABLE_NAME, null, contentValues);
            return id != ERROR_OCURRED;
        } else {
            db.execSQL("DELETE FROM " + ANSWERS_LOG_TABLE_NAME + " WHERE translation_id = " + translation.getId());
            return true;
        }
    }

    public List<DifficultyAtTime> getAnswersForTranslation(int translationId) {
        List<DifficultyAtTime> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.query(ANSWERS_LOG_TABLE_NAME, new String[]{COLUMN_TIME_ANSWERED, COLUMN_IS_CORRECT}, COLUMN_TRANSLATION_ID + " = ? ", new String[]{String.valueOf(translationId)}, null, null, null);
            res.moveToFirst();

            List<DifficultyAtTime> recentLatestDatesWhenMarketAsEasy = new ArrayList<>();
            while (!res.isAfterLast()) {

                long timeOfAnswer = res.getInt(res.getColumnIndex(COLUMN_TIME_ANSWERED));
                boolean isCorrectlyAnswered = res.getInt(res.getColumnIndex(COLUMN_IS_CORRECT)) > 0;
                Difficulty difficulty = isCorrectlyAnswered ? Difficulty.EASY: Difficulty.DIFFICULT;
                if (timeOfAnswer != 0) {
                    recentLatestDatesWhenMarketAsEasy.add(new DifficultyAtTime(difficulty, new Date(timeOfAnswer)));
                }
                res.moveToNext();
            }
            dates = recentLatestDatesWhenMarketAsEasy;
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return dates;
    }
}