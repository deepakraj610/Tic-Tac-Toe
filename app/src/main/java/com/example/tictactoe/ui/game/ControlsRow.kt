package com.example.tictactoe.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tictactoe.R

/** Restart Game and Reset Score buttons. */
@Composable
fun ControlsRow(
    onRestartGame: () -> Unit,
    onResetScore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onRestartGame,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.restart_game))
        }
        OutlinedButton(
            onClick = onResetScore,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.reset_score))
        }
    }
}
