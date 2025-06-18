import sqlite3

DB_PATH = "Create_Database/User.db" #Embedding Database path

def clear_database():
    """Delete all records from the embeddings table."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Delete all rows from the embeddings table
    cursor.execute("DELETE FROM embeddings")
    
    # Reset the auto-increment ID counter
    cursor.execute("DELETE FROM sqlite_sequence WHERE name='embeddings'")
    
    conn.commit()
    conn.close()
    print("Database cleared successfully.")

# Run the function to clear the database
clear_database()