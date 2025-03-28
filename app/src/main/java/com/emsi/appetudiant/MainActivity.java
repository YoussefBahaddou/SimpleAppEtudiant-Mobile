package com.emsi.appetudiant;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.service.EtudiantService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText nom, prenom, id;
    private Button add, rechercher, delete, btnViewAllStudents, btnSelectDate, btnSelectImage;
    private TextView res, selectedDate;
    private ImageView imagePreview;

    private EtudiantService es;
    private Etudiant currentEtudiant;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String selectedDateStr;
    private String currentImagePath;

    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        // Save the bitmap to a file and get the path
                        currentImagePath = saveImageToInternalStorage(bitmap);
                        // Display the image
                        imagePreview.setImageBitmap(bitmap);
                        imagePreview.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // Méthode pour vider les champs après l'ajout
    private void clearFields() {
        nom.setText("");
        prenom.setText("");
        selectedDate.setText("");
        selectedDateStr = null;
        currentImagePath = null;
        imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
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

        // Initialize calendar and date format
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

        // Initialisation des vues
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        add = findViewById(R.id.bn);
        id = findViewById(R.id.id);
        rechercher = findViewById(R.id.load);
        res = findViewById(R.id.res);
        delete = findViewById(R.id.delete);
        btnViewAllStudents = findViewById(R.id.btnViewAllStudents);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        selectedDate = findViewById(R.id.selectedDate);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imagePreview = findViewById(R.id.imagePreview);

        // Gestion du bouton de sélection d'image
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Gestion du bouton de sélection de date
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

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
                if (selectedDateStr == null || selectedDateStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Création de l'étudiant avec la date et l'image
                Etudiant etudiant = new Etudiant(nomInput, prenomInput);
                etudiant.setDate(selectedDateStr);
                etudiant.setImagePath(currentImagePath);

                // Insertion de l'étudiant
                es.create(etudiant);
                Toast.makeText(MainActivity.this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
                clearFields();
                clearResult();

                // Affichage de la liste des étudiants dans le Log
                for (Etudiant e : es.findAll()) {
                    Log.d("ETUDIANT", e.getId() + " - " + e.getNom() + " " + e.getPrenom() + " - " + e.getDate() + " - " + e.getImagePath());
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
                        String dateInfo = currentEtudiant.getDate() != null ?
                                "\nDate de naissance : " + currentEtudiant.getDate() : "";
                        res.setText("Nom : " + currentEtudiant.getNom() +
                                "\nPrénom : " + currentEtudiant.getPrenom() + dateInfo);

                        // Display image if available
                        if (currentEtudiant.getImagePath() != null && !currentEtudiant.getImagePath().isEmpty()) {
                            File imgFile = new File(currentEtudiant.getImagePath());
                            if (imgFile.exists()) {
                                imagePreview.setImageURI(Uri.fromFile(imgFile));
                                imagePreview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    } else {
                        res.setText("Aucun étudiant trouvé");
                        currentEtudiant = null;
                        imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
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
                        // Delete the image file if it exists
                        if (etudiantToDelete.getImagePath() != null && !etudiantToDelete.getImagePath().isEmpty()) {
                            File imageFile = new File(etudiantToDelete.getImagePath());
                            if (imageFile.exists()) {
                                imageFile.delete();
                            }
                        }

                        es.delete(etudiantToDelete);
                        Toast.makeText(MainActivity.this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
                        id.setText("");
                        clearResult();
                        imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
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

    // Méthode pour afficher le DatePickerDialog
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateLabel();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Méthode pour mettre à jour le label de date
    private void updateDateLabel() {
        selectedDateStr = dateFormat.format(calendar.getTime());
        selectedDate.setText(selectedDateStr);
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Method to save image to internal storage
    private String saveImageToInternalStorage(Bitmap bitmap) {
        // Create a unique filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";

        // Get the directory for the app's private pictures directory
        File directory = new File(getFilesDir(), "student_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
