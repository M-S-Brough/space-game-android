package com.mb.spacegame

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    val gameLiveModel = MutableLiveData<GameModel>()

    init {
        gameLiveModel.value = GameModel("Mark")
    }

    fun pauseGame() {
        gameLiveModel.value?.gameState = GameState.Paused
        gameLiveModel.postValue(gameLiveModel.value)
    }

    fun resumeGame() {
        gameLiveModel.value?.gameState = GameState.Running
        gameLiveModel.postValue(gameLiveModel.value)
    }
}

