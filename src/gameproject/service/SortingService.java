package gameproject.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service for sorting algorithms and their step-by-step visualization
 */
public class SortingService {
    
    /**
     * Represents a single step in a sorting algorithm
     */
    public static class SortStep {
        private final int[] array;
        private final int activeIndex;
        private final int compareIndex;
        private final String description;
        
        public SortStep(int[] array, int activeIndex, int compareIndex, String description) {
            this.array = array.clone();
            this.activeIndex = activeIndex;
            this.compareIndex = compareIndex;
            this.description = description;
        }
        
        public int[] getArray() {
            return array.clone();
        }
        
        public int getActiveIndex() {
            return activeIndex;
        }
        
        public int getCompareIndex() {
            return compareIndex;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Generate steps for insertion sort algorithm
     */
    public List<SortStep> insertionSort(int[] input) {
        List<SortStep> steps = new ArrayList<>();
        int[] array = input.clone();
        
        // Initial state
        steps.add(new SortStep(array, -1, -1, 
                "Starting the Insertion Sort algorithm."));
        
        for (int i = 1; i < array.length; i++) {
            int key = array[i];
            steps.add(new SortStep(array, i, -1, 
                    "Select element at index " + i + " with value " + key));
            
            int j = i - 1;
            while (j >= 0 && array[j] > key) {
                steps.add(new SortStep(array, i, j, 
                        "Compare " + key + " with " + array[j] + " at index " + j));
                
                array[j + 1] = array[j];
                j--;
                
                steps.add(new SortStep(array, i, j + 1, 
                        "Move " + array[j + 1] + " one position to the right"));
            }
            
            array[j + 1] = key;
            steps.add(new SortStep(array, j + 1, -1, 
                    "Place " + key + " at index " + (j + 1)));
        }
        
        steps.add(new SortStep(array, -1, -1, 
                "Insertion Sort complete! The array is now sorted."));
        
        return steps;
    }
    
    /**
     * Generate steps for merge sort algorithm
     */
    public List<SortStep> mergeSort(int[] input) {
        List<SortStep> steps = new ArrayList<>();
        int[] array = input.clone();
        
        // Initial state
        steps.add(new SortStep(array, -1, -1, 
                "Starting the Merge Sort algorithm."));
        
        mergeSortRecursive(array, 0, array.length - 1, steps);
        
        steps.add(new SortStep(array, -1, -1, 
                "Merge Sort complete! The array is now sorted."));
        
        return steps;
    }
    
    private void mergeSortRecursive(int[] array, int left, int right, List<SortStep> steps) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            
            steps.add(new SortStep(array, left, right, 
                    "Dividing array from index " + left + " to " + right));
            
            // Recursively sort left and right halves
            mergeSortRecursive(array, left, mid, steps);
            mergeSortRecursive(array, mid + 1, right, steps);
            
            // Merge the sorted halves
            merge(array, left, mid, right, steps);
        }
    }
    
    private void merge(int[] array, int left, int mid, int right, List<SortStep> steps) {
        steps.add(new SortStep(array, left, right, 
                "Merging subarrays from " + left + " to " + mid + 
                " and from " + (mid + 1) + " to " + right));
        
        // Sizes of two subarrays to be merged
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        // Create temp arrays
        int[] L = new int[n1];
        int[] R = new int[n2];
        
        // Copy data to temp arrays
        for (int i = 0; i < n1; ++i) {
            L[i] = array[left + i];
        }
        for (int j = 0; j < n2; ++j) {
            R[j] = array[mid + 1 + j];
        }
        
        // Merge the temp arrays
        int i = 0, j = 0;
        int k = left;
        while (i < n1 && j < n2) {
            steps.add(new SortStep(array, left + i, mid + 1 + j, 
                    "Compare " + L[i] + " with " + R[j]));
            
            if (L[i] <= R[j]) {
                array[k] = L[i];
                steps.add(new SortStep(array, k, -1, 
                        "Place " + L[i] + " at index " + k));
                i++;
            } else {
                array[k] = R[j];
                steps.add(new SortStep(array, k, -1, 
                        "Place " + R[j] + " at index " + k));
                j++;
            }
            k++;
        }
        
        // Copy remaining elements of L[]
        while (i < n1) {
            array[k] = L[i];
            steps.add(new SortStep(array, k, -1, 
                    "Place remaining " + L[i] + " at index " + k));
            i++;
            k++;
        }
        
        // Copy remaining elements of R[]
        while (j < n2) {
            array[k] = R[j];
            steps.add(new SortStep(array, k, -1, 
                    "Place remaining " + R[j] + " at index " + k));
            j++;
            k++;
        }
    }
    
    /**
     * Generate steps for TimSort algorithm
     * (TimSort is a hybrid sorting algorithm derived from merge sort and insertion sort)
     */
    public List<SortStep> timSort(int[] input) {
        List<SortStep> steps = new ArrayList<>();
        int[] array = input.clone();
        
        // Initial state
        steps.add(new SortStep(array, -1, -1, 
                "Starting the TimSort algorithm (hybrid of Insertion Sort and Merge Sort)."));
        
        final int RUN = 32; // Size of subarrays to be sorted using insertion sort
        
        // Sort individual subarrays of size RUN using insertion sort
        for (int i = 0; i < array.length; i += RUN) {
            int end = Math.min(i + RUN - 1, array.length - 1);
            insertionSortSubarray(array, i, end, steps);
        }
        
        // Start merging from size RUN (or the size of the last subarray)
        for (int size = RUN; size < array.length; size = 2 * size) {
            for (int left = 0; left < array.length; left += 2 * size) {
                int mid = left + size - 1;
                int right = Math.min(left + 2 * size - 1, array.length - 1);
                
                // Merge subarrays if mid is within bounds
                if (mid < right) {
                    merge(array, left, mid, right, steps);
                }
            }
        }
        
        steps.add(new SortStep(array, -1, -1, 
                "TimSort complete! The array is now sorted."));
        
        return steps;
    }
    
    private void insertionSortSubarray(int[] array, int left, int right, List<SortStep> steps) {
        steps.add(new SortStep(array, left, right, 
                "Using Insertion Sort for subarray from index " + left + " to " + right));
        
        for (int i = left + 1; i <= right; i++) {
            int key = array[i];
            int j = i - 1;
            
            steps.add(new SortStep(array, i, -1, 
                    "Select element at index " + i + " with value " + key));
            
            while (j >= left && array[j] > key) {
                steps.add(new SortStep(array, i, j, 
                        "Compare " + key + " with " + array[j]));
                
                array[j + 1] = array[j];
                j--;
                
                steps.add(new SortStep(array, j + 1, -1, 
                        "Shift element to the right"));
            }
            
            array[j + 1] = key;
            steps.add(new SortStep(array, j + 1, -1, 
                    "Place " + key + " at index " + (j + 1)));
        }
    }
}