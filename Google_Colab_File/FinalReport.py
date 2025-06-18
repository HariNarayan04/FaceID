import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.metrics import (
    confusion_matrix, precision_score, recall_score, f1_score, 
    accuracy_score, ConfusionMatrixDisplay, roc_curve, auc
)
import seaborn as sns
import os
from datetime import datetime
from scipy.integrate import trapezoid

# Load stored similarity scores
def load_scores():
    genuine_df = pd.read_csv("genuine_scores.csv")
    imposter_df = pd.read_csv("imposter_scores.csv")
    
    genuine_scores = genuine_df["similarity_score"].values
    imposter_scores = imposter_df["similarity_score"].values
    
    return genuine_scores, imposter_scores

# Function to create evaluation results directory
def create_results_dir():
    results_dir = f"Final_Results"
    os.makedirs(results_dir, exist_ok=True)
    return results_dir

# Evaluate metrics at different thresholds
def evaluate_metrics(thresholds, results_dir):
    genuine_scores, imposter_scores = load_scores()
    
    print(f"Loaded {len(genuine_scores)} genuine scores and {len(imposter_scores)} imposter scores")

    # Create ground truth labels: 1 for genuine, 0 for imposter
    y_true = np.concatenate([np.ones_like(genuine_scores), np.zeros_like(imposter_scores)])
    all_scores = np.concatenate([genuine_scores, imposter_scores])

    # Initialize lists to store metric results
    precisions, recalls, f1_scores, accuracies = [], [], [], []
    fars, frrs, tars, trrs = [], [], [], []
    y_pred_list = []

    # Create text file for results
    results_file = os.path.join(results_dir, "threshold_metrics.txt")
    with open(results_file, 'w') as f:
        f.write("=" * 80 + "\n")
        f.write("FACE RECOGNITION THRESHOLD EVALUATION RESULTS\n")
        f.write(f"Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 80 + "\n\n")
        
        f.write(f"Total genuine pairs: {len(genuine_scores)}\n")
        f.write(f"Total imposter pairs: {len(imposter_scores)}\n")
        f.write(f"Genuine score range: [{min(genuine_scores):.4f}, {max(genuine_scores):.4f}]\n")
        f.write(f"Imposter score range: [{min(imposter_scores):.4f}, {max(imposter_scores):.4f}]\n\n")
        
        f.write("EVALUATION METRICS BY THRESHOLD\n")
        f.write("=" * 80 + "\n")
        f.write(f"{'Threshold':<10} {'Accuracy':<10} {'Precision':<10} {'Recall':<10} {'F1 Score':<10} " +
                f"{'TAR':<10} {'TRR':<10} {'FAR':<10} {'FRR':<10}\n")
        f.write("-" * 80 + "\n")

        for threshold in thresholds:
            # Predicted labels based on threshold
            genuine_predictions = (genuine_scores >= threshold).astype(int)
            imposter_predictions = (imposter_scores >= threshold).astype(int)
            
            y_pred = np.concatenate([
                genuine_predictions,  # Genuine classified as genuine (1) or imposter (0)
                imposter_predictions  # Imposter classified as genuine (1) or imposter (0)
            ])
            y_pred_list.append(y_pred)

            # Compute metrics
            precision = precision_score(y_true, y_pred)
            recall = recall_score(y_true, y_pred)
            f1 = f1_score(y_true, y_pred)
            accuracy = accuracy_score(y_true, y_pred)
            
            # Compute TAR, TRR, FAR, FRR
            tar = np.sum(genuine_predictions == 1) / len(genuine_predictions)  # True Accept Rate
            frr = 1 - tar  # False Reject Rate
            trr = np.sum(imposter_predictions == 0) / len(imposter_predictions)  # True Reject Rate
            far = 1 - trr  # False Accept Rate
            
            # Store metrics
            precisions.append(precision)
            recalls.append(recall)
            f1_scores.append(f1)
            accuracies.append(accuracy)
            tars.append(tar)
            trrs.append(trr)
            fars.append(far)
            frrs.append(frr)
                    
            # Create confusion matrix for this threshold
            cm = confusion_matrix(y_true, y_pred)
            f.write(f"\nConfusion Matrix at Threshold {threshold:.4f}:\n")
            f.write(f"TN={cm[0,0]}, FP={cm[0,1]}\n")
            f.write(f"FN={cm[1,0]}, TP={cm[1,1]}\n\n")

            # Write metrics to file
            f.write(f"{threshold:<10.4f} {accuracy:<10.4f} {precision:<10.4f} {recall:<10.4f} {f1:<10.4f} " +
                    f"{tar:<10.4f} {trr:<10.4f} {far:<10.4f} {frr:<10.4f}\n")
            
            # # Generate and save confusion matrix plot
            # plt.figure(figsize=(8, 6))
            # disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=["Imposter", "Genuine"])
            # disp.plot(cmap="Blues", values_format="d")
            # plt.title(f"Confusion Matrix at Threshold = {threshold:.4f}")
            # plt.tight_layout()
            # plt.savefig(os.path.join(results_dir, f"confusion_matrix_{threshold:.4f}.png"))
            # plt.close()
            
        # Find the optimal threshold based on different criteria
        f.write("\nOPTIMAL THRESHOLDS\n")
        f.write("=" * 80 + "\n")
        
        # Best accuracy
        best_acc_idx = np.argmax(accuracies)
        f.write(f"Best Accuracy: {accuracies[best_acc_idx]:.4f} at threshold {thresholds[best_acc_idx]:.4f}\n")
        
        # Best F1 score
        best_f1_idx = np.argmax(f1_scores)
        f.write(f"Best F1 Score: {f1_scores[best_f1_idx]:.4f} at threshold {thresholds[best_f1_idx]:.4f}\n")
        
        # Equal Error Rate (where FAR = FRR)
        diff_far_frr = np.abs(np.array(fars) - np.array(frrs))
        eer_idx = np.argmin(diff_far_frr)
        f.write(f"Equal Error Rate point: FAR={fars[eer_idx]:.4f}, FRR={frrs[eer_idx]:.4f} at threshold {thresholds[eer_idx]:.4f}\n")
    
    print(f"Evaluation results saved to {results_file}")
    return precisions, recalls, f1_scores, accuracies, tars, trrs, fars, frrs, y_pred_list

# Plot the metrics
def plot_metrics(thresholds, precisions, recalls, f1_scores, accuracies, tars, trrs, fars, frrs, results_dir):
    # Plot 1: Classification metrics
    plt.figure(figsize=(15, 10))
    
    plt.subplot(2, 1, 1)
    plt.plot(thresholds, precisions, label="Precision", marker='o', markersize=4, linewidth=2)
    plt.plot(thresholds, recalls, label="Recall", marker='s', markersize=4, linewidth=2)
    plt.plot(thresholds, f1_scores, label="F1 Score", marker='^', markersize=4, linewidth=2)
    plt.plot(thresholds, accuracies, label="Accuracy", marker='d', markersize=4, linewidth=2)

    plt.xlabel("Threshold", fontsize=12)
    plt.ylabel("Score", fontsize=12)
    plt.title("Classification Metrics vs. Threshold", fontsize=14)
    plt.legend(fontsize=10)
    plt.grid(True, alpha=0.3)
    plt.xticks(np.round(thresholds, 2))
    
    # Plot 2: Face recognition specific metrics
    plt.subplot(2, 1, 2)
    plt.plot(thresholds, tars, label="True Accept Rate (TAR)", marker='o', markersize=4, linewidth=2)
    plt.plot(thresholds, trrs, label="True Reject Rate (TRR)", marker='s', markersize=4, linewidth=2)
    plt.plot(thresholds, fars, label="False Accept Rate (FAR)", marker='^', markersize=4, linewidth=2, linestyle='--')
    plt.plot(thresholds, frrs, label="False Reject Rate (FRR)", marker='d', markersize=4, linewidth=2, linestyle='--')
    
    plt.xlabel("Threshold", fontsize=12)
    plt.ylabel("Rate", fontsize=12)
    plt.title("Face Recognition Performance Metrics vs. Threshold", fontsize=14)
    plt.legend(fontsize=10)
    plt.grid(True, alpha=0.3)
    plt.xticks(np.round(thresholds, 2))
    
    plt.tight_layout()
    plt.savefig(os.path.join(results_dir, "threshold_metrics.png"), dpi=300)
    
    # Plot 3: ROC Curve
    plt.figure(figsize=(8, 8))
    plt.plot(fars, tars, 'b-', linewidth=2, marker='o', markersize=4)
    plt.plot([0, 1], [0, 1], 'k--', alpha=0.5)  # Diagonal line
    
    # Calculate AUC
    roc_auc = trapezoid(tars, fars)
    
    plt.xlabel('False Accept Rate (FAR)', fontsize=12)
    plt.ylabel('True Accept Rate (TAR)', fontsize=12)
    plt.title(f'ROC Curve (AUC = {roc_auc:.4f})', fontsize=14)
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(os.path.join(results_dir, "roc_curve.png"), dpi=300)
    
    # Plot 4: FAR-FRR intersection (Equal Error Rate)
    plt.figure(figsize=(10, 6))
    plt.plot(thresholds, fars, label='FAR', linewidth=2)
    plt.plot(thresholds, frrs, label='FRR', linewidth=2)
    
    # Find and mark the EER point
    diff_far_frr = np.abs(np.array(fars) - np.array(frrs))
    eer_idx = np.argmin(diff_far_frr)
    eer_threshold = thresholds[eer_idx]
    eer_value = (fars[eer_idx] + frrs[eer_idx]) / 2
    
    plt.scatter([eer_threshold], [eer_value], s=100, c='red', zorder=5)
    plt.annotate(f'EER: {eer_value:.4f} @ {eer_threshold:.4f}', 
                xy=(eer_threshold, eer_value), 
                xytext=(eer_threshold+0.05, eer_value+0.1),
                arrowprops=dict(facecolor='black', shrink=0.05))
    
    plt.xlabel('Threshold', fontsize=12)
    plt.ylabel('Error Rate', fontsize=12)
    plt.title('FAR-FRR Curves and Equal Error Rate', fontsize=14)
    plt.legend(fontsize=10)
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(os.path.join(results_dir, "far_frr_curve.png"), dpi=300)
    
    print(f"All plots saved to {results_dir}")

# Run evaluation and plot results
if __name__ == "__main__":
    try:
        # Create results directory
        results_dir = create_results_dir()
        
        optimal_threshold = 0.1682
        print(f"Using default threshold: {optimal_threshold:.4f}")
            
        # Generate thresholds around the optimal one
        low_bound = max(0, optimal_threshold - 0.3)
        high_bound = min(1, optimal_threshold + 0.3)
        
        # Create a dense array of thresholds with more points around the optimal threshold
        thresholds_around_optimal = np.linspace(
            optimal_threshold - 0.1, 
            optimal_threshold + 0.1, 
            15
        )
        
        thresholds_general = np.concatenate([
            np.linspace(low_bound, optimal_threshold - 0.1, 10),
            thresholds_around_optimal,
            np.linspace(optimal_threshold + 0.1, high_bound, 10)
        ])
        
        thresholds = np.sort(np.unique(np.round(thresholds_general, 4)))
        
        print(f"Evaluating {len(thresholds)} thresholds from {min(thresholds):.4f} to {max(thresholds):.4f}")
        
        # Run evaluation
        precisions, recalls, f1_scores, accuracies, tars, trrs, fars, frrs, y_pred_list = evaluate_metrics(thresholds, results_dir)
        
        # Plot metrics
        plot_metrics(thresholds, precisions, recalls, f1_scores, accuracies, tars, trrs, fars, frrs, results_dir)
        
        print("Evaluation complete! Check the results directory for all outputs.")
        
    except FileNotFoundError as e:
        print(f"Error: {e}")
        print("Please make sure you've generated the genuine_scores.csv and imposter_scores.csv files first.")
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()