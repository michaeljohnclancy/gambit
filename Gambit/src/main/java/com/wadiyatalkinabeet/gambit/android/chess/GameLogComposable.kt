package com.wadiyatalkinabeet.gambit.android.chess

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wadiyatalkinabeet.gambit.chess.GameLogViewModel

enum class RowStyle(val elevation: Dp, val alpha: Float) {
    PAST(2.dp, 1f),
    PRESENT(3.dp, 1f),
    FUTURE(0.dp, 0.33f)
}

@Composable
fun GameLogComposable(viewModel: GameLogViewModel) {
    val focusedMove by viewModel.focusedMove.observeAsState()
    Column() {
        Surface(
            elevation = 2.dp
        ) {
            BoardStateComposable(
                viewModel.gameLog.getStateAtMove(focusedMove!!)
            )
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxHeight()
        ) {
            MoveRow(viewModel, focusedMove!!, 0, "Game starts")

            viewModel.gameLog.moves.forEachIndexed { i, move ->
                if (i.mod(2) == 0)
                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colors.onBackground,
                        modifier = Modifier.alpha(0.25f)
                    )
                MoveRow(viewModel, focusedMove!!, i+1, move.toString())
            }
        }

    }
}

@Composable
fun MoveRow(
    viewModel: GameLogViewModel,
    selectedRow: Int,
    row: Int,
    description: String
) {
    val style = when(row) {
        in 0..selectedRow -> RowStyle.PAST
        selectedRow -> RowStyle.PRESENT
        else -> RowStyle.FUTURE
    }
    Surface(
        elevation = style.elevation,
        modifier = Modifier.clickable {
            viewModel.setMove(row)
        }.alpha(style.alpha)
    ) {
        Row(
            Modifier.padding(0.dp, 8.dp)
        ) {
            Text(
                text = row.toString(),
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .width(35.dp),
                textAlign = TextAlign.Right
            )
            Spacer(Modifier.width(20.dp))
            Text(
                text = description,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
fun PreviewGameLogComposable() {
    GameLogComposable(GameLogViewModel())
}
