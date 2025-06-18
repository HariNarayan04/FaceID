import sqlite3
import numpy as np
import cv2
import insightface
import os
import re

DB_PATH = "Create_Database/User.db" #Embedding Database path

def initialize_db():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Create the table with an auto-increment ID, allowing multiple embeddings per roll_no
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS embeddings (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            roll_no TEXT UNIQUE,
            embedding BLOB,
            cropped_face BLOB
        )
    """)
    
    conn.commit()
    conn.close()
    print("Database initialized successfully.")

def extract_and_preprocess_face(image_path, detector):
    img = cv2.imread(image_path)
    if img is None:
        print(f"Warning: Failed to read {image_path}")
        return None, None

    faces = detector.get(img)
    if not faces:
        print(f"Warning: No face detected in {image_path}")
        return None, None

    face = faces[0]  # Assuming one face per image
    face_bbox = face.bbox  # Bounding box of the detected face
    embedding = face.embedding  # Extracted face embedding

    # Crop and encode the detected face
    x1, y1, x2, y2 = map(int, face_bbox)
    cropped_face = img[y1:y2, x1:x2]
    _, face_blob = cv2.imencode('.jpg', cropped_face)

    return embedding, face_blob.tobytes()

def store_embedding(roll_no, embedding, cropped_face):
    if embedding is None or cropped_face is None:
        return

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    cursor.execute("""
        INSERT INTO embeddings (roll_no, embedding, cropped_face) 
        VALUES (?, ?, ?)
        ON CONFLICT(roll_no) DO UPDATE SET 
        embedding=excluded.embedding, 
        cropped_face=excluded.cropped_face
    """, (roll_no, embedding, cropped_face))

    conn.commit()
    conn.close()
    print(f"Stored/Updated embedding and face for roll no: {roll_no}")

def process_images(folder_path):
    app = insightface.app.FaceAnalysis(name='buffalo_l')  
    app.prepare(ctx_id=0)  # buffalo_l includes RetinaFace, so this handles detection

    initialize_db()

    for file_name in os.listdir(folder_path):
        match = re.match(r"(\d+)\.jpg", file_name) #You can modify image extention here
        if match:
            roll_no = match.group(1).replace(" ", "_")
            image_path = os.path.join(folder_path, file_name)
            print(f"Processing {file_name}...")

            img = cv2.imread(image_path)
            if img is None:
                print(f"Warning: Failed to read {image_path}")
                continue

            faces = app.get(img)  # Detect faces using buffalo_l
            if not faces:
                print(f"Warning: No face detected in {image_path}")
                continue

            face = faces[0]  # Assuming one face per image
            face_bbox = face.bbox  # Bounding box of the detected face
            embedding = face.embedding  # Extracted face embedding

            # Crop and encode the detected face
            x1, y1, x2, y2 = map(int, face_bbox)
            cropped_face = img[y1:y2, x1:x2]
            _, face_blob = cv2.imencode('.jpg', cropped_face)

            store_embedding(roll_no, embedding, face_blob.tobytes())
            print(f"Stored for roll no: {roll_no}...")

folder_path = "Create_Database/Valid_User_Database"  # Change this to your valid database path
process_images(folder_path)