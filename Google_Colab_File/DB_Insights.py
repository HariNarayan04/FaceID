import sqlite3
import pandas as pd
from tabulate import tabulate

def get_database_insights():
    output_lines = []  # Collect lines to write to file

    # Connect to the database
    conn = sqlite3.connect("User.db")
    cursor = conn.cursor()
    
    # Get the total number of unique roll numbers (different people)
    cursor.execute("SELECT COUNT(DISTINCT roll_no) FROM embeddings")
    total_people = cursor.fetchone()[0]
    
    # Get the count of embeddings for each roll number
    cursor.execute("""
        SELECT roll_no, COUNT(*) as embedding_count 
        FROM embeddings 
        GROUP BY roll_no 
        ORDER BY embedding_count DESC
    """)
    
    people_embeddings = cursor.fetchall()
    conn.close()
    
    # Store the insights
    output_lines.append("===== DATABASE INSIGHTS =====")
    output_lines.append(f"Total number of unique people (roll numbers): {total_people}")
    output_lines.append("\nEmbedding counts per person:")
    
    # Create a pretty table
    headers = ["Roll Number", "Number of Embeddings"]
    
    try:
        table_str = tabulate(people_embeddings, headers=headers, tablefmt="grid")
    except ImportError:
        # Fallback simple table if tabulate isn't available
        table_str = "\n{:<30} {:<20}".format("Roll Number", "Number of Embeddings")
        table_str += "\n" + "-" * 50
        for person in people_embeddings:
            table_str += "\n{:<30} {:<20}".format(person[0], person[1])
    
    output_lines.append(table_str)
    
    # Calculate some statistics
    if people_embeddings:
        df = pd.DataFrame(people_embeddings, columns=["roll_no", "count"])
        avg_embeddings = df["count"].mean()
        max_embeddings = df["count"].max()
        min_embeddings = df["count"].min()
        
        output_lines.append("\nStatistics:")
        output_lines.append(f"Average embeddings per person: {avg_embeddings:.2f}")
        output_lines.append(f"Maximum embeddings for a person: {max_embeddings}")
        output_lines.append(f"Minimum embeddings for a person: {min_embeddings}")
    
    # Write all output to a file
    with open("DBInsight.txt", "w") as f:
        for line in output_lines:
            f.write(line + "\n")
    
    return total_people, people_embeddings

if __name__ == "__main__":
    # Run the insights function
    try:
        get_database_insights()
    except sqlite3.OperationalError as e:
        with open("DBInsight.txt", "w") as f:
            if "no such table" in str(e):
                f.write("Error: The embeddings table doesn't exist. Please run the embedding storage script first.\n")
            else:
                f.write(f"Database error: {e}\n")
    except Exception as e:
        with open("DBInsight.txt", "w") as f:
            f.write(f"Error: {e}\n")