package Project.Common;

public class User {
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private String clientName;
    private boolean isReady = false;
    private boolean tookTurn = false;
    private boolean isEliminated = false; // for milestone3 I think?
    private final int maxShips = 5;
    private int placedShips = 0; 
    private int gamePoints = 0; // correct way?

    
  // protected boolean isEliminated() //yaw4 added 
   // {
        //return this.user.isEliminated();
   // }

   public boolean isSpectator()
   {
        return !isReady();
   }

   public int getGamePoints()
   {
        return gamePoints; 
   }

   public void addGamePoints(int points) // is this correct?
   {
        this.gamePoints += points; 
   }

    public void setPlacedShip() // increases placed ship when ship gets placed yaw4
    {
        placedShips++;
    }

    public boolean placedAllShips() // checks to see if placed ships are equal to max allowed ships yaw4
    {
        return placedShips >= maxShips;
    }

    /**
     * @return the clientId
     */
    public long getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the username
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param username the username to set
     */
    public void setClientName(String username) {
        this.clientName = username;
    }

    public String getDisplayName() {
        return String.format("%s#%s", this.clientName, this.clientId);
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void reset() {
        this.clientId = Constants.DEFAULT_CLIENT_ID;
        this.clientName = null;
        this.isReady = false;
        this.tookTurn = false;
    }

    /**
     * @return the tookTurn
     */
    public boolean didTakeTurn() {
        return tookTurn;
    }

    /**
     * @param tookTurn the tookTurn to set
     */
    public void setTookTurn(boolean tookTurn) {
        this.tookTurn = tookTurn;
    }
}