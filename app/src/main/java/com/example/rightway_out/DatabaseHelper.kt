package com.example.rightway_out

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "rightway_out.db"
        const val DATABASE_VERSION = 1
        const val TABLE_STUDENTS = "students"
        const val COL_ID = "id"
        const val COL_ADMISSION = "admission_number"
        const val COL_NAME = "name"
        const val COL_FORM = "form"
        const val COL_LIBRARY = "library_cleared"
        const val COL_ACCOUNTS = "accounts_cleared"
        const val COL_SPORTS = "sports_cleared"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_STUDENTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ADMISSION TEXT NOT NULL,
                $COL_NAME TEXT NOT NULL,
                $COL_FORM TEXT NOT NULL,
                $COL_LIBRARY INTEGER DEFAULT 0,
                $COL_ACCOUNTS INTEGER DEFAULT 0,
                $COL_SPORTS INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }

    // CREATE
    fun addStudent(student: Student): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ADMISSION, student.admissionNumber)
            put(COL_NAME, student.name)
            put(COL_FORM, student.form)
            put(COL_LIBRARY, if (student.libraryCleared) 1 else 0)
            put(COL_ACCOUNTS, if (student.accountsCleared) 1 else 0)
            put(COL_SPORTS, if (student.sportsCleared) 1 else 0)
        }
        return db.insert(TABLE_STUDENTS, null, values)
    }

    // READ ALL
    fun getAllStudents(): List<Student> {
        val students = mutableListOf<Student>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS", null)
        if (cursor.moveToFirst()) {
            do {
                students.add(
                    Student(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        admissionNumber = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADMISSION)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        form = cursor.getString(cursor.getColumnIndexOrThrow(COL_FORM)),
                        libraryCleared = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIBRARY)) == 1,
                        accountsCleared = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ACCOUNTS)) == 1,
                        sportsCleared = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SPORTS)) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return students
    }

    // UPDATE
    fun updateStudent(student: Student): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ADMISSION, student.admissionNumber)
            put(COL_NAME, student.name)
            put(COL_FORM, student.form)
            put(COL_LIBRARY, if (student.libraryCleared) 1 else 0)
            put(COL_ACCOUNTS, if (student.accountsCleared) 1 else 0)
            put(COL_SPORTS, if (student.sportsCleared) 1 else 0)
        }
        return db.update(TABLE_STUDENTS, values, "$COL_ID=?", arrayOf(student.id.toString()))
    }

    // DELETE
    fun deleteStudent(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_STUDENTS, "$COL_ID=?", arrayOf(id.toString()))
    }
}