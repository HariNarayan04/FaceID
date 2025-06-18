from fastapi import FastAPI, HTTPException, File, UploadFile, Form, Path, Query
from app.models import User
from app.auth import authenticate_user
from app.database import create_table, get_db_connection
from app.ml.ml_process import process_image
from app.ml.ml_verify import verify_face
from app.record_logger import log_verification, initialize_logger
from pydantic import BaseModel
from typing import Optional, List
import shutil
import os
import sqlite3


app = FastAPI()

# Create database and add dummy users on startup
create_table()
# add_dummy_users()

# Initialize record logger
initialize_logger()

# Models for the API
class AdminSecurityRequest(BaseModel):
    email: str
    password: str
    oldEmail: Optional[str] = None

class SecurityUser(BaseModel):
    id: int
    email: str
    password: str  # Note: This would normally be hashed and not returned

class VerificationRecord(BaseModel):
    id: int
    roll_number: str
    timestamp: str
    status: str

class GenericResponse(BaseModel):
    status: str
    message: str

@app.post("/login")
def login(user: User):
    print("new login request")
    if authenticate_user(user.email, user.password):
        return {"message": "Login successful", "status": "success"}
    raise HTTPException(status_code=401, detail="Invalid email or password")

@app.post("/upload")
async def upload_file(file: UploadFile = File(...)):
    print("tried sending photo")  # ✅ Check if this gets printed
    with open(f"uploads/{file.filename}", "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    return {"filename": file.filename, "status": "success"}


UPLOAD_FOLDER = "uploads/"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.post("/process-image")
async def process_image_upload():
    print("Tried Processing Image")
    image_path = os.path.join(UPLOAD_FOLDER, "uploaded_image.jpg")
    roll_number = process_image(image_path)

    print("Rollno : ", roll_number)

    if roll_number:
        print("Performing verification")
        result = verify_face(image_path, roll_number)
        if result["verified"]:
            status = log_verification(roll_number)
            print(status)
        else:
            status = None

        return {
            "roll_no_found": True,
            "roll_number": result["roll_number"],
            "verified": result["verified"],
            "similarity_score": result["similarity_score"],
            "stored_face": result["stored_face"],
            "captured_face": result["captured_face"],
            "status": status,
            "message": "Face verified successfully!" if result["verified"] else "Face verification failed."
        }
    else:
        return {"roll_no_found": False}

@app.post("/manual-roll-input")
async def manual_roll_input(roll_number: str = Form(...)):
    print("Manual Rollno. Part")
    image_path = os.path.join(UPLOAD_FOLDER, "uploaded_image.jpg")

    result = verify_face(image_path, roll_number)
    if result["verified"]:
        status = log_verification(roll_number)
        print(result["similarity_score"])
        print(status)
    else:
        status = None

    return {
        "verified": result["verified"],
        "similarity_score": result["similarity_score"],
        "stored_face": result["stored_face"],
        "captured_face": result["captured_face"],
        "roll_number": result["roll_number"],
        "status": status,
        "message": "Face matched successfully!" if result["verified"] else "Face verification failed."
    }

# ----- Admin API Endpoints -----

@app.get("/admin/security-users", response_model=List[SecurityUser])
async def get_security_users():
    
    """Get all security users"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT id, email, password FROM users")
        users = [{"id": row[0], "email": row[1], "password": "********"} for row in cursor.fetchall()]
        conn.close()
        return users
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/admin/security-users", response_model=GenericResponse)
async def add_security_user(request: AdminSecurityRequest):
    """Add a new security user"""
    try:
        from app.utils import hash_password
        
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Check if email already exists
        cursor.execute("SELECT email FROM users WHERE email = ?", (request.email,))
        if cursor.fetchone():
            conn.close()
            raise HTTPException(status_code=400, detail="Email already exists")
        
        # Insert new user
        hashed_password = hash_password(request.password)
        cursor.execute(
            "INSERT INTO users (email, password) VALUES (?, ?)",
            (request.email, hashed_password)
        )
        conn.commit()
        conn.close()
        
        return {"status": "success", "message": "Security user added successfully"}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.put("/admin/security-users", response_model=GenericResponse)
async def update_security_user(request: AdminSecurityRequest):
    """Update an existing security user"""
    try:
        from app.utils import hash_password
        
        if not request.oldEmail:
            raise HTTPException(status_code=400, detail="Old email is required")
        
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Check if old email exists
        cursor.execute("SELECT id FROM users WHERE email = ?", (request.oldEmail,))
        user = cursor.fetchone()
        if not user:
            conn.close()
            raise HTTPException(status_code=404, detail="User not found")
        
        # Check if new email already exists (if changing email)
        if request.email != request.oldEmail:
            cursor.execute("SELECT id FROM users WHERE email = ?", (request.email,))
            if cursor.fetchone():
                conn.close()
                raise HTTPException(status_code=400, detail="Email already exists")
        
        # Update user
        if request.password:
            # Update both email and password
            hashed_password = hash_password(request.password)
            cursor.execute(
                "UPDATE users SET email = ?, password = ? WHERE email = ?",
                (request.email, hashed_password, request.oldEmail)
            )
        else:
            # Update email only
            cursor.execute(
                "UPDATE users SET email = ? WHERE email = ?",
                (request.email, request.oldEmail)
            )
        
        conn.commit()
        conn.close()
        
        return {"status": "success", "message": "Security user updated successfully"}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/admin/security-users/{email}", response_model=GenericResponse)
async def delete_security_user(email: str):
    """Delete a security user"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Check if user exists
        cursor.execute("SELECT id FROM users WHERE email = ?", (email,))
        if not cursor.fetchone():
            conn.close()
            raise HTTPException(status_code=404, detail="User not found")
        
        # Delete user
        cursor.execute("DELETE FROM users WHERE email = ?", (email,))
        conn.commit()
        conn.close()
        
        return {"status": "success", "message": "Security user deleted successfully"}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/admin/records", response_model=List[VerificationRecord])
async def get_verification_records():
    """Get all verification records"""
    print("verification started")
    try:
        conn = sqlite3.connect("records.db")
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()

        print("connection estb")
        
        cursor.execute("SELECT id, roll_number, timestamp, status FROM records ORDER BY timestamp DESC")
        records = [dict(row) for row in cursor.fetchall()]
        
        conn.close()
        return records
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/admin/records", response_model=GenericResponse)
async def clear_all_records():
    """Clear all verification records"""
    try:
        print("verification started3")
        conn = sqlite3.connect("records.db")
        cursor = conn.cursor()
        
        print("connection estb2")
        cursor.execute("DELETE FROM records")
        conn.commit()
        conn.close()
        
        return {"status": "success", "message": "All records cleared successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
