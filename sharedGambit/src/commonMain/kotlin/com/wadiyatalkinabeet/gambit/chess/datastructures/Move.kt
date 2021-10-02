package com.wadiyatalkinabeet.gambit.chess.datastructures

import kotlin.math.abs

data class Move (
    val piece: Piece,
    val from: Square,
    val to: Square,
    val captures: Piece? = null,
    val isEnPassant: Boolean = false,
    val promoteTo: Piece? = null
) {
    val isCastle = piece.type == PieceType.KING && abs(to.file - from.file) == 2
    val isPromotion = piece.type == PieceType.PAWN && (
            (piece.color == PieceColor.WHITE && to.rank == 7)
                    || (piece.color == PieceColor.BLACK && to.rank == 0)
            )

    val specialTransform: ((PiecePositions) -> PiecePositions)? = when {
        /*
        * Some moves lead to additional side-effects in addition to a simple piece movement (e.g.
        * when a king castles there's an additional movement by the rook). Such actions are encoded
        * as a function altering a PiecePositions, stored as `specialTransform`.
        */
        isCastle -> ::performCastle
        isEnPassant -> ::performEnPassant
        isPromotion -> ::performPromotion
        else -> null
    }

    init {
        if (isPromotion)
            require(promoteTo != null)
    }

    fun reverse() = Move(
        piece, from=to, to=from
    )

    override fun toString(): String = when {
        isCastle -> {
            val side = when (to.file - from.file > 0) {
                true -> "King"
                false -> "Queen"
            }
            "${piece.color.string} castles $side side"
        }
        isEnPassant ->
            "${piece.color.string} en passant to $to"
        isPromotion ->
            "${piece.color.string} promotes to ${promoteTo!!.type} on $to"
        captures != null ->
            "$piece captures $from to $to"
        else -> "$piece from $from to $to"
    }


    private fun performCastle(positions: PiecePositions): PiecePositions {
        val (rookFileFrom, rookFileTo) = when(to.file) {
            2 -> 0 to 3
            6 -> 7 to 5
            else -> throw Exception("Invalid move")
        }
        val rook = positions[to.rank][rookFileFrom]

        return positions.mapIndexed { rank, row ->
            row.mapIndexed { file, oldPiece ->
                if (rank == to.rank && file == rookFileFrom) {
                    null
                } else if (rank == to.rank && file == rookFileTo){
                    rook
                } else
                    oldPiece
            }
        }
    }

    private fun performPromotion(positions: PiecePositions) =
        positions.mapIndexed { rank, row ->
            row.mapIndexed { file, oldPiece ->
                if (rank == to.rank && file == to.file) {
                    promoteTo
                } else
                    oldPiece
            }
        }

    private fun performEnPassant(positions: PiecePositions): PiecePositions {
        val captureRank = when(piece.color) {
            PieceColor.WHITE -> 5
            PieceColor.BLACK -> 4
        }
        return positions.mapIndexed { rank, row ->
            row.mapIndexed { file, oldPiece ->
                if (rank == captureRank && file == to.file) {
                    null
                } else
                    oldPiece
            }
        }
    }

}
