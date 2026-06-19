package com.example.rightway_out

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private var students: MutableList<Student>,
    private val onEdit: (Student) -> Unit,
    private val onDelete: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvAdmission: TextView = itemView.findViewById(R.id.tvAdmissionNumber)
        val tvForm: TextView = itemView.findViewById(R.id.tvForm)
        val tvStatus: TextView = itemView.findViewById(R.id.tvClearanceStatus)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val tvInitials: TextView = itemView.findViewById(R.id.tvInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.tvName.text = student.name

        holder.tvAdmission.text = student.admissionNumber
        holder.tvForm.text = student.form
        // Set initials avatar
        val initials = student.name.split(" ")
            .take(2)
            .joinToString("") { it.first().uppercase() }
        holder.tvInitials.text = initials

        val cleared = mutableListOf<String>()
        val pending = mutableListOf<String>()
        if (student.libraryCleared) cleared.add("Library") else pending.add("Library")
        if (student.accountsCleared) cleared.add("Accounts") else pending.add("Accounts")
        if (student.sportsCleared) cleared.add("Sports") else pending.add("Sports")

        holder.tvStatus.text = if (student.isFullyCleared()) {
            "✓ Fully Cleared"
        } else {
            "Pending: ${pending.joinToString(", ")}"
        }

        holder.tvStatus.setTextColor(
            if (student.isFullyCleared())
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
            else
                holder.itemView.context.getColor(android.R.color.holo_orange_dark)
        )

        holder.btnEdit.setOnClickListener { onEdit(student) }
        holder.btnDelete.setOnClickListener { onDelete(student) }
    }

    override fun getItemCount() = students.size

    fun updateList(newList: MutableList<Student>) {
        students = newList
        notifyDataSetChanged()
    }
}