package com.wadiyatalkinabeet.gambit.chess.datastructures

typealias PiecePositions = List<List<Piece?>>

fun piecePositionsFromIntMatrix(list: List<List<Int?>>): PiecePositions =
    list.map{row ->
        row.map{
            it?.let{Piece(it)}
        }
    }

data class Square(val file: Int, val rank: Int) {
    companion object {
        private fun charToFile(c: Char): Int = when (c) {
            'a' -> 0; 'b' -> 1; 'c' -> 2; 'd' -> 3; 'e' -> 4; 'f' -> 5; 'g' -> 6; 'h' -> 7
            else -> throw Exception("Not a valid file")
        }

        private fun charToRank(c: Char): Int = when (c) {
            '1' -> 0; '2' -> 1; '3' -> 2; '4' -> 3; '5' -> 4; '6' -> 5; '7' -> 6; '8' -> 7
            else -> throw Exception("Not a valid rank")
        }

        private fun fileToChar(i: Int): Char = when (i) {
            0 -> 'a'; 1 -> 'b'; 2 -> 'c'; 3 -> 'd'; 4 -> 'e'; 5 -> 'f'; 6 -> 'g'; 7 -> 'h'
            else -> throw Exception("Not a valid file")
        }

        private fun rankToChar(i: Int): Char = when (i) {
            0 -> '1'; 1 -> '2'; 2 -> '3'; 3 -> '4'; 4 -> '5'; 5 -> '6'; 6 -> '7'; 7 -> '8'
            else -> throw Exception("Not a valid rank")
        }

        fun fromString(string: String): Square {
            require(string.length == 2)
            return Square(charToFile(string[0]), charToRank(string[1]))
        }

    }

    override fun toString(): String {
        return StringBuilder().append(fileToChar(file)).append(rankToChar(rank)).toString()
    }
}

data class BoardState(
    val piecePositions: PiecePositions,
    val whiteCanCastleQueenSide: Boolean?,
    val whiteCanCastleKingSide: Boolean?,
    val blackCanCastleQueenSide: Boolean?,
    val blackCanCastleKingSide: Boolean?,
) {
    init {
        require(piecePositions.size == 8)
        require(piecePositions.map{it.size == 8}.all{it})
    }

    companion object{
        fun newCanCastle(
            oldCanCastle: Boolean?, castleColor: PieceColor, kingSide: Boolean,
            piece: Piece, square: Square
        ): Boolean? {
            if (oldCanCastle == false) {
                return false
            }
            if (piece.color != castleColor) {
                return oldCanCastle
            }
            if (piece.type == PieceType.KING) {
                return false
            }
            if (piece.type == PieceType.ROOK
                &&((kingSide && square.file == 7) || (!kingSide && square.file == 0))
            ) {
                return false
            }
            return oldCanCastle
        }

        val standardStartingBoardState = BoardState(
            piecePositions = listOf(
                listOf(
                    PieceID.WHITE_ROOK.piece, PieceID.WHITE_KNIGHT.piece, PieceID.WHITE_BISHOP.piece,
                    PieceID.WHITE_QUEEN.piece, PieceID.WHITE_KING.piece,
                    PieceID.WHITE_BISHOP.piece, PieceID.WHITE_KNIGHT.piece, PieceID.WHITE_ROOK.piece
                ),
                List(8) { PieceID.WHITE_PAWN.piece },
                List(8) { null },
                List(8) { null },
                List(8) { null },
                List(8) { null },
                List(8) { PieceID.BLACK_PAWN.piece },
                listOf(
                    PieceID.BLACK_ROOK.piece, PieceID.BLACK_KNIGHT.piece, PieceID.BLACK_BISHOP.piece,
                    PieceID.BLACK_QUEEN.piece, PieceID.BLACK_KING.piece,
                    PieceID.BLACK_BISHOP.piece, PieceID.BLACK_KNIGHT.piece, PieceID.BLACK_ROOK.piece
                ),
            ),
            whiteCanCastleQueenSide = true,
            whiteCanCastleKingSide = true,
            blackCanCastleQueenSide = true,
            blackCanCastleKingSide = true,
        )
    }

    override fun toString(): String {
        val builder = StringBuilder()
        val rowBorder = List(41){'-'}.joinToString("") + '\n'
        builder.append(rowBorder)
        piecePositions.reversed().map{row ->
            builder.append("| ")
            row.map{
                builder.append(it?.toString() ?: "  ").append(" | ")
            }
            builder.append('\n')
            builder.append(rowBorder)
        }
        return builder.toString()
    }

    operator fun get(file: Int, rank: Int): Piece? {
        return piecePositions[rank][file]
    }

    operator fun get(square: Square): Piece? {
        return piecePositions[square.rank][square.file]
    }

    operator fun get(string: String): Piece? {
        return get(Square.fromString(string))
    }

    fun applyMove(move: Move): BoardState {
        val piece = get(move.from)
        // Some basic sanity-checking asserts; probably unnecessary due to move validation
        require(piece == move.piece)
        require(get(move.to) == move.captures)

        return BoardState(
            piecePositions = piecePositions.mapIndexed { rank, row ->
                row.mapIndexed{ file, oldPiece ->
                    if (rank == move.from.rank && file == move.from.file) {
                        null
                    } else if (rank == move.to.rank && file == move.to.file) {
                        piece
                    } else {
                        oldPiece
                    }
                }
            }.let {
                move.specialTransform?.invoke(it) ?: it
            },
            whiteCanCastleQueenSide = newCanCastle(
                whiteCanCastleQueenSide, PieceColor.WHITE, false, piece, move.from
            ),
            whiteCanCastleKingSide = newCanCastle(
                whiteCanCastleKingSide, PieceColor.WHITE, true, piece, move.from
            ),
            blackCanCastleQueenSide = newCanCastle(
                blackCanCastleQueenSide, PieceColor.BLACK, false, piece, move.from
            ),
            blackCanCastleKingSide = newCanCastle(
                blackCanCastleKingSide, PieceColor.BLACK, true, piece, move.from
            )
        )
    }
}
