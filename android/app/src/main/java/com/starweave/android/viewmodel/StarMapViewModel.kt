package com.starweave.android.viewmodel

import androidx.lifecycle.ViewModel
import com.starweave.android.util.ConstellationData
import kotlinx.coroutines.flow.*

data class StarMapState(
    val inputText: String = "",
    val displayText: String = "",
    val showSparkle: Boolean = false,
    val healingQuote: String = ConstellationData.HEALING_TAGS.random().second
)

class StarMapViewModel : ViewModel() {
    private val _state = MutableStateFlow(StarMapState())
    val state: StateFlow<StarMapState> = _state.asStateFlow()

    private val presets = listOf(
        "在星河中漂流，捡拾别人的故事",
        "愿你被世界温柔以待",
        "所有失去的都会以另一种方式归来",
        "慢慢来，谁还没有一个未来呢",
        "你是我藏在心底的秘密",
        "星光不负赶路人",
        "万物皆有裂痕，那是光照进来的地方",
        "你要悄悄努力，然后惊艳所有人",
        "世界很大，幸福很小",
        "做自己的太阳，不必借谁的光"
    )

    fun updateInput(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun generate() {
        val text = _state.value.inputText.trim()
        if (text.isNotEmpty()) {
            _state.update { it.copy(displayText = text, showSparkle = true) }
        }
    }

    fun shuffle() {
        val random = presets.random()
        _state.update { it.copy(inputText = random, displayText = random, showSparkle = true) }
    }

    fun dismissSparkle() {
        _state.update { it.copy(showSparkle = false) }
    }

    fun rotateQuote() {
        _state.update { it.copy(healingQuote = ConstellationData.HEALING_TAGS.random().second) }
    }
}
