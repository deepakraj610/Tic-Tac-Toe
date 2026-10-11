package com.example.tictactoe.ui.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tictactoe.R
import com.example.tictactoe.domain.Difficulty

/** Easy / Unbeatable segmented toggle. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultySelector(
    selected: Difficulty,
    onDifficultyChanged: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options =
        listOf(
            Difficulty.EASY to R.string.difficulty_easy,
            Difficulty.UNBEATABLE to R.string.difficulty_unbeatable,
        )
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.difficulty_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, (difficulty, labelRes) ->
                SegmentedButton(
                    selected = difficulty == selected,
                    onClick = { if (difficulty != selected) onDifficultyChanged(difficulty) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                ) {
                    Text(text = stringResource(labelRes))
                }
            }
        }
    }
}
