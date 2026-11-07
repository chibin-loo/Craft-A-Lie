package com.artlu.craftalie.game;

public enum GamePhase {
    WAITING, // no game running
    GATHERING, // players are out finding items
    MEETING, // everyone's back at spawn showing items and discussing
    VOTING, // picking the imposter
    GUESSING, // imposter was caught and is guessing the word
}