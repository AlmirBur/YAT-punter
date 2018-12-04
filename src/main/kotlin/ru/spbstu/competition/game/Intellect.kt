
package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River

class Intellect(val graph: Graph, val protocol: Protocol) {

    var gameStage = 0
    var currentSetOfMines = -1
    var nextSetOfMines = -1

    fun init() {
        if (graph.getAllMines().size < 2) {//особый режим игры?
            return
        }
    }

    //Сейчас идёт от какой-то mine первого множества до второго множества
    //В идеале нужно, чтобы соединялись коротчайшим путём
    //так ли хорош поиск в ширину?
    private fun getNextRiver(firstSet: Int, secondSet: Int): River {
        if (!graph.getAllSetsOfMines().contains(firstSet) || !graph.getAllSetsOfMines().contains(secondSet))
            throw IllegalArgumentException("set(s) doesn't exist")
        if (firstSet == secondSet) throw IllegalArgumentException("it is one set")
        val queue = mutableListOf<Int>()
        //val from = graph.getSitesBySetId(first).elementAt(ANY)
        val from = graph.getMinesBySetId(firstSet).elementAt(0)
        queue.add(from)
        val visited = mutableSetOf(from)
        while (queue.isNotEmpty()) {
            val current = queue[0]
            queue.removeAt(0)
            for ((id, siteState) in graph.getNeighbors(current)) {
                if (siteState == SiteState.Enemy || id in visited) continue
                if (graph.getSitesBySetId(secondSet).contains(id)) {
                    return River(current, id)
                }
                queue.add(id)
                visited.add(id)
            }
        }
        throw IllegalArgumentException("impossible to connect")
    }

    //Улучьшить выбор множеств
    private fun updateCurrentAndNextSetsOfMines() {
        for (i in 0..graph.getAllSetsOfMines().size - 2) {
            for (j in i + 1 until graph.getAllSetsOfMines().size) {
                if (!graph.getIncompatibleSets(graph.getAllSetsOfMines().elementAt(i))
                        .contains(graph.getAllSetsOfMines().elementAt(j))) {
                    currentSetOfMines = graph.getAllSetsOfMines().elementAt(i)
                    nextSetOfMines = graph.getAllSetsOfMines().elementAt(j)
                    return
                }
            }
        }
        throw IllegalArgumentException()
    }

    //Улучшить выбор mine и конкретной реки
    //Быть может не следует занимать все реки mines
    private fun try0(): River {
        var min = Int.MAX_VALUE
        var source = -1
        for (mineId in graph.getAllMines()) {
            val neutral = graph.getNeighbors(mineId).keys.count { key -> graph.getNeighbors(mineId)[key] == SiteState.Neutral }
            if (neutral == 0) continue
            val our = graph.getNeighbors(mineId).keys.count { key -> graph.getNeighbors(mineId)[key] == SiteState.Our }
            val enemy = graph.getNeighbors(mineId).keys.count { key -> graph.getNeighbors(mineId)[key] == SiteState.Enemy }
            if (neutral == 1) {
                if (our == 0) {
                    source = mineId
                    break
                }
                if (enemy == 0) {
                    source = mineId
                    break
                }
            }
            val sum = neutral + enemy + 2 * our
            if (sum < min) {
                source = mineId
                min = sum
            }
        }
        if (source == -1) {//нетральных соседей нет ни у одной mine
            updateCurrentAndNextSetsOfMines()
            throw IllegalArgumentException()
        }
        //val target = graph.getNeighbors(source).keys.find { key -> graph.getNeighbors(source)[key] == SiteState.Neutral }!!
        var target = -1
        for ((key, _) in graph.getNeighbors(source)) {
            if (graph.getNeighbors(source)[key] == SiteState.Neutral) {
                target = key
                if (graph.getAllMines().contains(key)) break
            }
        }
        return River (source, target)
    }

    //try 0.5: отойти от mines на более безопасное расстояние
    //try 0.75: занять мосты
    //try1: занимать реки наперёд, в тех местах, где меньше разветвлений
    //На протяжении всего try1, если нас пытаются заблочить, поддерживать свою свободу
    private fun try1(): River {
        if (graph.getAllSetsOfMines().size < 2) throw IllegalArgumentException()//or (... == 1)
        while (true) {
            try {
                val result = getNextRiver(currentSetOfMines, nextSetOfMines)
                val temp = currentSetOfMines
                currentSetOfMines = nextSetOfMines
                nextSetOfMines = temp
                return result
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                if (graph.getAllSetsOfMines().contains(currentSetOfMines) && graph.getAllSetsOfMines().contains(nextSetOfMines))
                    graph.setIncompatibleSets(currentSetOfMines, nextSetOfMines)
                updateCurrentAndNextSetsOfMines()
            }
        }
    }

    //try 1.5: реализовать пересчёт множеств
    //Каждому множеству присвоить область sites с весами
    //try2 пускать щупальца в стороны (к site с большим весом )
    //Закрывать доступ к site с большим весом?
    private fun try2(): River {
        val setOfNeighbors = mutableSetOf<Int>()
        for (ourSite in graph.ourSites) {
            setOfNeighbors.addAll(graph.getNeighbors(ourSite).keys
                    .filter { key -> graph.getNeighbors(ourSite)[key] == SiteState.Neutral && !graph.ourSites.contains(key)})
        }
        if (setOfNeighbors.isEmpty()) throw IllegalArgumentException()
        graph.getAllSetsOfMines().forEach { setOfMines -> graph.setWeights(setOfMines) }
        var max = -1L
        var target = -1
        for (neighbor in setOfNeighbors) {
            if (graph.getWeight(neighbor) > max) {
                max = graph.getWeight(neighbor)
                target = neighbor
            }
        }
        //if (target == -1) throw Exception()
        val source = graph.getNeighbors(target).keys
                .find { key -> graph.ourSites.contains(key) && graph.getNeighbors(target)[key] == SiteState.Neutral }!!
        return River(source, target)
    }

    //Закрывать доступ к site с большим весом?
    private fun try3(): River {
        for (site in graph.getAllSites()) {
            val neighbor = graph.getNeighbors(site).keys.find { key -> graph.getNeighbors(site)[key] == SiteState.Neutral }
            if (neighbor != null) {
                return River(site, neighbor)
            }
        }
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
            println("Game stage: $gameStage")
            protocol.claimMove(result.source, result.target)
        } catch (e: IllegalArgumentException) {
            gameStage++
            makeMove()
        }
    }
}