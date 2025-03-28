package com.emsi.appetudiant;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.service.EtudiantService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EtudiantService es = new EtudiantService(this);

        // Création et insertion de 5 étudiants
        es.create(new Etudiant("ALAMI", "ALI"));
        es.create(new Etudiant("RAMI", "AMAL"));
        es.create(new Etudiant("SAFI", "MHMED"));
        es.create(new Etudiant("SELAOUI", "REDA"));
        es.create(new Etudiant("ALAMI", "WAFA"));

        // Affichage de la liste initiale
        Log.d("Liste", "Avant suppression:");
        for (Etudiant e : es.findAll()) {
            Log.d("ID " + e.getId(), e.getNom() + " " + e.getPrenom());
        }

        // Suppression de l'étudiant avec ID = 3
        Etudiant etudiantToDelete = es.findById(3);
        if (etudiantToDelete != null) {
            es.delete(etudiantToDelete);
            Log.d("Delete", "Étudiant ID 3 supprimé");
        } else {
            Log.d("Delete", "Aucun étudiant trouvé avec ID 3");
        }

        // Affichage de la liste après suppression
        Log.d("Liste", "Après suppression:");
        for (Etudiant e : es.findAll()) {
            Log.d("ID " + e.getId(), e.getNom() + " " + e.getPrenom());
        }
    }
}
