package com.emsi.appetudiant.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.util.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class EtudiantService {

    private static final String TABLE_NAME = "etudiant";
    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE = "date";
    private static final String KEY_IMAGE_PATH = "image_path";

    private static final String[] COLUMNS = {KEY_ID, KEY_NOM, KEY_PRENOM, KEY_DATE, KEY_IMAGE_PATH};

    private MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    public void create(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE, e.getDate());
        values.put(KEY_IMAGE_PATH, e.getImagePath());

        long id = db.insert(TABLE_NAME, null, values);
        e.setId((int) id); // Assigner l'ID généré
        Log.d("insert", "Ajouté: " + e.getNom() + " ID: " + id);
        db.close();
    }

    public void update(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE, e.getDate());
        values.put(KEY_IMAGE_PATH, e.getImagePath());

        db.update(TABLE_NAME, values, KEY_ID + " = ?", new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public Etudiant findById(int id) {
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, COLUMNS, KEY_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        Etudiant e = null;
        if (c.moveToFirst()) {
            e = new Etudiant();
            e.setId(c.getInt(0));
            e.setNom(c.getString(1));
            e.setPrenom(c.getString(2));

            // Handle date column
            if (c.getColumnCount() > 3) {
                e.setDate(c.getString(3));
            }

            // Handle image path column
            if (c.getColumnCount() > 4) {
                e.setImagePath(c.getString(4));
            }
        }
        c.close();
        db.close();
        return e;
    }

    public void delete(Etudiant e) {
        if (e != null) {
            SQLiteDatabase db = this.helper.getWritableDatabase();
            db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{String.valueOf(e.getId())});
            db.close();
        }
    }

    public List<Etudiant> findAll() {
        List<Etudiant> etudiants = new ArrayList<>();
        String req = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = db.rawQuery(req, null);

        if (c.moveToFirst()) {
            do {
                Etudiant e = new Etudiant();
                e.setId(c.getInt(0));
                e.setNom(c.getString(1));
                e.setPrenom(c.getString(2));

                // Handle date column
                if (c.getColumnCount() > 3) {
                    e.setDate(c.getString(3));
                }

                // Handle image path column
                if (c.getColumnCount() > 4) {
                    e.setImagePath(c.getString(4));
                }

                etudiants.add(e);
                Log.d("Liste", "ID: " + e.getId() + " Nom: " + e.getNom());
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return etudiants;
    }
}
