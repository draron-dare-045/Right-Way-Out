# RightWay Out

A Jetpack Compose Android app for managing student clearance at Kapsabet High School — students track their clearance status across departments, and admins manage students, update clearance, and communicate directly through in-app messaging.

## ✨ Features

### For Students
- 📋 **Dashboard** — live clearance status across Library, Boarding, Sports, and Finance departments
- 👤 **Profile** — view personal clearance details (read-only)
- 💬 **Messaging** — direct chat with school admin/support
- 🛒 **Shopping List** — manage a personal shopping list
- 🌗 Light/dark theme support

### For Admins
- 📊 **Dashboard** — live stats (total, cleared, flagged, pending) with a visual clearance progress bar
- 🔍 Search and filter students by name, admission number, or clearance status
- ✏️ Update a student's clearance status and leave comments per department
- ➕ Add new students
- 💬 **Messaging** — unified inbox of all student conversations, with real-time chat per student
- Bottom navigation between Dashboard and Messages

### Shared
- 🔐 Firebase Authentication with role-based routing (`STUDENT` vs `ADMIN`)
- ⚡ Real-time updates via Firebase Realtime Database (chat) and Firestore (student/clearance data)

## 🛠 Tech Stack

- **UI:** Jetpack Compose, Material 3
- **Architecture:** MVVM (ViewModel + StateFlow), Hilt for dependency injection
- **Navigation:** Jetpack Navigation Compose
- **Backend:** Firebase Authentication, Cloud Firestore, Firebase Realtime Database
- **Language:** Kotlin

## 📱 Screens

| Role    | Screens |
|---------|---------|
| Student | Dashboard, Profile, Messages, Shopping List |
| Admin   | Dashboard (student list + stats), Messages, Add Student, Student Profile (editable) |

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- A Firebase project with **Authentication**, **Cloud Firestore**, and **Realtime Database** enabled

### Setup

1. **Clone the repository**
```bash
   git clone https://github.com/<your-username>/RightWay_Out.git
   cd RightWay_Out
```

2. **Connect Firebase**
   - Create a project in the [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app with package name `com.example.rightway_out`
   - Download `google-services.json` and place it in `app/`
   - Enable **Email/Password** sign-in under Authentication
   - Enable **Cloud Firestore** and **Realtime Database**

3. **Firestore structure**
