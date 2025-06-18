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

<div style="display: flex; flex-wrap: wrap; gap: 15px; justify-content: center;">
  <img src="Screenshots/image.png" alt="Front Page" width="30%" />
  <img src="Screenshots/image-1.png" alt="Security_Person Login" width="30%" />
  <img src="Screenshots/image-2.png" alt="Security Dashboard" width="30%" />
  <img src="Screenshots/image-3.png" alt="Camera Screen" width="30%" />
  <img src="Screenshots/image-4.png" alt="Image Upload" width="30%" />
  <img src="Screenshots/image-5.png" alt="Manual Rollno. Entry" width="30%" />
  <img src="Screenshots/image-6.png" alt="Result Screen" width="30%" />
  <img src="Screenshots/image-7.png" alt="Admin Login" width="30%" />
  <img src="Screenshots/image-8.png" alt="Admin Dashboard" width="30%" />
  <img src="Screenshots/image-9.png" alt="Security Person Management" width="30%" />
  <img src="Screenshots/image-10.png" alt="Verification Record" width="30%" />
</div>

## 📂 Project Structure
<pre>
```
FaceID/
├── Create_Database/              # First you need to create a user database which are going to access the permises
    ├── Valid_User_Database/      # Add your user's images named by ID(Rollno.) to this folder 
    ├── NewClearDB.py
    ├── NewDBInsight.py
    └── NewStoreEmbedding.py      # Run this file to generate User.db then transfer that file to FaceID_Backend/app/ml

├── FaceID_Backend/               # FastAPI server, ML models, database logic
    ├── app/                      # Main SRC folder that contains complete backend logic
        ├── ml/                   # Folder which contain core face recognition logic
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
    ├── uploads/                  # Here your uploaded/captured image will be stored for processing (keep on replacing)
        └── ...
    ├── faceid_users.db           # This is database of Security personals allowed by admin to use this app
    └── records.db                # Contains Entry/Exit logs

├── Google_Colab_File/            # This contains code for large dataset to be run on colab to compute Bayesian Threshold
    ├── CALFW_Image_Dataset/
    ├── Final_Result/             # Gives a highly detailed result upon running files in proper order (*check file paths)
    └── ...

├── frontend/                     # Jetpack Compose Android app
    └── ...
    
├── Screenshots/                  # Sample Screenshots of the Andorid app in use
├── .gitignore
├── LICENSE
├── Project_Report.pdf            # A detailed report over learning, experiment and observations (*For institute purpose)
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

