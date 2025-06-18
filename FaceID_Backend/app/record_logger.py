import sqlite3
from datetime import datetime

DB_NAME = "records.db"

# Ensure the table exists
def initialize_logger():
    conn = sqlite3.connect(DB_NAME)
    c = conn.cursor()
    c.execute('''
        CREATE TABLE IF NOT EXISTS records (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            roll_number TEXT NOT NULL,
            timestamp TEXT NOT NULL,
            status TEXT NOT NULL
        )
    ''')
    conn.commit()
    conn.close()

# Call once on import
initialize_logger()

def log_verification(roll_number: str) -> str:
    conn = sqlite3.connect(DB_NAME)
    c = conn.cursor()

    # Get the last status for this roll number
    c.execute(
        "SELECT status FROM records WHERE roll_number = ? ORDER BY timestamp DESC LIMIT 1",
        (roll_number,)
    )
    row = c.fetchone()

    # Decide next status based on last status
    last_status = row[0] if row else None
    status = "Exit" if last_status == "Entry" else "Entry"

    # Insert new log
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    c.execute("INSERT INTO records (roll_number, timestamp, status) VALUES (?, ?, ?)",
              (roll_number, timestamp, status))

    conn.commit()
    conn.close()

    return status