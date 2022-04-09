import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class SudokuSolver {

    int[][] sudoku;
    ArrayList<ArrayList<ArrayList<Integer>>> possibleCombinations;
    int length = 9, breadth = 9, box = 3;
    boolean isUpdated = false;
    boolean isComplete = true;
    boolean isValid = true;

    public SudokuSolver(int[][] sudoku)
    {
        this.sudoku = Arrays.stream(sudoku).map(int[]::clone).toArray(int[][]::new);
        this.possibleCombinations = populatePossibleCombinations();
    }

    public ArrayList<ArrayList<ArrayList<Integer>>> populatePossibleCombinations() {
        ArrayList<ArrayList<ArrayList<Integer>>> possibleCombinations = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            // for each ith add ArrayList;
            ArrayList<ArrayList<Integer>> jthList = new ArrayList<>();
            possibleCombinations.add(jthList);
            for (int j = 0; j < breadth; j++) {
                ArrayList<Integer> elements = new ArrayList<>();
                jthList.add(elements);
                if (sudoku[i][j] == 0) { // element does exists here
                    for (int k = 1; k <= 9; k++) {
                        elements.add(k);
                    }
                } else {
                    elements.add(sudoku[i][j]);
                }
            }
        }
        return possibleCombinations;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i % box == 0)
                stringBuilder.append("\n");
            for (int j = 0; j < breadth; j++) {
                if (j % box == 0)
                    stringBuilder.append("  ");
                stringBuilder.append(String.format("%3d", sudoku[i][j]));
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }
    
    public void findPossibleElement() {
        do {
            isUpdated = false;
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < breadth; j++) {
                    if (sudoku[i][j] != 0) {
                        removeElementFromLine(i, sudoku[i][j]);
                        removeElementFromColumn(j, sudoku[i][j]);
                        removeElementFromBox(i, j, sudoku[i][j]);
                    }
                }
            }
            fillSudoku();
        }while (isUpdated);
    }

    private void removeElementFromLine(int rowIndex, int ele) {
        for (int j = 0; j < breadth; j++) {
            if (sudoku[rowIndex][j] == 0) {
                possibleCombinations.get(rowIndex).get(j).remove(Integer.valueOf(ele));
            }
        }
    }
    
    private void removeElementFromColumn(int columnIndex, int ele) {
        for (int i = 0; i < length; i++) {
            if (sudoku[i][columnIndex] == 0) {
                possibleCombinations.get(i).get(columnIndex).remove(Integer.valueOf(ele));
            }
        }
    }

    private void removeElementFromBox(int rowIndex, int columnIndex, int ele) {
        int[] startIndex = { 0, 0, 0, 3, 3, 3, 6, 6, 6 };
        int[] endIndex = { 3, 3, 3, 6, 6, 6, 9, 9, 9 };

        for (int i = startIndex[rowIndex]; i < endIndex[rowIndex]; i++) {
            for (int j = startIndex[columnIndex]; j < endIndex[columnIndex]; j++) {
                if (sudoku[i][j] == 0) {
                    possibleCombinations.get(i).get(j).remove(Integer.valueOf(ele));
                }
            }
        }
    }
    
    public void fillSudoku() {
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < breadth; j++) {
                if (sudoku[i][j] == 0 && possibleCombinations.get(i).get(j).size() == 1) {
                    sudoku[i][j] = possibleCombinations.get(i).get(j).get(0);
                    isUpdated = true;
                }
            }
        }
    }

    private Queue<SudokuSolver> populateQueue(SudokuSolver parent, Queue<SudokuSolver> sudokuSolverQueue, HashSet<SudokuSolver> set){
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < breadth; j++) {
                if (parent.sudoku[i][j] == 0) {
                    int elementsCount = parent.possibleCombinations.get(i).get(j).size();
                    for (int k = 0; k < elementsCount; k++) {
                        int elementToAdd = parent.possibleCombinations.get(i).get(j).get(k);

                        SudokuSolver temp = new SudokuSolver(parent.sudoku);
                        temp.sudoku[i][j] = elementToAdd;
                        temp.possibleCombinations.get(i).set(j, new ArrayList<>(Arrays.asList(elementToAdd)));
                        boolean addToQueue = set.add(temp);
                        if (addToQueue)
                            sudokuSolverQueue.offer(temp);
                    }
                }
            }
        }
        return sudokuSolverQueue;
    }

    public void backTracking() {
        Queue<SudokuSolver> sudokuSolverQueue = new LinkedList<>();
        HashSet<SudokuSolver> sudokuSolverSet = new HashSet<>();
        populateQueue(this, sudokuSolverQueue, sudokuSolverSet);
        
        while (!sudokuSolverQueue.isEmpty()) {
            SudokuSolver temp = sudokuSolverQueue.poll();
            sudokuSolverSet.remove(temp);
            temp.findPossibleElement();
            boolean tempIsValid = temp.isValidSudoku();
            // this is the answer
            if (tempIsValid && temp.isComplete) {
                this.sudoku = temp.sudoku;
                this.possibleCombinations = temp.possibleCombinations;
                return;
            }
            // this could be possible answer still all the elements are not filled
            else if (tempIsValid && !temp.isComplete) {
                populateQueue(temp, sudokuSolverQueue, sudokuSolverSet); 
            }
        }
    }
    
    public boolean isValidSudoku() {
        isComplete = true;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < breadth; j++) {
                if (sudoku[i][j] == 0)
                    isComplete = false;
                if (sudoku[i][j] != 0 &&
                        !(isValidAcrossLine(sudoku[i][j], i, j) &&
                          isValidAcrossColumn(sudoku[i][j], i, j) &&
                          isValidAcrossBox(sudoku[i][j], i, j)))
                    return false;
            }
        }
        return true;
    }
    
    private boolean isValidAcrossLine(int ele, int rowIndex, int columnIndex) {
        for (int j = columnIndex + 1; j < breadth; j++) {
            if (sudoku[rowIndex][j] == ele) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isValidAcrossColumn(int ele, int rowIndex, int columnIndex) {
        for (int i = rowIndex + 1; i < length; i++) {
            if (sudoku[i][columnIndex] == ele) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isValidAcrossBox(int ele, int rowIndex, int columnIndex) {
        int[] startIndex = { 0, 0, 0, 3, 3, 3, 6, 6, 6 };
        int[] endIndex = { 3, 3, 3, 6, 6, 6, 9, 9, 9 };

        for (int i = startIndex[rowIndex]; i < endIndex[rowIndex]; i++) {
            for (int j = startIndex[columnIndex]; j < endIndex[columnIndex]; j++) {
                if (i != rowIndex && j != columnIndex && sudoku[i][j] == ele) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[][] generateSudoku() {
        int[][] sudoku = {
                { 9, 0, 0, 8, 3, 0, 1, 5, 7 },
                { 5, 0, 3, 1, 0, 6, 2, 8, 0 },
                { 1, 0, 0, 7, 4, 0, 0, 9, 0 },
                { 0, 0, 0, 0, 5, 0, 8, 3, 0 },
                { 3, 0, 1, 0, 0, 4, 6, 7, 2 },
                { 2, 0, 0, 0, 1, 3, 0, 0, 9 },
                { 0, 0, 2, 0, 7, 0, 0, 1, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 6, 0 },
                { 0, 3, 4, 0, 6, 0, 9, 2, 0 }
        };
        return sudoku;
    }
    
    public static int[][] generateSudoku2() {
        //https://sudoku.com/hard/
        int[] input = { 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 5, 0, 7, 0, 6, 0, 1, 0, 0, 0, 0, 9, 8, 0, 0, 4, 0, 3,
                0, 0, 0, 2, 0, 2, 0, 6, 0, 9, 3, 8, 0, 1, 0, 0, 0, 2, 7, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 0, 0, 0, 4,
                9, 0, 5, 1, 0, 0, 0, 0, 0, 0, 3, 0, 2, 0 };
        
        int[][] sudoku = new int[9][9];
        int row = 0, column = 0, counter = 0;
        while (counter < input.length) {
            sudoku[row][column++] = input[counter++];
            if (counter % 9 == 0) {
                row++;
                column = 0;
            }
        }
        return sudoku;
    }

    public static void main(String[] args) {
        System.out.println("Sudoku Starts");
        SudokuSolver sudokuSolver = new SudokuSolver(SudokuSolver.generateSudoku2());
        sudokuSolver.findPossibleElement();
        System.out.println(sudokuSolver);
        boolean isValidSudoku = sudokuSolver.isValidSudoku();
        System.out.println("\nSudoku is valid ? "+isValidSudoku);
        System.out.println("Sudoku is complete ? " + sudokuSolver.isComplete);
        if (isValidSudoku && !sudokuSolver.isComplete) {
            System.out.println("Seems we need to do backtracking now!!");
            sudokuSolver.backTracking();
            System.out.println(sudokuSolver);
            isValidSudoku = sudokuSolver.isValidSudoku();
            System.out.println("\nSudoku is valid ? "+isValidSudoku);
            System.out.println("Sudoku is complete ? " + sudokuSolver.isComplete);
        }
        else {
            System.out.println("Not correct");
        }
     }
    
}
