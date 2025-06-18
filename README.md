# FaceID

A secure face and ID verification access system powered by **InsightFace**, **Kotlin**, and **Python**.

## 🚀 Overview

**FaceID** is a real-time identity verification system that uses facial recognition combined with ID card parsing to control and log access securely. Built for environments requiring tight access control, the system integrates machine learning, mobile interfaces, and backend automation.

## 🧠 Tech Stack

- **Frontend**: Kotlin (Jetpack Compose)
- **Backend**: Python (FastAPI)
- **Face Recognition**: InsightFace (ArcFace model)
- **Database**: SQLite (configurable)
- **Deployment**: Not Ready Yet

## ✨ Features

- 🔍 Real-time face + ID verification using live camera input
- 🧾 Automated access logging with timestamps and status
- 🛡️ Admin panel for regulating and reviewing access events
- 📱 User-friendly Android UI for seamless operation
- 📷 ML-powered extraction of roll number from ID card
- ✅ Face matching using 512-D embeddings and ArcFace

## 📸 Sample Screenshots

<img src="image.png" alt="Front Page" width="30%" />
<img src="image-1.png" alt="Security_Person Login" width="30%" />
<img src="image-2.png" alt="Security Dashboard" width="30%" />
<img src="image-3.png" alt="Camera Screen" width="30%" />
<img src="image-4.png" alt="Image Upload" width="30%" />
<img src="image-5.png" alt="Manual Rollno. Entry" width="30%" />
<img src="image-6.png" alt="Result Screen" width="30%" />
<img src="image-7.png" alt="Admin Login" width="30%" />
<img src="image-8.png" alt="Admin Dashboard" width="30%" />
<img src="image-9.png" alt="Security Person Management" width="30%" />
<img src="image-10.png" alt="Verification Record" width="30%" />

## 📂 Project Structure
<pre>
```
FaceID/
├── Create_Database/
    ├── Valid_User_Database/
    ├── NewClearDB.py
    ├── NewDBInsight.py
    └── NewStoreEmbedding.py
├── FaceID_Backend/                   # FastAPI server, ML models, database logic
    ├── app/
        ├── ml/
            ├── User.db
            ├── databaselog.txt
            ├── ml_init.py
            ├── ml_process.py
            └── ml_verify.py
        ├── __init__.py
        ├── auth.py
        ├── clear_record.py
        ├── database.py
        ├── main.py
        ├── models.py
        ├── record_logger.py
        ├── utils.py
        └── view_record.py
    ├── uploads/
        └── ...
    ├── faceid_users.db
    └── records.db
├── Google_Colab_File/
    ├── CALFW_Image_Dataset/
    ├── Final_Result/
    └── ...
├── frontend/                  # Jetpack Compose Android app
    └── ...
├── .gitignore
├── LICENSE
├── Project_Report.pdf
├── README.md
└── requirements.txt
```
</pre>

## 🛠️ Setup Instructions

### Backend

```bash
cd backend
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload
```

### Frontend
First you need to change the IP address in built 
Then build APK and run on your device
For admin restricted is limited to changes in frontend so add your username and password in frontend itself

