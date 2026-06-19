package com.example.rightway_out

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddEditStudentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var studentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_student)

        dbHelper = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etAdmission = findViewById<EditText>(R.id.etAdmissionNumber)
        val spinnerForm = findViewById<Spinner>(R.id.spinnerForm)
        val cbLibrary = findViewById<CheckBox>(R.id.cbLibrary)
        val cbAccounts = findViewById<CheckBox>(R.id.cbAccounts)
        val cbSports = findViewById<CheckBox>(R.id.cbSports)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val forms = arrayOf("Form 1", "Form 2", "Form 3", "Form 4")
        spinnerForm.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, forms)

        // If editing, load existing data
        studentId = intent.getIntExtra("STUDENT_ID", -1)
        if (studentId != -1) {
            etName.setText(intent.getStringExtra("STUDENT_NAME"))
            etAdmission.setText(intent.getStringExtra("STUDENT_ADMISSION"))
            val formIndex = forms.indexOf(intent.getStringExtra("STUDENT_FORM"))
            if (formIndex >= 0) spinnerForm.setSelection(formIndex)
            cbLibrary.isChecked = intent.getBooleanExtra("LIBRARY_CLEARED", false)
            cbAccounts.isChecked = intent.getBooleanExtra("ACCOUNTS_CLEARED", false)
            cbSports.isChecked = intent.getBooleanExtra("SPORTS_CLEARED", false)
            title = "Edit Student"
        } else {
            title = "Add Student"
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val admission = etAdmission.text.toString().trim()
            val form = spinnerForm.selectedItem.toString()

            if (name.isEmpty() || admission.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val student = Student(
                id = if (studentId != -1) studentId else 0,
                name = name,
                admissionNumber = admission,
                form = form,
                libraryCleared = cbLibrary.isChecked,
                accountsCleared = cbAccounts.isChecked,
                sportsCleared = cbSports.isChecked
            )

            if (studentId != -1) {
                dbHelper.updateStudent(student)
                Toast.makeText(this, "Student updated!", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addStudent(student)
                Toast.makeText(this, "Student added!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}