import sqlite3

DB_NAME = "Backend_Test/records.db"

def view_records():
    conn = sqlite3.connect(DB_NAME)
    c = conn.cursor()

    c.execute("SELECT * FROM records ORDER BY timestamp DESC")
    rows = c.fetchall()

    for row in rows:
        print(row)


    conn.close()

if __name__ == "__main__":
    view_records()
