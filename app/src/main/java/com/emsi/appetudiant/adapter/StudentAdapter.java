package com.emsi.appetudiant.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.appetudiant.R;
import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.service.EtudiantService;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Etudiant> studentList;
    private Context context;
    private EtudiantService etudiantService;

    public StudentAdapter(Context context, List<Etudiant> studentList) {
        this.context = context;
        this.studentList = studentList;
        this.etudiantService = new EtudiantService(context);
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

        // Handle click for edit
        holder.itemView.setOnClickListener(v -> showEditDialog(student, position));

        // Handle long click for delete
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(student, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
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
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Set current values
        editTextNom.setText(student.getNom());
        editTextPrenom.setText(student.getPrenom());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle save button click
        btnSave.setOnClickListener(v -> {
            String newNom = editTextNom.getText().toString().trim();
            String newPrenom = editTextPrenom.getText().toString().trim();

            if (newNom.isEmpty() || newPrenom.isEmpty()) {
                Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update student data
            student.setNom(newNom);
            student.setPrenom(newPrenom);
            etudiantService.update(student);

            // Update the RecyclerView
            notifyItemChanged(position);

            Toast.makeText(context, "Étudiant modifié avec succès", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    // Method to show delete confirmation dialog
    private void showDeleteDialog(Etudiant student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Supprimer l'étudiant");
        builder.setMessage("Voulez-vous vraiment supprimer cet étudiant ?");

        // Add Yes button
        builder.setPositiveButton("Oui", (dialog, which) -> {
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

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStudentId = itemView.findViewById(R.id.textViewStudentId);
            textViewStudentName = itemView.findViewById(R.id.textViewStudentName);
            textViewStudentFirstName = itemView.findViewById(R.id.textViewStudentFirstName);
        }
    }
}
