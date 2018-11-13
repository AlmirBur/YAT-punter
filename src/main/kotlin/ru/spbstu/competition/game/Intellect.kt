
package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River

class Intellect(val graph: Graph, val protocol: Protocol) {

    var gameStage = 0
    var river = River(-1, -1)
    //var connectibleMinesMoreThen1 = true
    val unconnectedMines = mutableSetOf<Int>()
    var currentMineId = -1
    var nextMineId = -1

    fun init() {
        if (graph.getAllMines().size < 2) {//особый режим игры???
            //connectibleMinesMoreThen1 = false
            return
        }
        unconnectedMines += graph.getAllMines()
        //currentMineId = getCurrentMine()
        //nextMineId = getNextMine()
    }

    /*fun getCurrentMine(): Int = when (gameStage) {
        0 -> {
            var min = Int.MAX_VALUE
            var result = -1
            for (mineId in graph.allMines) {
                val sum = graph.getNeighbors(mineId).keys.count { key -> graph.getNeighbors(mineId)[key] == VertexState.Our }
                if (sum < min) {
                    result = mineId
                    min = sum
                }
            }
            if (graph.getNeighbors(result).keys.find { key -> graph.getNeighbors(result)[key] == VertexState.Neutral } == null) -1
            else result
        }
        1 -> {
            var min = Int.MAX_VALUE
            var result = -1
            for (mineId in unconnectedMines) {
                val sum = graph.cantBeConnected(mineId).size
                if (sum < min) {
                    result = mineId
                    min = sum
                }
            }
            result
        }
        2 -> {
            -1
        }
        3 -> {
            -1
        }
        else -> {
            -1
        }
    }*/

    /*fun getNextMine(): Int = when(gameStage) {
        0 -> {
            if (currentMineId == -1) -1
            else graph.getNeighbors(currentMineId).keys.find { key -> graph.getNeighbors(currentMineId)[key] == VertexState.Neutral }!!
        }
        1 -> {
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
            result
        }
        2 -> {
            -1
        }
        3 -> {
            -1
        }
        else -> {
            -1
        }
    }*/

    //fun getNextRiver(): River = when(gameStage) {
    //    0 -> {
    //        var min = Int.MAX_VALUE
    //        var source = -1
    //        for (mineId in graph.allMines) {
    //            val sum = graph.getNeighbors(mineId).keys.count { key -> graph.getNeighbors(mineId)[key] == VertexState.Our }
    //            if (sum < min) {
    //                source = mineId
    //                min = sum
    //            }
    //        }
    //        val target = graph.getNeighbors(source).keys.find { key -> graph.getNeighbors(source)[key] == VertexState.Neutral }//else graph.getNeighbors(currentMineId).keys.find { key -> graph.getNeighbors(currentMineId)[key] == VertexState.Neutral }!!
    //        if (target == null) {
    //            //нетральных соседей нет
    //            throw IllegalArgumentException()
    //        } else {
    //            River(source, target)
    //        }
    //    }
    //    1 -> {//River
    //        var min_1 = Int.MAX_VALUE
    //        var min_2 = Int.MAX_VALUE
    //        var first = -1
    //        var second = -1
    //        for (mineId in unconnectedMines) {
    //            val sum = graph.cantBeConnected(mineId).size
    //            if (sum < min_1) {
    //                second = first
    //                min_2 = min_1
    //                first = mineId
    //                min_1 = sum
    //            } else {
    //                if (sum < min_2) {
    //                    min_2 = sum
    //                    second = mineId
    //                }
    //            }
    //        }
    //        findWayToMine(first, second)
    //    }
    //    11 -> {//unconnectedMines.size >= 2 !!!!!!!!!
    //        while (connectibleMinesMoreThen1) {
    //            try {
    //                findWayToMine(currentMineId, nextMineId)//throws
    //            } catch (e: IllegalArgumentException) {
    //                graph.setCantBeConnected(currentMineId, nextMineId)
    //                graph.setCantBeConnected(nextMineId, currentMineId)
    //                if (graph.cantBeConnected(currentMineId).size >= graph.allMines.size - 1) {//==???
    //                    unconnectedMines.remove(currentMineId)
    //                }
    //                if (graph.cantBeConnected(nextMineId).size >= graph.allMines.size - 1) {//==???
    //                    unconnectedMines.remove(nextMineId)
    //                }
    //                if (unconnectedMines.size < 2) {
    //                    connectibleMinesMoreThen1 = false
    //                    throw IllegalArgumentException()
    //                }
    //                var min_1 = Int.MAX_VALUE
    //                var min_2 = Int.MAX_VALUE
    //                var first = -1
    //                var second = -1
    //                for (mineId in unconnectedMines) {
    //                    val sum = graph.cantBeConnected(mineId).size
    //                    if (sum < min_1) {
    //                        second = first
    //                        min_2 = min_1
    //                        first = mineId
    //                        min_1 = sum
    //                    } else {
    //                        if (sum < min_2) {
    //                            min_2 = sum
    //                            second = mineId
    //                        }
    //                    }
    //                }
    //                currentMineId = first
    //                nextMineId = second
    //            }
    //        }
    //        River(-1, -1)
    //    }
    //    else -> throw IllegalArgumentException()
    //}

    fun findWayToMine(first: Int, second: Int): River {//second is always "mine"
        if (!graph.getAllSites().contains(first) || !graph.getAllSites().contains(second)) throw IllegalArgumentException("site(s) doesn't exist")
        if (graph.getSites(second)!!.contains(first)) throw IllegalArgumentException("already connected")
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
                if (graph.getSites(second).contains(id)) {
                    return River(current, id)
                }
                queue.add(id)
                visited.add(id)
            }
        }
        throw IllegalArgumentException("impossible to connect")//
    }

    private fun try0(): River {
        var min = Int.MAX_VALUE
        var source = -1
        for (mineId in graph.getAllMines()) {
            if (graph.getNeighbors(mineId).keys.find
                { key -> graph.getNeighbors(mineId)[key] == VertexState.Neutral } == null) continue
            val sum = graph.getNeighbors(mineId).keys.count { key -> graph.getNeighbors(mineId)[key] == VertexState.Our }//Neutral?
            if (sum < min) {
                source = mineId
                min = sum
            }
        }
        if (source == -1) {//нетральных соседей нет ни у одной mine
            gameStage = 1
            if (graph.getAllMines().size > 1) {
                currentMineId = graph.getAllMines().elementAt(0)//??
                nextMineId = graph.getAllMines().elementAt(1)//??
            }
            throw IllegalArgumentException()
        }
        val target = graph.getNeighbors(source).keys.find { key -> graph.getNeighbors(source)[key] == VertexState.Neutral }!!
        return River (source, target)
    }

    private fun try1(): River {
        while (true) {//connectibleMinesMoreThan1
            try {
                val result = findWayToMine(currentMineId, nextMineId)//throws
                val tempMineId = currentMineId
                currentMineId = nextMineId
                nextMineId = tempMineId
                return result
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                //graph.setCantBeConnected(currentMineId, nextMineId)
                //graph.setCantBeConnected(nextMineId, currentMineId)
                graph.getAllMines()
                        .filter { graph.getSites(it) === graph.getSites(currentMineId) }
                        .forEach { mineId ->
                            graph.getAllMines()
                                    .filter { graph.getSites(it) === graph.getSites(nextMineId) }
                                    .forEach {
                                        graph.setIncompatibleSets(mineId, it)
                                        graph.setIncompatibleSets(it, mineId)
                                    }
                        }
                //if (graph.cantBeConnected(currentMineId).size >= graph.getAllMines().size - 1) {//==???
                //    unconnectedMines.remove(currentMineId)
                //}
                //if (graph.cantBeConnected(nextMineId).size >= graph.getAllMines().size - 1) {//==???
                //    unconnectedMines.remove(nextMineId)
                //}
                //if (graph.getIncompatibleSets(currentMineId)) {}
                //if (graph.getIncompatibleSets(nextMineId)) {}
                //if (unconnectedMines.size < 2) {
                //    //connectibleMinesMoreThen1 = false
                //    gameStage = 2
                //    throw IllegalArgumentException()
                //}
                var connectibleMinesLessThan2 = true
                findMines@for (i in 0..graph.getAllMines().size - 2) {
                    for (j in i + 1 until graph.getAllMines().size) {
                        if (graph.getSites(graph.getAllMines().elementAt(i))
                                !== graph.getSites(graph.getAllMines().elementAt(j))
                                && !graph.getIncompatibleSets(graph.getAllMines().elementAt(i))
                                .contains(graph.getAllMines().elementAt(j))) {
                            currentMineId = graph.getAllMines().elementAt(i)
                            nextMineId = graph.getAllMines().elementAt(j)
                            connectibleMinesLessThan2 = false
                            break@findMines
                        }
                    }
                }
                if (connectibleMinesLessThan2) {
                    gameStage = 2
                    throw IllegalArgumentException()
                }


                //var min = Int.MAX_VALUE
                //var first = -1
                //for (mineId in unconnectedMines) {
                //    val sum = graph.cantBeConnected(mineId).size
                //    if (sum < min) {
                //        first = mineId
                //        min = sum
                //    }
                //}
                //min = Int.MAX_VALUE
                //var second = -1
                //for (mineId in unconnectedMines) {
                //    if (mineId == first) continue
                //    val sum = graph.cantBeConnected(mineId).size
                //    if (sum < min) {
                //        second = mineId
                //        min = sum
                //    }
                //}
                //currentMineId = first
                //nextMineId = second
            }
        }
    }

    //private fun try1(): River {
    //    while (connectibleMinesMoreThen1) {
    //        try {
    //            val result = findWayToMine(river.source, river.target)
    //            river = River(river.target, river.source)
    //            return River(result.source, result.target)
    //        } catch (e: IllegalArgumentException) {
    //            e.printStackTrace()
    //            println("$currentMineId   $nextMineId")
    //            graph.setCantBeConnected(currentMineId, nextMineId)
    //            graph.setCantBeConnected(nextMineId, currentMineId)
    //            if (graph.cantBeConnected(currentMineId).size >= graph.getAllMines().size - 1) {//==???
    //                unconnectedMines.remove(currentMineId)
    //            }
    //            if (graph.cantBeConnected(nextMineId).size >= graph.getAllMines().size - 1) {//==???
    //                unconnectedMines.remove(nextMineId)
    //            }
    //            if (unconnectedMines.size < 2) {
    //                connectibleMinesMoreThen1 = false
    //            } else {
    //                currentMineId = getCurrentMine()
    //                nextMineId = getNextMine()
    //            }
    //        }
    //    }
    //    gameStage = 2
    //    throw IllegalArgumentException()
    //}
//
    //private fun try11(): River {
    //    while (connectibleMinesMoreThen1) {
    //        try {
    //            getNextRiver()
    //        } catch (e: IllegalArgumentException) {
    //            graph.setCantBeConnected(currentMineId, nextMineId)
    //            graph.setCantBeConnected(nextMineId, currentMineId)
    //            if (graph.cantBeConnected(currentMineId).size >= graph.getAllMines().size - 1) {//==???
    //                unconnectedMines.remove(currentMineId)
    //            }
    //            if (graph.cantBeConnected(nextMineId).size >= graph.getAllMines().size - 1) {//==???
    //                unconnectedMines.remove(nextMineId)
    //            }
    //            if (unconnectedMines.size < 2) {
    //                connectibleMinesMoreThen1 = false
    //            } else {
    //                currentMineId = getCurrentMine()
    //                nextMineId = getNextMine()
    //            }
    //        }
    //    }
    //    gameStage = 2
    //    throw IllegalArgumentException()
    //}

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

    private fun try22(): River {
        val setNeighbors = mutableSetOf<Int>()
        for (ourSite in graph.ourSites) {
            setNeighbors.addAll(graph.getNeighbors(ourSite).keys
                    .filter { key -> graph.getNeighbors(ourSite)[key] == VertexState.Neutral && !graph.ourSites.contains(key)})
        }
        if (setNeighbors.isEmpty()) {
            gameStage = 3
            throw IllegalArgumentException()
        }
        //!! найти средние веса соседей
        for (mine in graph.getAllMines()) {//не оптимально
            graph.setAverageWeights(mine)
        }
        //
        var max = -1.0
        var target = -1
        for (neighbor in setNeighbors) {
            if (graph.getAverageWeight(neighbor) > max) {
                max = graph.getAverageWeight(neighbor)
                target = neighbor
            }
        }
        if (target == -1) throw Exception()//?????
        val source = graph.getNeighbors(target).keys
                .find { key -> graph.ourSites.contains(key) && graph.getNeighbors(target)[key] == VertexState.Neutral }!!
        println("$source,       $target")
        return River(source, target)
    }

    private fun try3(): River {
        for (site in graph.getAllSites()) {
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
                2 -> try22()
                3 -> try3()
                else -> return protocol.passMove()
            }
            println("Game stage: $gameStage")
            protocol.claimMove(result.source, result.target)
        } catch (e: IllegalArgumentException) {
            makeMove()
        }
    }
}