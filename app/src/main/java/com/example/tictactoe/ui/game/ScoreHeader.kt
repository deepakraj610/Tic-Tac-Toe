package com.example.tictactoe.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tictactoe.R
import com.example.tictactoe.domain.GameStatus
import com.example.tictactoe.domain.GameUiState
import com.example.tictactoe.domain.Player

/** Scoreboard (You / Draws / Computer) plus the turn or result indicator. */
@Composable
fun ScoreHeader(
    state: GameUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScoreItem(label = stringResource(R.string.you_label), value = state.score.player)
            ScoreItem(label = stringResource(R.string.draws_label), value = state.score.draws)
            ScoreItem(label = stringResource(R.string.computer_label), value = state.score.computer)
        }
        Text(
            text = stringResource(statusTextRes(state)),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .padding(vertical = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@Composable
private fun ScoreItem(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun statusTextRes(state: GameUiState): Int =
    when (val status = state.status) {
        is GameStatus.Won -> if (status.winner == Player.X) R.string.result_win else R.string.result_lose
        GameStatus.Draw -> R.string.result_draw
        GameStatus.InProgress ->
            if (state.isComputerThinking || state.currentTurn == Player.O) {
                R.string.turn_computer_thinking
            } else {
                R.string.turn_your
            }
    }
