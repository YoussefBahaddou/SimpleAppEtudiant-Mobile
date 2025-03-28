package com.emsi.appetudiant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.service.EtudiantService;

public class MainActivity extends AppCompatActivity {

    private EditText nom, prenom, id;
    private Button add, rechercher, delete, btnViewAllStudents;
    private TextView res;

    private EtudiantService es;
    private Etudiant currentEtudiant;

    // Méthode pour vider les champs après l'ajout
    private void clearFields() {
        nom.setText("");
        prenom.setText("");
    }

    // Méthode pour vider le résultat et réinitialiser l'étudiant courant
    private void clearResult() {
        res.setText("");
        currentEtudiant = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        es = new EtudiantService(this);

        // Initialisation des vues
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        add = findViewById(R.id.bn);
        id = findViewById(R.id.id);
        rechercher = findViewById(R.id.load);
        res = findViewById(R.id.res);
        delete = findViewById(R.id.delete);
        btnViewAllStudents = findViewById(R.id.btnViewAllStudents);

        // Gestion du bouton d'ajout
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomInput = nom.getText().toString().trim();
                String prenomInput = prenom.getText().toString().trim();

                if (nomInput.isEmpty()) {
                    nom.setError("Veuillez entrer un nom");
                    return;
                }
                if (prenomInput.isEmpty()) {
                    prenom.setError("Veuillez entrer un prénom");
                    return;
                }

                // Insertion de l'étudiant
                es.create(new Etudiant(nomInput, prenomInput));
                Toast.makeText(MainActivity.this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
                clearFields();
                clearResult();

                // Affichage de la liste des étudiants dans le Log
                for (Etudiant e : es.findAll()) {
                    Log.d("ETUDIANT", e.getId() + " - " + e.getNom() + " " + e.getPrenom());
                }
            }
        });

        // Gestion du bouton de recherche
        rechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idInput = id.getText().toString().trim();

                if (idInput.isEmpty()) {
                    id.setError("Veuillez entrer un ID");
                    return;
                }

                try {
                    int idEtudiant = Integer.parseInt(idInput);
                    currentEtudiant = es.findById(idEtudiant);

                    if (currentEtudiant != null) {
                        res.setText("Nom : " + currentEtudiant.getNom() + "\nPrénom : " + currentEtudiant.getPrenom());
                    } else {
                        res.setText("Aucun étudiant trouvé");
                        currentEtudiant = null;
                    }
                } catch (NumberFormatException ex) {
                    id.setError("ID invalide");
                }
            }
        });

        // Gestion du bouton de suppression
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idInput = id.getText().toString().trim();

                if (idInput.isEmpty()) {
                    id.setError("Veuillez entrer un ID");
                    return;
                }

                try {
                    int idEtudiant = Integer.parseInt(idInput);
                    Etudiant etudiantToDelete = es.findById(idEtudiant);

                    if (etudiantToDelete != null) {
                        es.delete(etudiantToDelete);
                        Toast.makeText(MainActivity.this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
                        id.setText("");
                        clearResult();
                    } else {
                        Toast.makeText(MainActivity.this, "Aucun étudiant trouvé avec cet ID", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException ex) {
                    id.setError("ID invalide");
                }
            }
        });

        // Gestion du bouton pour voir tous les étudiants
        btnViewAllStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StudentListActivity.class);
                startActivity(intent);
            }
        });
    }
}
