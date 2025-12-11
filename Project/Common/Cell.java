package Project.Common;

import java.util.HashMap;
import java.util.Map;

import Project.Common.TextFX.Color;

public class Cell {
    private int row;
    private int col;
    private int status = 0; // 0 = untouched, 1 = hit, 2 = miss
    private HashMap<Long, Integer> ships; // ClientID and # of ships user has
    private int pointsForShips;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col; 
        this.ships = new HashMap<Long, Integer>();
    }

    public void placeShip(long clientId) {
        int value = 1;
        if(ships.containsKey(clientId))
        {
            value = ships.get(clientId);
            ships.put(clientId, value + 1);
        }
        else
        {
            ships.put(clientId, value);
        }
    }

    public void changeStatus(int status)
    {
        this.status = status;
    }

    public boolean attackShip() // need to finish 
    {
       // int points = 0;  // changed how this works
        int numOfShips = 0;
        if(getShip() > 0 && status == 0)
        {
            for (HashMap.Entry<Long, Integer> entry : ships.entrySet()) {
                numOfShips+=entry.getValue(); 
                pointsForShips = numOfShips;
                //LoggerUtil.INSTANCE.warning(TextFX.colorize("attackShip() called! Ships: " + numOfShips, Color.RED));
                entry.setValue(0); 
            }
            changeStatus(1);
            return true;
        }
        else 
        {
            //LoggerUtil.INSTANCE.warning(TextFX.colorize("attackShip() called! but status not 0 or ships is: " + numOfShips, Color.RED));
            changeStatus(1);
            return false; 
        }
    }

    public int getLastShips() { // used for when attacking and getting the ships that were there
        return pointsForShips;
    }

    public int getShip() {
        int numOfShips = 0;
        for (HashMap.Entry<Long, Integer> entry : ships.entrySet()) {
            numOfShips += entry.getValue();
        }
        return numOfShips; 
    }

    public boolean getUsersShips(long clientId) // returns true if user has ships and returns false if user doesnt have ships // TODO: need to finish
    {
        boolean hasShips = false; 
        int value = ships.get(clientId);
        if(ships.containsKey(clientId) && value == 0)
        {
            ships.get(clientId);
            //ships.put(clientId,1);
            hasShips = true;
        }
        else
        {
            //ships.put(clientId);
            hasShips = false;
        }
        return hasShips;
    }

    public int cellStatus() { // returns ships status
        return status;
    }
}