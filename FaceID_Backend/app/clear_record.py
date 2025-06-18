import sqlite3

DB_NAME = "Backend_Test/records.db"

def clear_records():
    conn = sqlite3.connect(DB_NAME)
    c = conn.cursor()

    c.execute("DELETE FROM records")
    conn.commit()

    print("All records have been cleared.")

    conn.close()

if __name__ == "__main__":
    clear_records()
