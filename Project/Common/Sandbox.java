/*  package Project.Common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap; 

public class Sandbox {
    public static void main(String[] args) {
        // Create a grid with 5 rows and 5 columns
        Grid grid = new Grid(5, 5);

        // Print the initial state of the grid
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.println(grid.getCell(i, j));
            }
        }
        // place ships
        Cell cell = grid.getCell(2, 3);
        cell.placeShip(1);

        //attack ships
        cell.attackShip(1);

        Cell cell2 = grid.getCell(2, 3);
        
    }
}


class Grid {
    private Cell[][] cells;

    public Grid(int rows, int cols) {
        cells = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}

class Cell {
    private int row;
    private int col;
    private int status; // 0 = untouched, 1 = hit, 2 = miss
    private HashMap<Long, Integer> ships; // ClientID and # of ships user has

    public Cell(int row, int col) {
        this.row = row;
        this.col = col; // in case we need to reference the cell's position
        System.out.println("Created: " + this.toString());
    }

    public void placeShip(long clientId) {
        int value = ships.get(clientId);
        ships.put(clientId, value + 1);
    }

    public void attackShip(long clientId) {
        int value = ships.get(clientId);
        ships.put(clientId, 0); 
    }

    public void shipStatus(long clientId) {
        int value = ships.get(clientId);
        System.out.println("Client " + clientId + " has " + value + " ships in this cell.");
    }

    @Override
    public String toString() {
        return String.format(
                "Cell[%d][%d]: baseProb=%.2f, currentProb=%.2f, spawns=%d, totalFish=%d",
                row,
                col);
    }
}
*/
