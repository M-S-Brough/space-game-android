package com.mb.spacegame

enum class GameState{
    Running, Paused
}

class GameModel(var name:String, var gameState: GameState = GameState.Running)

