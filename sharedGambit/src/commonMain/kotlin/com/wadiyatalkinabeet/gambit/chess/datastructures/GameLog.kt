package com.wadiyatalkinabeet.gambit.chess.datastructures

class GameLog(
    val startingState: BoardState,
    var moves: MutableList<Move> = mutableListOf()
) {
    var currentState: BoardState

    init {
        currentState = applyMoves(startingState, moves)
    }

    private fun applyMoves(fromState: BoardState, moves: List<Move>): BoardState =
        moves.fold(fromState) { accumulator, move ->
            accumulator.applyMove(move)
        }

    fun addMove(move: Move) {
        moves.add(move)
        currentState = currentState.applyMove(move)
    }

    fun revertMove() {
        if (moves.isEmpty())
            return
        val lastMove = moves.removeLast()
        currentState = currentState.applyMove(lastMove.reverse())
    }

    fun getStateAtMove(i: Int) =
        applyMoves(startingState, moves.take(i))
}
