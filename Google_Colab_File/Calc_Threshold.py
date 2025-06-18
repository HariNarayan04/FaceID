import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy import stats
import seaborn as sns

def load_scores():
    """Load similarity scores from CSV files"""
    genuine_df = pd.read_csv("genuine_scores.csv")
    imposter_df = pd.read_csv("imposter_scores.csv")
    
    # Extract similarity scores from the dataframes
    genuine_scores = genuine_df["similarity_score"].values
    imposter_scores = imposter_df["similarity_score"].values
    
    return genuine_scores, imposter_scores

def compute_bayesian_threshold():
    """Compute Bayesian threshold and visualize distributions"""
    genuine_scores, imposter_scores = load_scores()
    
    print(f"Loaded {len(genuine_scores)} genuine scores and {len(imposter_scores)} imposter scores")
    
    # Print some statistics about the scores
    print("\nGenuine Scores Statistics:")
    print(f"Mean: {np.mean(genuine_scores):.4f}")
    print(f"Std: {np.std(genuine_scores):.4f}")
    print(f"Min: {np.min(genuine_scores):.4f}")
    print(f"Max: {np.max(genuine_scores):.4f}")
    
    print("\nImposter Scores Statistics:")
    print(f"Mean: {np.mean(imposter_scores):.4f}")
    print(f"Std: {np.std(imposter_scores):.4f}")
    print(f"Min: {np.min(imposter_scores):.4f}")
    print(f"Max: {np.max(imposter_scores):.4f}")

    # Fit Gaussian distributions
    genuine_mean, genuine_std = np.mean(genuine_scores), np.std(genuine_scores)
    imposter_mean, imposter_std = np.mean(imposter_scores), np.std(imposter_scores)

    genuine_dist = stats.norm(genuine_mean, genuine_std)
    imposter_dist = stats.norm(imposter_mean, imposter_std)

    # Find threshold where P(S|G) = P(S|I)
    x_values = np.linspace(-0.5, 1.0, 1000)  # Adjusted for cosine similarity range
    genuine_pdf = genuine_dist.pdf(x_values)
    imposter_pdf = imposter_dist.pdf(x_values)

    # Find the intersection point after zero
    valid_indices = np.where(x_values > 0)[0]
    threshold_index = valid_indices[np.argmin(np.abs(genuine_pdf[valid_indices] - imposter_pdf[valid_indices]))]
    optimal_threshold = x_values[threshold_index]

    # Create plots
    plt.figure(figsize=(12, 8))
    
    # Plot 1: PDF distributions with threshold
    plt.subplot(2, 1, 1)
    plt.plot(x_values, genuine_pdf, label="Genuine Distribution", color="green", linewidth=2)
    plt.plot(x_values, imposter_pdf, label="Impostor Distribution", color="red", linewidth=2)
    plt.axvline(optimal_threshold, color="blue", linestyle="--", linewidth=2, 
                label=f"Threshold = {optimal_threshold:.4f}")
    plt.legend(fontsize=10)
    plt.xlabel("Similarity Score", fontsize=12)
    plt.ylabel("Probability Density", fontsize=12)
    plt.title("Bayesian Threshold for Face Similarity", fontsize=14)
    plt.grid(True, alpha=0.3)
    
    # Plot 2: Histograms of actual scores
    plt.subplot(2, 1, 2)
    sns.histplot(genuine_scores, color="green", kde=True, label="Genuine Scores", alpha=0.6)
    sns.histplot(imposter_scores, color="red", kde=True, label="Impostor Scores", alpha=0.6)
    plt.axvline(optimal_threshold, color="blue", linestyle="--", linewidth=2,
                label=f"Threshold = {optimal_threshold:.4f}")
    plt.legend(fontsize=10)
    plt.xlabel("Similarity Score", fontsize=12)
    plt.ylabel("Frequency", fontsize=12)
    plt.title("Distribution of Actual Similarity Scores", fontsize=14)
    plt.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.savefig("similarity_threshold_analysis.png", dpi=300)
    plt.show()

    print(f"\nOptimal Bayesian threshold: {optimal_threshold:.4f}")
    
    # Calculate performance metrics at the threshold
    genuine_accept = np.sum(genuine_scores >= optimal_threshold) / len(genuine_scores)
    imposter_reject = np.sum(imposter_scores < optimal_threshold) / len(imposter_scores)
    
    print(f"True Accept Rate (TAR): {genuine_accept:.4f} ({genuine_accept*100:.2f}%)")
    print(f"True Reject Rate (TRR): {imposter_reject:.4f} ({imposter_reject*100:.2f}%)")
    print(f"False Accept Rate (FAR): {1-imposter_reject:.4f} ({(1-imposter_reject)*100:.2f}%)")
    print(f"False Reject Rate (FRR): {1-genuine_accept:.4f} ({(1-genuine_accept)*100:.2f}%)")
    
    return optimal_threshold

# Run the function
if __name__ == "__main__":
    try:
        threshold = compute_bayesian_threshold()
        
        # Save the threshold to a file for later use
        with open("optimal_threshold.txt", "w") as f:
            f.write(f"{threshold:.6f}")
        print(f"Threshold saved to 'optimal_threshold.txt'")
        
    except FileNotFoundError as e:
        print(f"Error: {e}")
        print("Please make sure you've generated the genuine_scores.csv and imposter_scores.csv files first.")
    except Exception as e:
        print(f"Error: {e}")