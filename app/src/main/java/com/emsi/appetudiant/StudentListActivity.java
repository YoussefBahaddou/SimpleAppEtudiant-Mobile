package com.emsi.appetudiant;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.appetudiant.adapter.StudentAdapter;
import com.emsi.appetudiant.classes.Etudiant;
import com.emsi.appetudiant.service.EtudiantService;

import java.util.List;

public class StudentListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewStudents;
    private StudentAdapter adapter;
    private EtudiantService etudiantService;
    private List<Etudiant> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        // Initialize the RecyclerView
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));

        // Get all students from the database
        etudiantService = new EtudiantService(this);
        studentList = etudiantService.findAll();

        // Set up the adapter with context
        adapter = new StudentAdapter(this, studentList);
        recyclerViewStudents.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        refreshStudentList();
    }

    private void refreshStudentList() {
        studentList.clear();
        studentList.addAll(etudiantService.findAll());
        adapter.notifyDataSetChanged();
    }
}
