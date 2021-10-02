package com.wadiyatalkinabeet.gambit.chess.datastructures

enum class PieceColor(val char: Char, val string: String) {
    WHITE('W', "White"),
    BLACK('B', "Black")
}

enum class PieceType(val char: Char, val string: String) {
    KING('K', "King"),
    QUEEN('Q', "Queen"),
    ROOK('R', "Rook"),
    BISHOP('B', "Bishop"),
    KNIGHT('N', "Knight"),
    PAWN('P', "Pawn"),
}

data class Piece (
    val color: PieceColor,
    val type: PieceType,
) {
    constructor(id: Int) : this(
        PieceColor.values()[id % 6],
        PieceType.values()[if (id > 5) 1 else 0],
    )

    fun getId() { type.ordinal + 6 * color.ordinal }

    override fun toString(): String {
        return "${color.string} ${type.string}"
    }
}

// Create Enum so we can say e.g. 'WHITE_ROOK.ordinal' instead of '2'
enum class PieceID(val piece: Piece) {
    WHITE_KING(Piece(PieceColor.WHITE, PieceType.KING)),
    WHITE_QUEEN(Piece(PieceColor.WHITE, PieceType.QUEEN)),
    WHITE_ROOK(Piece(PieceColor.WHITE, PieceType.ROOK)),
    WHITE_BISHOP(Piece(PieceColor.WHITE, PieceType.BISHOP)),
    WHITE_KNIGHT(Piece(PieceColor.WHITE, PieceType.KNIGHT)),
    WHITE_PAWN(Piece(PieceColor.WHITE, PieceType.PAWN)),

    BLACK_KING(Piece(PieceColor.BLACK, PieceType.KING)),
    BLACK_QUEEN(Piece(PieceColor.BLACK, PieceType.QUEEN)),
    BLACK_ROOK(Piece(PieceColor.BLACK, PieceType.ROOK)),
    BLACK_BISHOP(Piece(PieceColor.BLACK, PieceType.BISHOP)),
    BLACK_KNIGHT(Piece(PieceColor.BLACK, PieceType.KNIGHT)),
    BLACK_PAWN(Piece(PieceColor.BLACK, PieceType.PAWN)),
}
