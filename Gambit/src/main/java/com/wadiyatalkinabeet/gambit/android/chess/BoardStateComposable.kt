package com.wadiyatalkinabeet.gambit.android.chess

import androidx.compose.animation.core.Animatable
import com.wadiyatalkinabeet.gambit.chess.datastructures.BoardState
import com.wadiyatalkinabeet.gambit.chess.datastructures.PieceColor
import com.wadiyatalkinabeet.gambit.chess.datastructures.PieceType

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.wadiyatalkinabeet.gambit.R
import com.wadiyatalkinabeet.gambit.chess.datastructures.Move

@Composable
fun BoardStateComposable(boardState: BoardState, lastMove: Move? = null) {
    val typeIcon: Map<PieceType, Painter> = mapOf(
        PieceType.KING to painterResource(R.drawable.king),
        PieceType.QUEEN to painterResource(R.drawable.queen),
        PieceType.ROOK to painterResource(R.drawable.rook),
        PieceType.BISHOP to painterResource(R.drawable.bishop),
        PieceType.KNIGHT to painterResource(R.drawable.knight),
        PieceType.PAWN to painterResource(R.drawable.pawn),
    )

    val blackSquareColor = Color(70, 70, 80, 255)
    val whiteSquareColor = Color(110, 110, 120, 255)

    val blackPieceColor = Color(20, 20, 20, 255)
    val whitePieceColor = Color(180, 180, 180, 255)

    val moveColor = MaterialTheme.colors.secondaryVariant//.copy(alpha = 0.9f)

    val moveState = remember(lastMove) { Animatable(0f) }
    LaunchedEffect(lastMove) { moveState.animateTo(1f) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1F),
    ){
        val squareSize = size / 8F
        val edgeLength = squareSize.width

        var startOffset: Offset? = null
        var currentOffset: Offset? = null
        lastMove?.run {
            startOffset = Offset(
                (from.file) * squareSize.width,
                (7f - from.rank) * squareSize.height
            )
            currentOffset = lerp(
                startOffset!!,
                Offset(
                     (to.file) * squareSize.width,
                     (7f - to.rank) * squareSize.height
                ),
                moveState.value
            )
        }

        for (x in 0 until 8) {
            for (y in 0 until 8) {
                drawRect(
                    size = squareSize,
                    topLeft = Offset(
                        x * edgeLength,
                        y * edgeLength
                    ),
                    color = if ((x + y) % 2 != 0) {
                        blackSquareColor
                    } else {
                        whiteSquareColor
                    }
                )
            }
        }

        lastMove?.run {
            var toCenter = Offset(0.5f * edgeLength, 0.5f * edgeLength)
            drawLine(
                moveColor,
                startOffset!! + toCenter,
                currentOffset!! + toCenter,
                12f,
                StrokeCap.Round
            )
            drawCircle(
                moveColor,
                16f,
                startOffset!! + toCenter
            )
        }

        for (x in 0 until 8) {
            for (y in 0 until 8) {
                boardState[x, 7 - y]?.let{ piece ->

                    val pieceOffset =
                        if (lastMove?.let{ it.to.file == x && it.to.rank == 7 - y } == true)
                            currentOffset!!
                        else
                            Offset(x * squareSize.width, y * squareSize.height)

                    translate(
                        left = pieceOffset.x,
                        top = pieceOffset.y
                    ) {
                        val icon = typeIcon[piece.type]!!
                        val color = when(piece.color) {
                            PieceColor.WHITE -> whitePieceColor
                            PieceColor.BLACK -> blackPieceColor
                        }
                        with(icon) {
                            draw(squareSize, colorFilter = ColorFilter.tint(color))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewBoardStateComposable() {
    BoardStateComposable(BoardState.standardStartingBoardState)
}
