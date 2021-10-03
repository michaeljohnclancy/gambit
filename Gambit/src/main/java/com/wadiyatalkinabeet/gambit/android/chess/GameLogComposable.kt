package com.wadiyatalkinabeet.gambit.android.chess

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FirstPage
import androidx.compose.material.icons.rounded.LastPage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.wadiyatalkinabeet.gambit.chess.GameLogViewModel
import kotlinx.coroutines.launch
import kotlin.math.round

@Composable
fun GameLogComposable(viewModel: GameLogViewModel) {
    val focusedMove by viewModel.focusedMove.observeAsState()
    val scrollState = rememberScrollState()

    val scrollCoroutine = rememberCoroutineScope()
    fun setMoveWithScroll(moveID: Int) {
        viewModel.setMove(moveID)

        fun movePos(id: Int) = id * 106
        val minScroll = movePos(moveID - 3)
        val maxScroll = movePos(moveID - 1)
        if (scrollState.value < minScroll) {
            scrollCoroutine.launch {
                scrollState.animateScrollTo(minScroll)
            }
        } else if (scrollState.value > maxScroll) {
            scrollCoroutine.launch {
                scrollState.animateScrollTo(maxScroll)
            }
        }
    }

    val boardElev by animateDpAsState(
        if (scrollState.value == 0) 3.dp
        else 8.dp
    )

    val controlsElev by animateDpAsState(
        if (scrollState.value == scrollState.maxValue) 3.dp
        else 8.dp
    )

    Column() {
        // Game Board
        Surface(
            elevation = boardElev,
            color = MaterialTheme.colors.surface,
            modifier = Modifier.zIndex(1f),
        ) {
            BoardStateComposable(
                viewModel.gameLog.getStateAtMove(focusedMove!!)
            )
        }

        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            // Move List
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxHeight(),
            ) {
                Spacer(Modifier.height(2.dp))

                MoveRow(viewModel, focusedMove!!, 0, "Game starts")
                viewModel.gameLog.moves.forEachIndexed{ i, move ->
                    if (i.mod(2) == 0)
                        Spacer(
                            Modifier.height(1.dp)
                        )
                    MoveRow(viewModel, focusedMove!!, i+1, move.toString())
                }

                Spacer(Modifier.height(125.dp))
            }

            // Controls
            Surface(
//                elevation = controlsElev,
                color = MaterialTheme.colors.surface,
                modifier = Modifier
                    .zIndex(1f)
                    .alpha(0.85f),
            ) {
                Column() {
                    Divider(thickness = 1.dp)

                    Slider(
                        modifier = Modifier.padding(20.dp, 4.dp),
                        value = focusedMove!!.toFloat(),
                        valueRange = 0f.rangeTo(viewModel.gameLog.moves.size.toFloat()),
                        steps = viewModel.gameLog.moves.size - 1,
                        onValueChange = {
                            setMoveWithScroll(round(it).toInt())
                        },
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MoveButton(this, ::setMoveWithScroll,
                            1f, Icons.Rounded.FirstPage,
                            "Jump to start", 0
                        )
                        MoveButton( this, ::setMoveWithScroll,
                            2f, Icons.Rounded.ChevronLeft,
                            "Previous move", focusedMove!! - 1
                        )
                        MoveButton(this, ::setMoveWithScroll,
                            2f, Icons.Rounded.ChevronRight,
                            "Next move",
                            focusedMove!! + 1
                        )
                        MoveButton(this, ::setMoveWithScroll,
                            1f, Icons.Rounded.LastPage,
                            "Jump to end", viewModel.gameLog.moves.size
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun MoveButton(
    row: RowScope,
    setMove: (Int) -> Unit,
    weight: Float,
    icon: ImageVector,
    description: String,
    moveID: Int
) {
    with(row) {
        OutlinedButton(
            onClick = { setMove(moveID) },
            modifier = Modifier
                .weight(weight)
                .padding(8.dp)
                .height(50.dp)
        ) {
            Icon(icon, description)
        }
    }
}

enum class RowState {
    PAST, PRESENT, FUTURE
}

@Composable
fun MoveRow(
    viewModel: GameLogViewModel,
    selectedRow: Int,
    row: Int,
    description: String
) {
    val state = when(row) {
        in 0 until selectedRow -> RowState.PAST
        selectedRow -> RowState.PRESENT
        else -> RowState.FUTURE
    }

    val textAlpha =
        if (state == RowState.FUTURE) ContentAlpha.disabled
        else 1f

    Surface(
        elevation = when(state) {
            RowState.PAST -> 2.dp
            RowState.PRESENT -> 3.dp
            RowState.FUTURE -> 1.dp
        },
        modifier = Modifier
            .clickable {
                viewModel.setMove(row)
            }
    ) {
        Row(
            Modifier.padding(0.dp, 8.dp)
        ) {
            Text(
                text = row.toString(),
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .width(35.dp)
                    .alpha(textAlpha),
            textAlign = TextAlign.Right
            )
            Spacer(Modifier.width(20.dp))
            Text(
                text = description,
                color =
                    if (state == RowState.PRESENT) MaterialTheme.colors.primary
                    else MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(textAlpha)
            )
        }
    }
}

@Preview
@Composable
fun PreviewGameLogComposable() {
    GameLogComposable(GameLogViewModel())
}
