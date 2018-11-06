package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River

class Intellect(val graph: Graph, val protocol: Protocol) {

    var gameStage = 0
    var currentMineId = -1
    var nextMineId = -1
    var connectibleMinesMoreThen1 = true
    val unconnectedMines = mutableSetOf<Int>()

    fun init() {
        if (graph.allMines.size < 2) {
            connectibleMinesMoreThen1 = false
            return
        }
        unconnectedMines += graph.allMines
        currentMineId = getCurrentMine()
        nextMineId = getNextMine()
    }

    fun getCurrentMine(): Int {
        var min = Int.MAX_VALUE
        var result = -1
        for (mineId in unconnectedMines) {
            val sum = graph.cantBeConnected(mineId).size
            if (sum < min) {
                result = mineId
                min = sum
            }
        }
        return result
    }

    fun getNextMine(): Int {
        var min = Int.MAX_VALUE
        var result = -1
        for (mineId in unconnectedMines) {
            if (graph.cantBeConnected(currentMineId).contains(mineId) || mineId == currentMineId) continue
            val sum = graph.cantBeConnected(mineId).size
            if (sum < min) {
                result = mineId
                min = sum
            }
        }
        return result
    }

    fun findWayToMine(first: Int, second: Int): River {//second is always "mine"
        if (graph.getCites(second)!!.contains(first)) throw IllegalArgumentException("already connected")
        //val wayFrom2to1 = mutableMapOf<Int, Int>()
        val queue = mutableListOf<Int>()
        queue.add(first)
        //wayFrom2to1.put(first, first)//
        val visited = mutableSetOf(first)
        while (queue.isNotEmpty()) {
            val current = queue[0]
            queue.removeAt(0)
            for ((id, vertexState) in graph.getNeighbors(current)) {
                if (vertexState == VertexState.Enemy || id in visited) continue
                //wayFrom2to1.put(id, current)
                if (graph.getCites(second).contains(id)) {
                    return River(current, id)
                }
                queue.add(id)
                visited.add(id)
            }
        }
        throw IllegalArgumentException("impossible to connect")//
    }

    private fun try0(): River {
        for (mine in graph.allMines) {
            val neighbor = graph.getNeighbors(mine).keys.find { key -> graph.getNeighbors(mine)[key] == VertexState.Neutral }
            if (neighbor != null) {
                return River(mine, neighbor)
            }
        }
        gameStage = 1
        throw IllegalArgumentException()
    }

    private fun try1(): River {
        var i = 0
        while (connectibleMinesMoreThen1) {
            println(i++)
            try {
                val result = findWayToMine(currentMineId, nextMineId)
                return River(result.source, result.target)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                println("$currentMineId   $nextMineId")
                graph.setCantBeConnected(currentMineId, nextMineId)
                graph.setCantBeConnected(nextMineId, currentMineId)
                if (graph.cantBeConnected(currentMineId).size >= graph.allMines.size - 1) {//==???
                    unconnectedMines.remove(currentMineId)
                }
                if (graph.cantBeConnected(nextMineId).size >= graph.allMines.size - 1) {//==???
                    unconnectedMines.remove(nextMineId)
                }
                if (unconnectedMines.size < 2) {
                    connectibleMinesMoreThen1 = false
                } else {
                    currentMineId = getCurrentMine()
                    nextMineId = getNextMine()
                }
            }
        }
        gameStage = 2
        throw IllegalArgumentException()
    }

    private fun try2(): River {
        for (ourSite in graph.ourSites) {
            val neighbor = graph.getNeighbors(ourSite).keys.find { key -> graph.getNeighbors(ourSite)[key] == VertexState.Neutral }
            if (neighbor != null) {
                return River(ourSite, neighbor)
            }
        }
        gameStage = 3
        throw IllegalArgumentException()
    }

    private fun try3(): River {
        for (site in graph.allSites) {
            val neighbor = graph.getNeighbors(site).keys.find { key -> graph.getNeighbors(site)[key] == VertexState.Neutral }
            if (neighbor != null) {
                return River(site, neighbor)
            }
        }
        gameStage = 4
        throw IllegalArgumentException()
    }

    fun makeMove() {
        try {
            val result = when (gameStage) {
                0 -> try0()
                1 -> try1()
                2 -> try2()
                3 -> try3()
                else -> return protocol.passMove()
            }
            protocol.claimMove(result.source, result.target)
        } catch (e: IllegalArgumentException) {
            makeMove()
        }
    }
}
