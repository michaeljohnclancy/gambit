package com.wadiyatalkinabeet.gambit.android.chess

import com.wadiyatalkinabeet.gambit.chess.datastructures.BoardState
import com.wadiyatalkinabeet.gambit.chess.datastructures.PieceColor
import com.wadiyatalkinabeet.gambit.chess.datastructures.PieceType

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.wadiyatalkinabeet.gambit.R

@Composable
fun BoardStateComposable(boardState: BoardState) {
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

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1F),
    ){
        val squareSize = size / 8F
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                val squareOffset = Offset(
                    x * squareSize.width,
                    y * squareSize.height
                )
                drawRect(
                    size = squareSize,
                    topLeft = squareOffset,
                    color = if ((x+y)%2!=0) {
                        blackSquareColor
                    } else {
                        whiteSquareColor
                    }
                )

                boardState[x, 7-y]?.let{ piece ->
                    translate(
                        left = squareOffset.x,
                        top = squareOffset.y
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
