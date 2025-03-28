package com.emsi.appetudiant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.appetudiant.adapter.StudentAdapter;
import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.service.EtudiantService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StudentListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewStudents;
    private StudentAdapter adapter;
    private EtudiantService etudiantService;
    private int currentEditPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Liste des Étudiants");
        }

        // Initialize service
        etudiantService = new EtudiantService(this);

        // Initialize RecyclerView
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));

        // Load students and set adapter
        loadStudents();
    }

    private void loadStudents() {
        List<Etudiant> studentList = etudiantService.findAll();
        adapter = new StudentAdapter(this, studentList);
        recyclerViewStudents.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadStudents();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The requestCode is the position of the student in the list
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                // Save the bitmap to a file and get the path
                String imagePath = saveImageToInternalStorage(bitmap);
                // Update the student's image path
                adapter.updateImagePath(requestCode, imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
