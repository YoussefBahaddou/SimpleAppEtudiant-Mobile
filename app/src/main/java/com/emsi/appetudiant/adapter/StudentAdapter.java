package com.emsi.appetudiant.adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.appetudiant.R;
import com.emsi.appetudiant.StudentListActivity;
import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.service.EtudiantService;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Etudiant> studentList;
    private Context context;
    private EtudiantService etudiantService;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    public StudentAdapter(Context context, List<Etudiant> studentList) {
        this.context = context;
        this.studentList = studentList;
        this.etudiantService = new EtudiantService(context);
        this.calendar = Calendar.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Etudiant student = studentList.get(position);
        holder.textViewStudentId.setText("ID: " + student.getId());
        holder.textViewStudentName.setText("Nom: " + student.getNom());
        holder.textViewStudentFirstName.setText("Prénom: " + student.getPrenom());

        // Handle date display
        if (student.getDate() != null && !student.getDate().isEmpty()) {
            holder.textViewStudentDate.setText("Date de naissance: " + student.getDate());
            holder.textViewStudentDate.setVisibility(View.VISIBLE);
        } else {
            holder.textViewStudentDate.setVisibility(View.GONE);
        }

        // Handle image display
        if (student.getImagePath() != null && !student.getImagePath().isEmpty()) {
            File imgFile = new File(student.getImagePath());
            if (imgFile.exists()) {
                holder.imageViewStudent.setImageURI(Uri.fromFile(imgFile));
            } else {
                holder.imageViewStudent.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imageViewStudent.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Handle click to show options dialog
        holder.itemView.setOnClickListener(v -> showOptionsDialog(student, position));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // Method to update image path for a student
    public void updateImagePath(int position, String imagePath) {
        if (position >= 0 && position < studentList.size()) {
            Etudiant student = studentList.get(position);
            student.setImagePath(imagePath);
            etudiantService.update(student);
            notifyItemChanged(position);
        }
    }

    // Method to show options dialog (modify or delete)
    private void showOptionsDialog(Etudiant student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Options pour l'étudiant");

        // Set up the options
        String[] options = {"Modifier", "Supprimer"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Modify
                    showEditDialog(student, position);
                    break;
                case 1: // Delete
                    showDeleteDialog(student, position);
                    break;
            }
        });

        // Add cancel button
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        builder.create().show();
    }

    // Method to show edit dialog
    private void showEditDialog(Etudiant student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modifier l'étudiant");

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_student, null);
        builder.setView(dialogView);

        // Get references to the EditText fields
        EditText editTextNom = dialogView.findViewById(R.id.editTextNom);
        EditText editTextPrenom = dialogView.findViewById(R.id.editTextPrenom);
        TextView textViewDate = dialogView.findViewById(R.id.textViewDate);
        Button btnChangeDate = dialogView.findViewById(R.id.btnChangeDate);
        ImageView imageViewPreview = dialogView.findViewById(R.id.imageViewPreview);
        Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Set current values
        editTextNom.setText(student.getNom());
        editTextPrenom.setText(student.getPrenom());

        // Set date if available
        if (student.getDate() != null && !student.getDate().isEmpty()) {
            textViewDate.setText(student.getDate());
        } else {
            textViewDate.setText("Aucune date sélectionnée");
        }

        // Set image if available
        if (student.getImagePath() != null && !student.getImagePath().isEmpty()) {
            File imgFile = new File(student.getImagePath());
            if (imgFile.exists()) {
                imageViewPreview.setImageURI(Uri.fromFile(imgFile));
            }
        }

        // Handle date change button
        btnChangeDate.setOnClickListener(v -> {
            showDatePickerDialog(textViewDate);
        });

        // Handle image change button
        btnChangeImage.setOnClickListener(v -> {
            // Launch image picker
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            ((StudentListActivity) context).startActivityForResult(intent, position);
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle save button click
        btnSave.setOnClickListener(v -> {
            String newNom = editTextNom.getText().toString().trim();
            String newPrenom = editTextPrenom.getText().toString().trim();
            String newDate = textViewDate.getText().toString().trim();

            if (newNom.isEmpty() || newPrenom.isEmpty()) {
                Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update student data
            student.setNom(newNom);
            student.setPrenom(newPrenom);

            // Only set date if it's not the default text
            if (!newDate.equals("Aucune date sélectionnée")) {
                student.setDate(newDate);
            }

            etudiantService.update(student);

            // Update the RecyclerView
            notifyItemChanged(position);

            Toast.makeText(context, "Étudiant modifié avec succès", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    // Method to show date picker dialog
    private void showDatePickerDialog(TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String selectedDate = dateFormat.format(calendar.getTime());
                        textView.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Method to show delete confirmation dialog
    private void showDeleteDialog(Etudiant student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Supprimer l'étudiant");
        builder.setMessage("Voulez-vous vraiment supprimer cet étudiant ?");

        // Add Yes button
        builder.setPositiveButton("Oui", (dialog, which) -> {
            // Delete the image file if it exists
            if (student.getImagePath() != null && !student.getImagePath().isEmpty()) {
                File imageFile = new File(student.getImagePath());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            // Delete the student
            etudiantService.delete(student);

            // Remove from the list and update RecyclerView
            studentList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, studentList.size());

            Toast.makeText(context, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
        });

        // Add No button
        builder.setNegativeButton("Non", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        builder.create().show();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStudentId;
        TextView textViewStudentName;
        TextView textViewStudentFirstName;
        TextView textViewStudentDate;
        ShapeableImageView imageViewStudent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStudentId = itemView.findViewById(R.id.textViewStudentId);
            textViewStudentName = itemView.findViewById(R.id.textViewStudentName);
            textViewStudentFirstName = itemView.findViewById(R.id.textViewStudentFirstName);
            textViewStudentDate = itemView.findViewById(R.id.textViewStudentDate);
            imageViewStudent = itemView.findViewById(R.id.imageViewStudent);
        }
    }
}
