package uk.ignas.livedictionary.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.*;

public class TranslationDao extends SQLiteOpenHelper {

    public static final int ERROR_OCURRED = -1;
    public static final String DATABASE_NAME = "LiveDictionary.db";

    public static class AnswersLog {
        public static final String TABLE_NAME = "answers_log";
        public static final String ID = "id";
        public static final String TRANSLATION_ID = "translation_id";
        public static final String TIME_ANSWERED = "time_answered";
        public static final String IS_CORRECT = "is_correct";
    }
    public static class Translations {
        public static final String TABLE_NAME = "translations";
        public static final String ID = "id";
        public static final String NATIVE_WORD = "nativeWord";
        public static final String FOREIGN_WORD = "foreignWord";
    }
    public static class LabelledTranslation {
        public static final String TABLE_NAME = "labelled_translation";
        public static final String ID = "id";
        public static final String TRANSLATION_ID = "translation_id";
        public static final String UNIQUE_TRANSLATION_INDEX = "LTT83YROS3HVD";
    }
    public TranslationDao(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        prepareDbV1(db);
        prepareDbV2(db);
    }
    private void prepareDbV1(SQLiteDatabase db) {
        db.execSQL(
            "create table " + Translations.TABLE_NAME + " " +
            "(" +
            Translations.ID + " integer primary key, " +
            Translations.NATIVE_WORD + " text NOT NULL," +
            Translations.FOREIGN_WORD + " text NOT NULL, " +
            "CONSTRAINT uniqueWT UNIQUE (" + Translations.NATIVE_WORD + ", " + Translations.FOREIGN_WORD + ")" +
            ")"
                  );
        db.execSQL(
            "create table " + AnswersLog.TABLE_NAME + " " +
            "(" +
            AnswersLog.ID + " integer primary key, " +
            AnswersLog.TRANSLATION_ID + " integer NOT NULL," +
            AnswersLog.TIME_ANSWERED + " integer NOT NULL, " +
            AnswersLog.IS_CORRECT + " integer NOT NULL, " +
            "FOREIGN KEY(" + AnswersLog.TRANSLATION_ID + ") " +
            "REFERENCES " + Translations.TABLE_NAME + "(" + Translations.ID + ")" +
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

    private void prepareDbV2(SQLiteDatabase db) {
        db.execSQL(
            "create table " + LabelledTranslation.TABLE_NAME + " " +
            "(" +
            LabelledTranslation.ID + " integer primary key, " +
            LabelledTranslation.TRANSLATION_ID + " integer NOT NULL," +
            "FOREIGN KEY(" + LabelledTranslation.TRANSLATION_ID + ") " +
            "REFERENCES " + Translations.TABLE_NAME + "(" + Translations.ID + ")," +
            "CONSTRAINT uniqueLT UNIQUE (" + LabelledTranslation.TRANSLATION_ID + ")" +
            ")"
        );
//        db.execSQL("CREATE UNIQUE INDEX " + LabelledTranslation.UNIQUE_TRANSLATION_INDEX +
//                   " ON " + LabelledTranslation.TABLE_NAME + "(" + LabelledTranslation.TRANSLATION_ID + "); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            prepareDbV2(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

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

    public boolean insertSingle(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertSingleUsingDb(translation, db);
    }

    private boolean insertSingleUsingDb(Translation translation, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Translations.NATIVE_WORD, translation.getNativeWord().get());
        contentValues.put(Translations.FOREIGN_WORD, translation.getForeignWord().get());
        long id = db.insert(Translations.TABLE_NAME, null, contentValues);
        return id != ERROR_OCURRED;
    }

    public void addLabel(Translation translation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LabelledTranslation.TRANSLATION_ID, translation.getId());
        this.getWritableDatabase().execSQL("insert into " +
                                           LabelledTranslation.TABLE_NAME + " (" + LabelledTranslation.TRANSLATION_ID + ") "
                                           + "VALUES (" + translation.getId() + ")");
    }

    public int update(int id, ForeignWord foreignWord, NativeWord nativeWord) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(Translations.NATIVE_WORD, nativeWord.get());
            contentValues.put(Translations.FOREIGN_WORD, foreignWord.get());
            return db.update(Translations.TABLE_NAME, contentValues,
                    Translations.ID + " = ? ",
                    new String[]{String.valueOf(id)});

        } catch (SQLiteConstraintException e) {
            deleteById(id);
            return 1;
        }
    }

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
        return db.delete(Translations.TABLE_NAME,
                Translations.ID + " = ? ",
                new String[]{String.valueOf(id)});
    }

    private Integer deleteSingle(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Translations.TABLE_NAME,
                Translations.NATIVE_WORD + " = ? AND " + Translations.FOREIGN_WORD + " = ? ",
                new String[]{translation.getNativeWord().get(), translation.getForeignWord().get()});
    }

    public Collection<Integer> getTranslationWithLabelIds() {
        List<Integer> translationIds = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("select " +
                              LabelledTranslation.TRANSLATION_ID + " " +
                              " from " + LabelledTranslation.TABLE_NAME, null);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                translationIds.add(res.getInt(res.getColumnIndex(LabelledTranslation.TRANSLATION_ID)));
                res.moveToNext();
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return translationIds;
    }

    public Collection<Translation> getTranslationsByIds(Collection<Integer> translationIds) {
        List<Translation> translations = getAllTranslations();
        for (Iterator<Translation> iter = translations.iterator(); iter.hasNext(); ) {
            if (!translationIds.contains(iter.next().getId())) {
                iter.remove();
            }
        }
        return translations;
    }

    public List<Translation> getAllTranslations() {
        List<Translation> translations = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("select " +
                    Translations.ID + ", " +
                    Translations.FOREIGN_WORD + ", " +
                    Translations.NATIVE_WORD +
                    " from " + Translations.TABLE_NAME, null);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                translations.add(new Translation(
                        res.getInt(res.getColumnIndex(Translations.ID)),
                        new ForeignWord(res.getString(res.getColumnIndex(Translations.FOREIGN_WORD))),
                        new NativeWord(res.getString(res.getColumnIndex(Translations.NATIVE_WORD))),
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

    public boolean logAnswer(Translation translation, Answer answer, Date time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AnswersLog.TRANSLATION_ID, translation.getId());
        contentValues.put(AnswersLog.TIME_ANSWERED, time.getTime());
        contentValues.put(AnswersLog.IS_CORRECT, answer.isCorrect());
        long id = db.insert(AnswersLog.TABLE_NAME, null, contentValues);
        ListMultimap<Integer, AnswerAtTime> answersLogByTranslationId = getAnswersLogByTranslationId();
        return id != ERROR_OCURRED;
    }

    private void deleteAnswersByTranslationIds(List<Integer> translationIdsToDelete) {
        String inClause = Joiner.on(", ").join(translationIdsToDelete);

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + AnswersLog.TABLE_NAME + " WHERE " +
                AnswersLog.TRANSLATION_ID + " IN (" + inClause + ") ");
    }

    public ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId() {
        ListMultimap<Integer, AnswerAtTime> answersLogByTranslationId = ArrayListMultimap.create();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("select " +
                    AnswersLog.IS_CORRECT + ", " +
                    AnswersLog.TRANSLATION_ID + ", " +
                    AnswersLog.TIME_ANSWERED +
                    " from " + AnswersLog.TABLE_NAME, null);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                int newTranslationId = res.getInt(res.getColumnIndex(AnswersLog.TRANSLATION_ID));
                Answer answer = res.getInt(res.getColumnIndex(AnswersLog.IS_CORRECT)) > 0 ? Answer.CORRECT : Answer.INCORRECT;
                long timeOfAnswer = res.getLong(res.getColumnIndex(AnswersLog.TIME_ANSWERED));
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
