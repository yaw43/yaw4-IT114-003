package Project.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.TimedEvent;
import Project.Common.TimerType;
import Project.Exceptions.MissingCurrentPlayerException;
import Project.Exceptions.NotPlayersTurnException;
import Project.Exceptions.NotReadyException;
import Project.Exceptions.PhaseMismatchException;
import Project.Exceptions.PlayerNotFoundException;
import Project.Common.Grid;
import Project.Common.TextFX;
import Project.Common.TextFX.Color;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;
    private List<ServerThread> turnOrder = new ArrayList<>();
    private long currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
    private int round = 0;
    private Grid grid = new Grid(); // yaw4 12/11, used to init grid on server

    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerThread sp) {
        // sync GameRoom state to new client

        syncCurrentPhase(sp); 
        // sync only what's necessary for the specific phase
        // if you blindly sync everything, you'll get visual artifacts/discrepancies
        syncReadyStatus(sp);
        syncTurnStatus(sp);
        if (currentPhase != Phase.READY) {
                                // turn/ready use the same visual process so ensure turn status is only called
                                // outside of ready phase
            syncPlayerPoints(sp);
        }

    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerThread sp) {
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + clientsInRoom.size());
        long removedClient = sp.getClientId();
        turnOrder.removeIf(player -> player.getClientId() == sp.getClientId());
        if (clientsInRoom.isEmpty()) {
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        } else if (removedClient == currentTurnClientId) {
            onTurnStart();
        }
    }

    // timer handlers
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> {
            System.out.println("Round Time: " + time);
            sendCurrentTime(TimerType.ROUND, time);
        });
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
            sendCurrentTime(TimerType.ROUND, -1);
        }
    }

    private void startTurnTimer() {
        turnTimer = new TimedEvent(30, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> {
            System.out.println("Turn Time: " + time);
            sendCurrentTime(TimerType.TURN, time);
        });
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
            sendCurrentTime(TimerType.TURN, -1);
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.PLACE);

        LoggerUtil.INSTANCE.info("onSessionStart() attack stuff");
        currentTurnClientId = Constants.DEFAULT_CLIENT_ID; // added for phase based turn
        setTurnOrder();

        round = 0;
        grid.generate(5,5, true); // yaw4 12/10 used to generate grid and start turn order and game logic when game starts
        LoggerUtil.INSTANCE.info(TextFX.colorize("Grid generated: " + grid, Color.PURPLE));
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        
        if(currentPhase == Phase.PLACE && round >= 1) // yaw4 12/10 checks to see if phase is place and round is 1 or higher, 
        {                                               // it then changes the phase to attack and if not it just increases round and adjusts timer/status
            resetRoundTimer();
            resetTurnStatus();
            LoggerUtil.INSTANCE.info("Changing phase to attack.");
            changePhase(Phase.ATTACK);
        }
        else
        {
            LoggerUtil.INSTANCE.info("OnRoundStart not attack stuff");
            resetRoundTimer();
            resetTurnStatus();
            round++;
        }
        // relay(null, String.format("Round %d has started", round));
        sendGameEvent(String.format("Round %d has started", round));
        // startRoundTimer(); Round timers aren't needed for turns
        // if you do decide to use it, ensure it's reasonable and based on the number of
        // players
        LoggerUtil.INSTANCE.info("onRoundStart() end");
        onTurnStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        if(currentPhase == Phase.ATTACK) // yaw4 12/10 checks to see if phase is attack, it then uses round robin to select next player
        {
            LoggerUtil.INSTANCE.info("onTurnStart attack stuff");
        try {
            ServerThread currentPlayer = getNextPlayer();
            // relay(null, String.format("It's %s's turn", currentPlayer.getDisplayName()));
            sendGameEvent(String.format("It's %s's turn", currentPlayer.getDisplayName()));
        } catch (MissingCurrentPlayerException | PlayerNotFoundException e) {

            e.printStackTrace();
            }
        }
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring
        if(currentPhase == Phase.ATTACK) //yaw4 12/10 checks to see if phase is "ATTACK" then looks to see if round should end or do next turn
        {
        try {
            // optionally can use checkAllTookTurn();
                LoggerUtil.INSTANCE.info("onTurnEnd attack stuff");
            if (isLastPlayer()) {
                // if the current player is the last player in the turn order, end the round
                onRoundEnd();
            } else {
                onTurnStart();
            }
        } catch (MissingCurrentPlayerException | PlayerNotFoundException e) {

            e.printStackTrace();
            }
            
        }
        else
        {
            onRoundEnd();
            LoggerUtil.INSTANCE.info("onTurnEnd() ending placing phase");
        }
        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer(); // reset timer if round ended without the time expiring

        LoggerUtil.INSTANCE.info(TextFX.colorize("Grid status: " + grid, Color.PURPLE));  // yaw4 12/11, resets the round timer and then 
        LoggerUtil.INSTANCE.info("onRoundEnd() end");                            // shows status of grid on server
        if (round >= 3) {
            onSessionEnd();
        } else {
            onRoundStart();
        }
       onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        turnOrder.clear();
        currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
        resetReadyStatus(); // yaw4 12/11, resets ready,turn status and turn timer
        resetTurnStatus(); // it then changes phase back to ready
        resetTurnTimer();
        
        grid.reset(); // added for yaw4 resets grid on server 

        changePhase(Phase.READY);

        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // send/sync data to ServerThread(s)
    private void sendResetTurnStatus() {
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !spInRoom.sendResetTurnStatus();
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    private void sendTurnStatus(ServerThread client, boolean tookTurn) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendTurnStatus(client.getClientId(), client.didTakeTurn());
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

      // send/sync data to ServerThread(s) added for syncing points
     private void syncPlayerPoints(ServerThread incomingClient) {
        clientsInRoom.values().forEach(serverUser -> {
            if (serverUser.getClientId() != incomingClient.getClientId()) {
                boolean failedToSync = !incomingClient.sendPlayerPoints(serverUser.getClientId(),
                        serverUser.getPoints());
                if (failedToSync) {
                    LoggerUtil.INSTANCE.warning(
                            String.format("Removing disconnected %s from list", serverUser.getDisplayName()));
                    disconnect(serverUser);
                }
            }
        });
    }

    private void sendPlayerPoints(ServerThread sp) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendPlayerPoints(sp.getClientId(), sp.getPoints());
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    private void syncTurnStatus(ServerThread incomingClient) {
        clientsInRoom.values().forEach(serverUser -> {
            if (serverUser.getClientId() != incomingClient.getClientId()) {
                boolean failedToSync = !incomingClient.sendTurnStatus(serverUser.getClientId(),
                        serverUser.didTakeTurn(), true);
                if (failedToSync) {
                    LoggerUtil.INSTANCE.warning(
                            String.format("Removing disconnected %s from list", serverUser.getDisplayName()));
                    disconnect(serverUser);
                }
            }
        });
    }

    private void sendPlaceShipUpdate(ServerThread client, int x, int y) // used to send data of placing ship to client
    {
        boolean failedToSend = !client.sendPlaceShipUpdate(client.getClientId(), x, y);
        if (failedToSend) {
                removeClient(client);
        }
    }

    // end send data to ServerThread(s)

    // misc methods
    private void resetTurnStatus() {
        clientsInRoom.values().forEach(sp -> {
            sp.setTookTurn(false);
        });
        sendResetTurnStatus();
    }

    /**
     * Sets `turnOrder` to a shuffled list of players who are ready.
     */
    private void setTurnOrder() {
        turnOrder.clear();
        turnOrder = clientsInRoom.values().stream().filter(ServerThread::isReady).collect(Collectors.toList());
        Collections.shuffle(turnOrder);
    }

    /**
     * Gets the current player based on the `currentTurnClientId`.
     * 
     * @return
     * @throws MissingCurrentPlayerException
     * @throws PlayerNotFoundException
     */
    private ServerThread getCurrentPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        // quick early exit
        if (currentTurnClientId == Constants.DEFAULT_CLIENT_ID) {
            throw new MissingCurrentPlayerException("Current Player not set");
        }
        return turnOrder.stream()
                .filter(sp -> sp.getClientId() == currentTurnClientId)
                .findFirst()
                // this shouldn't occur but is included as a "just in case"
                .orElseThrow(() -> new PlayerNotFoundException("Current player not found in turn order"));
    }

    /**
     * Gets the next player in the turn order.
     * If the current player is the last in the turn order, it wraps around
     * (round-robin).
     * 
     * @return
     * @throws MissingCurrentPlayerException
     * @throws PlayerNotFoundException
     */
    private ServerThread getNextPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        int index = 0;
        if (currentTurnClientId != Constants.DEFAULT_CLIENT_ID) {
            index = turnOrder.indexOf(getCurrentPlayer()) + 1;
            if (index >= turnOrder.size()) {
                index = 0;
            }
        }
        ServerThread nextPlayer = turnOrder.get(index);
        currentTurnClientId = nextPlayer.getClientId();
        return nextPlayer;
    }

    /**
     * Checks if the current player is the last player in the turn order.
     * 
     * @return
     * @throws MissingCurrentPlayerException
     * @throws PlayerNotFoundException
     */
    private boolean isLastPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        // check if the current player is the last player in the turn order
        return turnOrder.indexOf(getCurrentPlayer()) == (turnOrder.size() - 1);
    }

    private void checkTookTurn(ServerThread currentUser) throws NotPlayersTurnException {  // adding here for phase place
        if (currentUser.didTakeTurn()) {
            throw new NotPlayersTurnException("You have already taken your turn this round");
        }
    }

    private void checkCoordinateBounds(int x, int y) 
    {

    }  // adding here for phase place

    private void checkAllTookTurn() {
        int numReady = clientsInRoom.values().stream()
                .filter(sp -> sp.isReady())
                .toList().size();
        int numTookTurn = clientsInRoom.values().stream()
                // ensure to verify the isReady part since it's against the original list
                .filter(sp -> sp.isReady() && sp.didTakeTurn())
                .toList().size();
        if (numReady == numTookTurn) {
            // relay(null,
            // String.format("All players have taken their turn (%d/%d) ending the round",
            // numTookTurn, numReady));
            sendGameEvent(
                    String.format("All players have taken their turn (%d/%d) ending the round", numTookTurn, numReady));
            onRoundEnd();
        }
    }

    // start check methods
    private void checkCurrentPlayer(long clientId) throws NotPlayersTurnException {
        if (currentTurnClientId != clientId) {
            throw new NotPlayersTurnException("You are not the current player");
        }
    }

    // end check methods

    // receive data from ServerThread (GameRoom specific)
    protected void handleSkipAction(ServerThread currentUser) // yaw4 12/11, called to skip on serverside, called by serverThread
    {
        try
        {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.ATTACK);
            checkIsReady(currentUser);
            checkTookTurn(currentUser);

            currentUser.setTookTurn(true);
            sendTurnStatus(currentUser, currentUser.didTakeTurn());
            onTurnEnd();
        }
        catch (NotPlayersTurnException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            LoggerUtil.INSTANCE.severe("handleSkipAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handleSkipAction exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handleSkipAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "You can only skip during the ATTACK phase");
            LoggerUtil.INSTANCE.severe("handleSkipAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleSkipAction exception", e);
        }
    }

    //attempting attack action code yaw4 
    protected void handleAttackAction(ServerThread currentUser, int x, int y) // yaw4 12/11, called to attack ship on serverside grid and called by serverThread
    {
        try
        {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.ATTACK);
            checkIsReady(currentUser);
            checkCurrentPlayer(currentUser.getClientId());
            checkTookTurn(currentUser);
            checkCoordinateBounds(x, y); 

            if (currentUser.didTakeTurn()) {
                currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have already attacked this round.");
                return;
            }
            else
            {
              if(grid.attackShip(x,y) && grid.cellStatus(x,y) == 1) // yaw4 12/11, used to attack ship in grid when attack command
                {
                    currentUser.addGamePoints(grid.getLastShips(x, y));
                    currentUser.sendAttackShipUpdate(currentUser.getClientId(), x, y); // sends attack command to client
                    relay(null, String.format("%s hit " + grid.getLastShips(x,y) + " ships!", currentUser.getDisplayName()));
                    LoggerUtil.INSTANCE.warning("ship successfully attacked and user's points now: " + currentUser.getPoints() + " Client ID:" + currentUser.getClientId());
                }
                else 
                {
                    relay(null, String.format("%s missed and hit " + grid.getLastShips(x,y) + " ships!", currentUser.getDisplayName()));
                    LoggerUtil.INSTANCE.info("ship failed attack and user's points now " + currentUser.getPoints());
                } // yaw4 attack ship logic to be added here
            }
            if(currentUser.getPoints() >= 6)
            {
                onSessionEnd();
                //resetTurnTimer();
            }
            currentUser.setTookTurn(true);
            sendTurnStatus(currentUser, currentUser.didTakeTurn());
            onTurnEnd();
        }
        catch (NotPlayersTurnException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            LoggerUtil.INSTANCE.severe("handleAttackAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handleAttackAction exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handleAttackAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "You can only attack during the ATTACK phase");
            LoggerUtil.INSTANCE.severe("handleAttackAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleAttackAction exception", e);
        }
    }

    // attempting place action code yaw4
    protected void handlePlaceAction(ServerThread currentUser, int x, int y) // yaw4 12/11, called to place ship on serverside grid and called by serverThread
    {
        try
        {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.PLACE);
            checkIsReady(currentUser);
            checkCoordinateBounds(x, y); 
            
            currentUser.setPlacedShip();

            if (currentUser.didTakeTurn() && currentUser.placedAllShips()) {
                currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have placed your ships this game");
                return;
            }
            else
            {
                grid.placeShip(x, y, currentTurnClientId); // yaw4 12/11, used to place ship on grid 
                currentUser.sendPlaceShipUpdate(currentUser.getClientId(), x, y);
            }
            if(currentUser.placedAllShips()) // checks to see if user has placed all ships before setting turn true yaw4
            {
                currentUser.setTookTurn(true);
                sendTurnStatus(currentUser, currentUser.didTakeTurn());
            }
            checkAllTookTurn();
        }
        catch (NotPlayersTurnException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            LoggerUtil.INSTANCE.severe("handlePlaceAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handlePlaceAction exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handlePlaceAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "You can only place during the PLACE phase");
            LoggerUtil.INSTANCE.severe("handlePlaceAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handlePlaceAction exception", e);
        }
    }

    /**
     * Handles the turn action from the client.
     * 
     * @param currentUser
     * @param exampleText (arbitrary text from the client, can be used for
     *                    additional actions or information)
     */
    protected void handleTurnAction(ServerThread currentUser, String exampleText) {
        // check if the client is in the room
        try {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.IN_PROGRESS);
            checkCurrentPlayer(currentUser.getClientId());
            checkIsReady(currentUser);
            if (currentUser.didTakeTurn()) {
                currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have already taken your turn this round");
                return;
            }
            // example points
            int points = new Random().nextInt(4) == 3 ? 1 : 0;
            sendGameEvent(String.format("%s %s", currentUser.getDisplayName(),
                    points > 0 ? "gained a point" : "didn't gain a point"));
            if (points > 0) {
                currentUser.changePoints(points);
                sendPlayerPoints(currentUser);
            }
            currentUser.setTookTurn(true);
            // TODO handle example text possibly or other turn related intention from client
            sendTurnStatus(currentUser, currentUser.didTakeTurn());
            // finished processing the turn
            onTurnEnd();
        } catch (NotPlayersTurnException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "You can only take a turn during the IN_PROGRESS phase");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        }
    }

    // end receive data from ServerThread (GameRoom specific)
}