import cv2
import sqlite3
import numpy as np
import insightface
import base64
from io import BytesIO
from sklearn.metrics.pairwise import cosine_similarity

# Initialize InsightFace model
app = insightface.app.FaceAnalysis(name='buffalo_l')
app.prepare(ctx_id=0)

# Cosine similarity threshold for face matching
THRESHOLD = 0.16

def extract_face_embedding_and_crop(image_path, app):
    """Extract face embedding and cropped face image from the provided image."""
    img = cv2.imread(image_path)
    if img is None:
        print(f"Warning: Failed to read {image_path}")
        return None, None
    
    faces = app.get(img)
    if faces:
        embedding = faces[0].embedding

        # Get cropped face
        x1, y1, x2, y2 = map(int, faces[0].bbox)
        cropped_face = img[y1:y2, x1:x2]

        # Encode cropped face to base64
        _, buffer = cv2.imencode('.jpg', cropped_face)
        cropped_face_base64 = base64.b64encode(buffer).decode('utf-8')

        return embedding, cropped_face_base64
    else:
        print(f"Warning: No face detected in {image_path}")
        return None, None

def get_face_data(roll_no, db_path="app/ml/User.db"):
    """Retrieve stored face embedding and cropped face for the given roll number."""
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()

        cursor.execute("SELECT embedding, cropped_face FROM embeddings WHERE roll_no = ?", (roll_no,))
        result = cursor.fetchone()
        conn.close()

        if result:
            embedding = np.frombuffer(result[0], dtype=np.float32)

            # Convert stored cropped face (BLOB) to base64
            stored_face_base64 = base64.b64encode(result[1]).decode('utf-8') if result[1] else None
            return embedding, stored_face_base64
        else:
            print(f"No data found for roll number {roll_no}")
            return None, None
    except Exception as e:
        print(f"Error accessing database: {e}")
        return None, None

def verify_face(image_path, roll_number):
    """Verify if the face in the image matches the stored embedding for the roll number."""
    # Extract embedding and cropped face from uploaded image
    embd1, captured_face_base64 = extract_face_embedding_and_crop(image_path, app)
    if embd1 is None:
        return {"verified": False}

    # Fetch stored embedding and cropped face from database
    embd2, stored_face_base64 = get_face_data(roll_number)
    if embd2 is None:
        return {"verified": False}

    # Compute cosine similarity
    embd1 = embd1.reshape(1, -1)
    embd2 = embd2.reshape(1, -1)
    similarity = cosine_similarity(embd1, embd2)[0][0]

    print(f"Cosine Similarity Score: {similarity:.4f}")

    # Determine verification result
    verified = bool(similarity >= THRESHOLD)

    return {
        "verified": verified,
        "similarity_score": float(similarity),
        "stored_face": stored_face_base64,
        "captured_face": captured_face_base64,
        "roll_number": roll_number
    }