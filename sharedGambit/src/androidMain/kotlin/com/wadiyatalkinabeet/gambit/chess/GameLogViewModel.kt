package com.wadiyatalkinabeet.gambit.chess

import com.wadiyatalkinabeet.gambit.chess.datastructures.*

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameLogViewModel: ViewModel() {
    val gameLog: GameLog = GameLog(BoardState.standardStartingBoardState)
    var focusedMove: MutableLiveData<Int> = MutableLiveData(0)

    init{
        listOf(
            Move(PieceID.WHITE_PAWN.piece, Square.fromString("e2"), Square.fromString("e4")),
            Move(PieceID.BLACK_PAWN.piece, Square.fromString("e7"), Square.fromString("e5")),
            Move(PieceID.WHITE_KNIGHT.piece, Square.fromString("g1"), Square.fromString("f3")),
            Move(PieceID.BLACK_KNIGHT.piece, Square.fromString("b8"), Square.fromString("c6")),
            Move(PieceID.WHITE_BISHOP.piece, Square.fromString("f1"), Square.fromString("b5")),
            Move(PieceID.BLACK_PAWN.piece, Square.fromString("a7"), Square.fromString("a6")),
            Move(PieceID.WHITE_BISHOP.piece, Square.fromString("b5"), Square.fromString("a4")),
            Move(PieceID.BLACK_KNIGHT.piece, Square.fromString("g8"), Square.fromString("f6")),
            Move(PieceID.WHITE_KING.piece, Square.fromString("e1"), Square.fromString("g1")),
            Move(PieceID.BLACK_BISHOP.piece, Square.fromString("f8"), Square.fromString("e7")),
            Move(PieceID.WHITE_ROOK.piece, Square.fromString("f1"), Square.fromString("e1")),
            Move(PieceID.BLACK_PAWN.piece, Square.fromString("b7"), Square.fromString("b5")),
            Move(PieceID.WHITE_BISHOP.piece, Square.fromString("a4"), Square.fromString("b3")),
            Move(PieceID.BLACK_KING.piece, Square.fromString("e8"), Square.fromString("g8")),
        ).forEach{ gameLog.addMove(it) }
    }

    fun setMove(i: Int) {
        focusedMove.value = i
    }
}
