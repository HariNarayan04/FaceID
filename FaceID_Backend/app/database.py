import sqlite3
from app.utils import hash_password


# Connect to SQLite database
def get_db_connection():
    conn = sqlite3.connect("faceid_users.db")
    conn.row_factory = sqlite3.Row
    return conn

# Create user table if not exists
def create_table():
    conn = get_db_connection()
    conn.execute("""
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        email TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL
    )
    """)
    conn.commit()
    conn.close()

