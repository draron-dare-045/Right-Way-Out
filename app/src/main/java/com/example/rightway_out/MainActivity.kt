package com.example.rightway_out

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: StudentAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddStudent)

        adapter = StudentAdapter(
            dbHelper.getAllStudents().toMutableList(),
            onEdit = { student ->
                val intent = Intent(this, AddEditStudentActivity::class.java).apply {
                    putExtra("STUDENT_ID", student.id)
                    putExtra("STUDENT_NAME", student.name)
                    putExtra("STUDENT_ADMISSION", student.admissionNumber)
                    putExtra("STUDENT_FORM", student.form)
                    putExtra("LIBRARY_CLEARED", student.libraryCleared)
                    putExtra("ACCOUNTS_CLEARED", student.accountsCleared)
                    putExtra("SPORTS_CLEARED", student.sportsCleared)
                }
                startActivity(intent)
            },
            onDelete = { student ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Student")
                    .setMessage("Are you sure you want to delete ${student.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        dbHelper.deleteStudent(student.id)
                        refreshList()
                        Toast.makeText(this, "Student deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(this, AddEditStudentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        adapter.updateList(dbHelper.getAllStudents().toMutableList())
    }
}