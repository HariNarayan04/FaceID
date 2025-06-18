import sqlite3
import numpy as np
import cv2
import insightface
import os
import re

def initialize_db():
    conn = sqlite3.connect("User.db")
    cursor = conn.cursor()
    
    # Modified table structure to allow multiple embeddings per roll_no
    # Removed cropped_face field
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS embeddings (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            roll_no TEXT,
            embedding BLOB,
            image_id TEXT
        )
    """)
    
    # Create an index on roll_no for faster lookups
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_roll_no ON embeddings (roll_no)")
    
    conn.commit()
    conn.close()
    print("Database initialized successfully.")

def extract_face_embedding(image_path, detector):
    img = cv2.imread(image_path)
    if img is None:
        print(f"Warning: Failed to read {image_path}")
        return None

    faces = detector.get(img)
    if not faces:
        print(f"Warning: No face detected in {image_path}")
        return None

    face = faces[0]  # Assuming one face per image
    embedding = face.embedding  # Extracted face embedding

    return embedding

def store_embedding(roll_no, embedding, image_id):
    if embedding is None:
        return

    conn = sqlite3.connect("User.db")
    cursor = conn.cursor()

    # Modified to insert each embedding as a new record, without cropped_face
    cursor.execute("""
        INSERT INTO embeddings (roll_no, embedding, image_id) 
        VALUES (?, ?, ?)
    """, (roll_no, embedding, image_id))

    conn.commit()
    conn.close()
    print(f"Stored embedding for roll no: {roll_no}, image ID: {image_id}")

def process_images(folder_path):
    app = insightface.app.FaceAnalysis(name='buffalo_l')  
    app.prepare(ctx_id=0)  # buffalo_l includes RetinaFace, so this handles detection

    initialize_db()

    for file_name in os.listdir(folder_path):
        # New regex pattern to extract roll_no from file names like "Jackie_Chan_0001.jpg"
        match = re.match(r"(.+)_(\d+)\.jpg", file_name)
        if match:
            roll_no = match.group(1)  # Extract the name part (e.g., "Jackie_Chan")
            image_id = match.group(2)  # Extract the numeric ID (e.g., "0001")
            image_path = os.path.join(folder_path, file_name)
            print(f"Processing {file_name}...")

            embedding = extract_face_embedding(image_path, app)
            
            if embedding is not None:
                store_embedding(roll_no, embedding.tobytes(), image_id)
                print(f"Stored embedding for roll no: {roll_no}, image ID: {image_id}")
            else:
                print(f"Failed to process {file_name}")

if __name__ == "__main__":
    folder_path = "CALFW_Image_Dataset"  # Change this to your actual dataset path
    process_images(folder_path)