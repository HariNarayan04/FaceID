import sqlite3
import numpy as np
import pandas as pd
import random
from sklearn.metrics.pairwise import cosine_similarity

def load_all_embeddings():
    """Load all embeddings from the database with their roll numbers and IDs"""
    conn = sqlite3.connect("User.db")
    cursor = conn.cursor()
    
    cursor.execute("SELECT id, roll_no, embedding, image_id FROM embeddings")
    results = cursor.fetchall()
    conn.close()
    
    embeddings_data = []
    for row in results:
        db_id, roll_no, embedding_blob, image_id = row
        # Convert BLOB to numpy array
        embedding_array = np.frombuffer(embedding_blob, dtype=np.float32)
        embeddings_data.append({
            'db_id': db_id,
            'roll_no': roll_no,
            'image_id': image_id,
            'embedding': embedding_array
        })
    
    return embeddings_data

def generate_genuine_scores(embeddings_data):
    """Generate similarity scores between embeddings of same person"""
    genuine_scores = []
    
    # Group embeddings by roll_no
    roll_to_embeddings = {}
    for entry in embeddings_data:
        roll_no = entry['roll_no']
        if roll_no not in roll_to_embeddings:
            roll_to_embeddings[roll_no] = []
        roll_to_embeddings[roll_no].append(entry)
    
    # For each person, calculate similarity between each pair of their embeddings
    for roll_no, embeddings in roll_to_embeddings.items():
        # Skip if there's only one embedding for this person
        if len(embeddings) < 2:
            continue
            
        # Compare each pair of embeddings
        for i in range(len(embeddings)):
            for j in range(i + 1, len(embeddings)):
                emb1 = embeddings[i]['embedding'].reshape(1, -1)
                emb2 = embeddings[j]['embedding'].reshape(1, -1)
                
                similarity = cosine_similarity(emb1, emb2)[0][0]
                
                genuine_scores.append({
                    'roll_no': roll_no,
                    'image_id_1': embeddings[i]['image_id'],
                    'image_id_2': embeddings[j]['image_id'],
                    'similarity_score': similarity
                })
    
    return genuine_scores

def generate_imposter_scores(embeddings_data):
    """Generate similarity scores between embeddings of different people"""
    imposter_scores = []
    
    # Group embeddings by roll_no
    roll_to_embeddings = {}
    for entry in embeddings_data:
        roll_no = entry['roll_no']
        if roll_no not in roll_to_embeddings:
            roll_to_embeddings[roll_no] = []
        roll_to_embeddings[roll_no].append(entry)
    
    all_roll_nos = list(roll_to_embeddings.keys())
    
    # For each embedding, find one random embedding from a different person
    for entry in embeddings_data:
        current_roll = entry['roll_no']
        
        # Get list of other roll numbers
        other_rolls = [r for r in all_roll_nos if r != current_roll]
        if not other_rolls:  # Skip if there are no other people
            continue
        
        # Select a random different person
        other_roll = random.choice(other_rolls)
        
        # Select a random embedding from that person
        other_entry = random.choice(roll_to_embeddings[other_roll])
        
        # Calculate similarity
        emb1 = entry['embedding'].reshape(1, -1)
        emb2 = other_entry['embedding'].reshape(1, -1)
        
        similarity = cosine_similarity(emb1, emb2)[0][0]
        
        imposter_scores.append({
            'roll_no_1': entry['roll_no'],
            'image_id_1': entry['image_id'],
            'roll_no_2': other_entry['roll_no'],
            'image_id_2': other_entry['image_id'],
            'similarity_score': similarity
        })
    
    return imposter_scores

def main():
    print("Loading embeddings from database...")
    embeddings_data = load_all_embeddings()
    
    if not embeddings_data:
        print("No embeddings found in the database.")
        return
    
    print(f"Loaded {len(embeddings_data)} embeddings from database.")
    
    # Generate genuine scores
    print("Generating genuine similarity scores...")
    genuine_scores = generate_genuine_scores(embeddings_data)
    genuine_df = pd.DataFrame(genuine_scores)
    genuine_df.to_csv('genuine_scores.csv', index=False)
    print(f"Generated {len(genuine_scores)} genuine similarity scores.")
    
    # Generate imposter scores
    print("Generating imposter similarity scores...")
    imposter_scores = generate_imposter_scores(embeddings_data)
    imposter_df = pd.DataFrame(imposter_scores)
    imposter_df.to_csv('imposter_scores.csv', index=False)
    print(f"Generated {len(imposter_scores)} imposter similarity scores.")
    
    print("Complete! Files saved as 'genuine_scores.csv' and 'imposter_scores.csv'")
    
    # Print some statistics
    if genuine_scores:
        genuine_similarities = [score['similarity_score'] for score in genuine_scores]
        print(f"\nGenuine similarity statistics:")
        print(f"Average: {np.mean(genuine_similarities):.4f}")
        print(f"Min: {np.min(genuine_similarities):.4f}")
        print(f"Max: {np.max(genuine_similarities):.4f}")
    
    if imposter_scores:
        imposter_similarities = [score['similarity_score'] for score in imposter_scores]
        print(f"\nImposter similarity statistics:")
        print(f"Average: {np.mean(imposter_similarities):.4f}")
        print(f"Min: {np.min(imposter_similarities):.4f}")
        print(f"Max: {np.max(imposter_similarities):.4f}")

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"Error: {e}")