package com.ahirst.doodaddash.model;

import java.util.List;

public class GameState {

    private String gamePin;
    private List<Player> players;

    private String currentObject;
    private int playerScore;

    public void setGamePin(String gamePin) {
        this.gamePin = gamePin;
    }

    public String getGamePin() {
        return gamePin;
    }

    public void setPlayerList(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    private void setCurrentObject(String currentObject) {
        this.currentObject = currentObject;
    }

    private String getCurrentObject() {
        return currentObject;
    }

}
