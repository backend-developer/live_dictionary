package uk.ignas.livedictionary.core.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import uk.ignas.livedictionary.core.util.Transactable;

public class Dao extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "LiveDictionary.db";

    public Dao(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        prepareDbV1(db);
        prepareDbV2(db);
        prepareDbV3(db);
    }

    private void prepareDbV1(SQLiteDatabase db) {
        db.execSQL("create table " + "translations" + " " +
                   "(" +
                   "id" + " integer primary key, " +
                   "nativeWord" + " text NOT NULL," +
                   "foreignWord" + " text NOT NULL, " +
                   "CONSTRAINT uniqueWT UNIQUE (" + "nativeWord" + ", " + "foreignWord" + ")" +
                   ")");
        db.execSQL("create table " + "answers_log" + " " +
                   "(" +
                   "id" + " integer primary key, " +
                   "translation_id" + " integer NOT NULL," +
                   "time_answered" + " integer NOT NULL, " +
                   "is_correct" + " integer NOT NULL, " +
                   "FOREIGN KEY(" + "translation_id" + ") " +
                   "REFERENCES " + "translations" + "(" + "id" + ")" +
                   ")");
        insertSeedData(db);
    }

    private void insertSeedData(SQLiteDatabase db) {
        insertSingleV1(db, "purple", "morado");
        insertSingleV1(db, "green", "verde");
        insertSingleV1(db, "pink", "rosa");
        insertSingleV1(db, "red", "rojo");
        insertSingleV1(db, "silver", "plateado");
        insertSingleV1(db, "black", "negro");
        insertSingleV1(db, "orange", "naranja");
        insertSingleV1(db, "brown", "marrón");
        insertSingleV1(db, "grey", "gris");
        insertSingleV1(db, "fuchsia", "fucsia");
        insertSingleV1(db, "gold", "dorado");
        insertSingleV1(db, "white", "blanco");
        insertSingleV1(db, "beige", "beige");
        insertSingleV1(db, "navy blue", "azul marino");
        insertSingleV1(db, "blue", "azul");
        insertSingleV1(db, "yellow", "amarillo");
        insertSingleV1(db, "colors", "Los coroles");
    }

    private long insertSingleV1(SQLiteDatabase db, String nativeWord, String foreignWord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("nativeWord", nativeWord);
        contentValues.put("foreignWord", foreignWord);
        return db.insert("translations", null, contentValues);
    }

    private void prepareDbV2(SQLiteDatabase db) {
        db.execSQL("create table " + "labelled_translation" + " " +
                   "(" +
                   "id" + " integer primary key, " +
                   "translation_id" + " integer NOT NULL," +
                   "FOREIGN KEY(" + "translation_id" + ") " +
                   "REFERENCES " + "translations" + "(" + "id" + ")," +
                   "CONSTRAINT uniqueLT UNIQUE (" + "translation_id" + ")" +
                   ")");
    }

    private void prepareDbV3(SQLiteDatabase db) {
        db.execSQL("DROP TABLE labelled_translation");
        db.execSQL("create table label (id integer primary key, translation_id text NOT NULL)");

        insertLabelV3(db, "RED");
        insertLabelV3(db, "BLACK");
        insertLabelV3(db, "YELLOW");
        insertLabelV3(db, "GREEN");

        db.execSQL("create table labelled_translation (id integer primary key, translation_id integer NOT NULL,"
                   + "label_id integer NOT NULL," + "FOREIGN KEY(translation_id) REFERENCES translations(id),"
                   + "FOREIGN KEY(label_id) REFERENCES label(id),"
                   + "CONSTRAINT uniqueLTLT UNIQUE (translation_id, label_id))");
    }

    private void insertLabelV3(SQLiteDatabase db, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("translation_id", value);
        db.insert("label", null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            prepareDbV2(db);
        }
        if (oldVersion < 3) {
            prepareDbV3(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public long insert(String tableName, ContentValues contentValues) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(tableName, null, contentValues);
    }

    public <T> T doInTransaction(Transactable<T> transactable) {
        T result = null;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            result = transactable.perform();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    public void execSql(String sql) {
        this.getWritableDatabase().execSQL(sql);
    }

    public int update(String tableName, ContentValues contentValues, String condition, String[] params) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(tableName, contentValues, condition, params);
    }

    public Integer delete(String tableName, String condition, String[] args) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName, condition, args);
    }


    public Cursor rawQuery(String sql) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(sql, null);
    }
}
