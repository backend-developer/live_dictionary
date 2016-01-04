package uk.ignas.langlearn.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TranslationDaoSqlite extends SQLiteOpenHelper implements TranslationDao {

    public static final String DATABASE_NAME = "LiveDictionary.db";
    public static final String TRANSLATIONS_TABLE_NAME = "translations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NATIVE_WORD = "nativeWord";
    public static final String COLUMN_FOREIGN_WORD = "foreignWord";

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
                        "CONSTRAINT uniqueWT UNIQUE (" + COLUMN_NATIVE_WORD + ", " + COLUMN_FOREIGN_WORD + ")" +
                        ")"
        );
        db.execSQL(
                "create table " + AnswersTable.ANSWERS_LOG_TABLE_NAME + " " +
                        "(" +
                        AnswersTable.COLUMN_ID + " integer primary key, " +
                        AnswersTable.COLUMN_TRANSLATION_ID + " integer," +
                        AnswersTable.COLUMN_TIME_ANSWERED + " integer, " +
                        AnswersTable.COLUMN_IS_CORRECT + " integer" +
                        ")"
        );
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
        db.execSQL("DROP TABLE IF EXISTS " + AnswersTable.ANSWERS_LOG_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void insert(List<Translation> translations) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (Translation translation : translations) {
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
        long id = db.insert(TRANSLATIONS_TABLE_NAME, null, contentValues);
        return id != ERROR_OCURRED;
    }

    @Override
    public int update(int id, ForeignWord foreignWord, NativeWord nativeWord) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
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
            deleteAnswersByTranslationIds(collectIds(translations));
            for (Translation translation : translations) {
                this.deleteSingle(translation);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private List<Integer> collectIds(Collection<Translation> translations) {
        List<Integer> translationIdsToDelete = new ArrayList<>();
        for (Translation translation : translations) {
            translationIdsToDelete.add(translation.getId());
        }
        return translationIdsToDelete;
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
        Cursor res = null;
        try {
            res = db.rawQuery("select " +
                    COLUMN_ID + ", " +
                    COLUMN_FOREIGN_WORD + ", " +
                    COLUMN_NATIVE_WORD +
                    " from " + TRANSLATIONS_TABLE_NAME, null);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                translations.add(new Translation(
                        res.getInt(res.getColumnIndex(COLUMN_ID)),
                        new ForeignWord(res.getString(res.getColumnIndex(COLUMN_FOREIGN_WORD))),
                        new NativeWord(res.getString(res.getColumnIndex(COLUMN_NATIVE_WORD))),
                        new TranslationMetadata(
                                new ArrayList<AnswerAtTime>())));
                res.moveToNext();
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return translations;
    }


    public static class AnswersTable {
        public static final String COLUMN_ID = "id";
        public static final String ANSWERS_LOG_TABLE_NAME = "answer_log";
        public static final String COLUMN_TRANSLATION_ID = "translation_id";

        public static final String COLUMN_TIME_ANSWERED = "time_answered";

        public static final String COLUMN_IS_CORRECT = "is_correct";
    }
    @Override
    public boolean logAnswer(Translation translation, Answer answer, Date time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AnswersTable.COLUMN_TRANSLATION_ID, translation.getId());
        contentValues.put(AnswersTable.COLUMN_TIME_ANSWERED, time.getTime());
        contentValues.put(AnswersTable.COLUMN_IS_CORRECT, answer.isCorrect());
        long id = db.insert(AnswersTable.ANSWERS_LOG_TABLE_NAME, null, contentValues);
        return id != ERROR_OCURRED;
    }

    private void deleteAnswersByTranslationIds(List<Integer> translationIdsToDelete) {
        String inClause = Joiner.on(", ").join(translationIdsToDelete);

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AnswersTable.ANSWERS_LOG_TABLE_NAME,
                AnswersTable.COLUMN_TRANSLATION_ID + " IN (?) ",
                new String[]{inClause});
    }

    public ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId() {
        ListMultimap<Integer, AnswerAtTime> answersLogByTranslationId = ArrayListMultimap.create();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("select " +
                    AnswersTable.COLUMN_IS_CORRECT + ", " +
                    AnswersTable.COLUMN_TRANSLATION_ID + ", " +
                    AnswersTable.COLUMN_TIME_ANSWERED +
                    " from " + AnswersTable.ANSWERS_LOG_TABLE_NAME, null);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                int newTranslationId = res.getInt(res.getColumnIndex(AnswersTable.COLUMN_TRANSLATION_ID));
                Answer answer = res.getInt(res.getColumnIndex(AnswersTable.COLUMN_IS_CORRECT)) > 0 ? Answer.CORRECT : Answer.INCORRECT;
                long timeOfAnswer = res.getLong(res.getColumnIndex(AnswersTable.COLUMN_TIME_ANSWERED));
                answersLogByTranslationId.put(newTranslationId, new AnswerAtTime(answer, new Date(timeOfAnswer)));
                res.moveToNext();
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return answersLogByTranslationId;
    }
}
