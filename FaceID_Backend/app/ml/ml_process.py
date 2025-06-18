import cv2
import numpy as np
import re
import pytesseract
from pathlib import Path
import os

def process_image(image_path):
    """
    Process an image to extract a student roll number.
    
    Args:
        image_path (str): Path to the image file
        
    Returns:
        str or None: Roll number if found, None otherwise
    """
    extractor = RollNumberExtractor(save_debug_images=False)
    result = extractor.process_image(image_path)
    
    if result and result.get("roll_number"):
        return result["roll_number"]
    else:
        return None

class RollNumberExtractor:
    def __init__(self, output_dir="image_output", save_debug_images=False):
        # Create output directory if saving debug images
        self.save_debug_images = save_debug_images
        if save_debug_images:
            self.output_dir = Path(output_dir)
            self.output_dir.mkdir(exist_ok=True)
        
        # Valid roll number ranges
        self.valid_roll_prefixes = ['2101', '2201', '2301', '2401']
        self.roll_min = 2101000
        self.roll_max = 2401260
        
    def save_image(self, name, image):
        """Save image to output directory only if debug mode is enabled"""
        if not self.save_debug_images:
            return None
        
        output_path = self.output_dir / f"{name}"
        cv2.imwrite(str(output_path), image)
        return output_path
    
    def extract_text_regions(self, image):
        """Extract regions likely to contain text from the image with focus on ID card area"""
        # Convert to grayscale if not already
        if len(image.shape) == 3:
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        else:
            gray = image
            
        # Save original grayscale
        self.save_image("01_grayscale.jpg", gray)
        
        # Apply CLAHE for enhanced contrast
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        enhanced = clahe.apply(gray)
        self.save_image("02_enhanced.jpg", enhanced)
        
        # Edge detection to find text boundaries
        edges = cv2.Canny(enhanced, 50, 150)
        
        # Dilate edges to connect nearby text
        kernel = np.ones((3,3), np.uint8)
        dilated = cv2.dilate(edges, kernel, iterations=2)
        self.save_image("03_dilated_edges.jpg", dilated)
        
        # Find contours in the dilated image
        contours, _ = cv2.findContours(dilated, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        
        # Create a visualization image
        vis_img = cv2.cvtColor(gray, cv2.COLOR_GRAY2BGR) if len(image.shape) == 2 else image.copy()
        
        # Get image dimensions
        height, width = gray.shape
        min_area = height * width * 0.0005  # Minimum area for a text region
        max_area = height * width * 0.2     # Maximum area for a text region
        
        # Extract regions that might contain text - prioritize regions that look like ID cards
        text_regions = []
        
        # First pass - look specifically for ID card fields (Roll No. field is usually in a small rectangular area)
        for i, contour in enumerate(contours):
            x, y, w, h = cv2.boundingRect(contour)
            area = w * h
            
            # Filter by size
            if min_area < area < max_area:
                # Filter by aspect ratio - text lines usually wider than tall
                aspect_ratio = w / h
                if 1.5 < aspect_ratio < 10:  # More focused on typical ID field aspect ratios
                    padding = 5  # Reduced padding
                    x1 = max(0, x - padding)
                    y1 = max(0, y - padding)
                    x2 = min(width, x + w + padding)
                    y2 = min(height, y + h + padding)
                    
                    region = gray[y1:y2, x1:x2]
                    
                    # Quick check for "Roll" text or digits pattern before adding
                    region_enhanced = clahe.apply(region)
                    ocr_text = pytesseract.image_to_string(region_enhanced, config='--oem 3 --psm 6')
                    
                    # Check if this region likely contains a roll number
                    contains_roll_text = re.search(r'Roll|roll|no\.?|No\.?|\d{6,7}', ocr_text)
                    if contains_roll_text:
                        text_regions.append((region, (x1, y1, x2, y2)))
                        # Draw rectangle around this region
                        cv2.rectangle(vis_img, (x1, y1), (x2, y2), (0, 255, 0), 2)
                        cv2.putText(vis_img, f"{i}", (x1, y1-5), 
                                  cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 1)
        
        # If we didn't find good regions with quick check, fall back to more traditional approach
        # if len(text_regions) < 2:
        #     # Reset text_regions for second pass
        #     text_regions = []
            
        #     for i, contour in enumerate(contours):
        #         x, y, w, h = cv2.boundingRect(contour)
        #         area = w * h
                
        #         # Filter by size
        #         if min_area < area < max_area:
        #             aspect_ratio = w / h
        #             if 1 < aspect_ratio < 15:
        #                 padding = 5
        #                 x1 = max(0, x - padding)
        #                 y1 = max(0, y - padding)
        #                 x2 = min(width, x + w + padding)
        #                 y2 = min(height, y + h + padding)
                        
        #                 region = gray[y1:y2, x1:x2]
        #                 text_regions.append((region, (x1, y1, x2, y2)))
                        
        #                 # Draw rectangle around this region
        #                 cv2.rectangle(vis_img, (x1, y1), (x2, y2), (0, 255, 0), 2)
        
        # Save visualization
        self.save_image("04_text_regions.jpg", vis_img)
        
        return text_regions
    
    def is_valid_roll_number(self, roll_number):
        """Check if roll number is within valid ranges"""
        if not roll_number.isdigit():
            return False
            
        # Check length
        if len(roll_number) != 7:
            return False
            
        # Check if within valid ranges
        num = int(roll_number)
        if not (self.roll_min <= num <= self.roll_max):
            return False
            
        # Check prefixes
        prefix = roll_number[:4]
        if prefix not in self.valid_roll_prefixes:
            return False
            
        # Check last part is within range
        suffix = int(roll_number[4:])
        if not (0 <= suffix <= 260):
            return False
            
        return True
    
    def extract_roll_number_from_region(self, region, region_index):
        """Try to extract roll number from a specific region with reduced computations"""
        # Only use the most effective preprocessing techniques
        preprocessed = []
        
        # Original with CLAHE enhancement
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        enhanced = clahe.apply(region)
        preprocessed.append(("enhanced", enhanced))
        
        # Enhanced and scaled
        enhanced_scaled = cv2.resize(enhanced, None, fx=2, fy=2, interpolation=cv2.INTER_CUBIC)
        preprocessed.append(("enhanced_scaled", enhanced_scaled))
        
        # Most effective OCR configs
        configs = [
            '--oem 3 --psm 6',  # Assume a single uniform block of text
            '--oem 3 --psm 11 -c tessedit_char_whitelist=0123456789:',  # Sparse text, only digits and colon
        ]
        
        # Focused patterns for roll numbers
        roll_patterns = [
            r"Roll\s*No\.?\s*:?\s*(\d{7})",       # Roll No.: 2201085 (7 digits)
            r"(?<!\d)(\d{7})(?!\d)",             # Standalone 7-digit number
            r"\bRoll\b.*?:?\s*(\d{7})"           # Roll followed by exactly 7 digits
        ]
        
        results = []
        
        for name, proc_img in preprocessed:
            # Save the processed image
            self.save_image(f"region_{region_index}_{name}.jpg", proc_img)
            
            for config in configs:
                try:
                    # Perform OCR
                    ocr_text = pytesseract.image_to_string(proc_img, config=config)
                    
                    # Early exit: Try to directly find 7-digit numbers that match our range pattern
                    all_numbers = re.findall(r'\d{7}', ocr_text)
                    for num in all_numbers:
                        if self.is_valid_roll_number(num):
                            return [(num, 100, name, config, "direct_match")]  # High confidence for direct match
                    
                    # If no direct match, try pattern matching
                    for pattern in roll_patterns:
                        matches = re.search(pattern, ocr_text, re.IGNORECASE)
                        if matches:
                            roll_number = matches.group(1)
                            # Validate against allowed ranges
                            if self.is_valid_roll_number(roll_number):
                                results.append((roll_number, 90, name, config, pattern))
                                # Early exit on first valid match to save time
                                return results
                
                except Exception as e:
                    if self.save_debug_images:
                        print(f"Error with OCR on region {region_index}: {str(e)}")
        
        return results
    
    def process_image(self, image_path):
        """Process the image to extract roll number with optimization"""
        try:
            # Read the image
            img = cv2.imread(image_path)
            if img is None:
                raise ValueError(f"Could not read image at {image_path}")
            
            self.save_image("00_original.jpg", img)
            
            # Extract text regions
            regions = self.extract_text_regions(img)

            if(len(regions)<=0):
                return {"roll_number": None, "error": "No valid roll number found"}
            
            # Process each region
            all_results = []
            
            # First try fast processing with early exit
            for i, (region, coords) in enumerate(regions):
                results = self.extract_roll_number_from_region(region, i)
                if results:  # Early exit if we found a valid roll number
                    all_results.extend([(r[0], r[1], i, coords) + r[2:] for r in results])
                    break
            
            # Create visualization of best results
            if all_results:
                vis_img = img.copy()
                
                # Draw best result
                roll_number, confidence, region_idx, coords, *_ = all_results[0]
                x1, y1, x2, y2 = coords
                cv2.rectangle(vis_img, (x1, y1), (x2, y2), (0, 255, 0), 2)
                cv2.putText(vis_img, f"{roll_number}", (x1, y1-5), 
                           cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
                
                self.save_image("05_result.jpg", vis_img)
                
                # Return the best result
                if all_results:
                    best_result = all_results[0]
                    return {
                        "roll_number": best_result[0],
                        "confidence": best_result[1],
                        "region_index": best_result[2],
                        "coordinates": best_result[3]
                    }
            
            return {"roll_number": None, "error": "No valid roll number found"}
        
        except Exception as e:
            return {"roll_number": None, "error": str(e)}

# # Example of how this module would be used:
# if __name__ == "__main__":
#     image_path = "sample_id_card.jpg"
#     roll_number = process_image(image_path)
    
#     if roll_number:
#         print(f"Successfully extracted roll number: {roll_number}")
#     else:
#         print("Could not extract roll number.")