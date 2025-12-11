package Project.Common;

public class Grid {
    private Cell[][] cells;

        public void generate(int rows, int cols, boolean isServer) {
        cells = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }        
    }
    public boolean isValidCoordinate(int row, int col) 
    {
        return cells != null && row >= 0 && col >= 0 && row < cells.length && col < cells[0].length;
    }

    public Cell getCell(int row, int col)
    {
        return cells[row][col];
    }

    public int getLastShips(int x, int y) // used for when attacking and getting the ships that were there
    {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid grid coordinates");
        }
        Cell cell = cells[x][y];
        if (cell == null) {
            throw new IllegalStateException("Cell is not initialized");
        }
        return cell.getLastShips();
    }

    public int getShip(int x, int y)
    {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid grid coordinates");
        }
        Cell cell = cells[x][y];
        if (cell == null) {
            throw new IllegalStateException("Cell is not initialized");
        }
        return cell.getShip();
    }

    public int cellStatus(int x, int y)
    {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid grid coordinates");
        }
        Cell cell = cells[x][y];
        if (cell == null) {
            throw new IllegalStateException("Cell is not initialized");
        }
        return cell.cellStatus();
    }

    public void placeShip(int x, int y, long clientId) // for placing ship
    {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid grid coordinates");
        }
        Cell cell = cells[x][y];
        if (cell == null) {
            throw new IllegalStateException("Cell is not initialized");
        }
        cell.placeShip(clientId);
    }

    public boolean attackShip(int x, int y) // for attacking ship
    {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid grid coordinates");
        }
        Cell cell = cells[x][y];
        if (cell == null) {
            throw new IllegalStateException("Cell is not initialized");
        }
        if(cell.attackShip())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void reset() 
    {
        if (cells == null)
            return;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] != null) {
                    cells[i][j] = null;
                }
            }
        }
        cells = null;
    }

    //ships remaining function
    public boolean shipsRemaining(int rows, int cols, long clientId)
    {
        boolean shipsLeft = false;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(cells[i][j].getUsersShips(clientId))
                    {
                        shipsLeft = true;
                    }
                    else 
                    {
                        shipsLeft = false;
                    }
            }
        }
        return shipsLeft;
    }


    @Override
    public String toString() {
        if (cells == null) {
            return "Grid is not initialized.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                int count = cells[i][j].getShip();
                String cellValue = String.format("[%s]", count > 0 ? count : "X", cells[i][j].cellStatus() == 0 ? count : "0" );
                sb.append(cellValue);// .append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
