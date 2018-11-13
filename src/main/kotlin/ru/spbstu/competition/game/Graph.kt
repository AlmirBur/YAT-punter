package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.Claim
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup

enum class VertexState{ Our, Enemy, Neutral }

class Graph {
    private class Vertex {
        val neighbors = mutableMapOf<Int, VertexState>()
        val shortestWays = mutableMapOf<Int, Int>()
        var averageWeight = 0.0
    }

    private class Mine(id: Int) {
        var sites = mutableSetOf(id)
        val incompatibleSets = mutableSetOf<Int>()
    }

    fun init(setup: Setup) {
        myId = setup.punter

        for ((id) in setup.map.sites) {
            addVertex(id)
        }
        for (river in setup.map.rivers) {
            connect(river.source, river.target)
            setNeighborsState(river.source, river.target, VertexState.Neutral)
            allRivers.add(river)
        }
        for (mineId in setup.map.mines) {
            addMine(mineId)
            findSitesWeights(mineId)
        }
    }

    var myId = -1
    private val vertices = mutableMapOf<Int, Vertex>()
    private val mines = mutableMapOf<Int, Mine>()
    val ourSites = mutableSetOf<Int>()
    val allRivers = mutableSetOf<River>()

    fun getAllMines() = mines.keys
    fun getAllSites() = vertices.keys

    fun getNeighbors(id: Int) = vertices[id]!!.neighbors
    fun getShortestWays(id: Int) = vertices[id]!!.shortestWays
    fun getSites(mine: Int) = mines[mine]!!.sites

    fun getIncompatibleSets(id: Int): MutableSet<Int> = mines[id]!!.incompatibleSets

    fun setIncompatibleSets(id: Int, setId: Int) {
        getIncompatibleSets(id).add(setId)
    }

    fun getAverageWeight(id: Int) = vertices[id]!!.averageWeight
    fun setAverageWeight(id: Int, value: Double) {
        vertices[id]!!.averageWeight = value
    }

    fun addVertex(id: Int) {
        vertices[id] = Vertex()
    }

    fun connect(first: Int, second: Int) {
        vertices[first]!!.neighbors.put(second, VertexState.Neutral)
        vertices[second]!!.neighbors.put(first, VertexState.Neutral)
    }

    fun addMine(id: Int) {
        mines[id] = Mine(id)
        for (vertex in vertices) {
            vertex.value.shortestWays.put(id, 0)
        }
    }

    fun setNeighborsState(first: Int, second: Int, state: VertexState) {
        vertices[first]!!.neighbors[second] = state
        vertices[second]!!.neighbors[first] = state
    }

    fun update(claim: Claim) {
        when (claim.punter) {
            myId -> {
                setNeighborsState(claim.source, claim.target, VertexState.Our)
                ourSites.add(claim.source)
                ourSites.add(claim.target)
                updateSites(claim.source, claim.target)
                println("${claim.source}   ${claim.target} \n")
            }
            else -> {
                setNeighborsState(claim.source, claim.target, VertexState.Enemy)
            }
        }
    }

    fun updateSites(first: Int, second: Int) {
        val minesContainsFirst = mutableListOf<Int>()
        val minesContainsSecond = mutableListOf<Int>()
        for ((id, mine) in mines) {
            if (mine.sites.contains(first)) minesContainsFirst.add(id)
            if (mine.sites.contains(second)) minesContainsSecond.add(id)
        }
        //if (minesContainsFirst.isEmpty() && minesContainsSecond.isEmpty()) throw IllegalArgumentException("both sites not added")
        if (minesContainsFirst.isEmpty() && minesContainsSecond.isEmpty()) return
        if (minesContainsFirst.isEmpty()) {
            mines[minesContainsSecond[0]]!!.sites.add(first)
            return
        }
        if (minesContainsSecond.isEmpty()) {
            mines[minesContainsFirst[0]]!!.sites.add(second)
            return
        }
        //both are not empty
        if (!minesContainsFirst.contains(minesContainsSecond[0])) {
            //set1 + set2
            val newSet = (mines[minesContainsFirst[0]]!!.sites + mines[minesContainsSecond[0]]!!.sites).toMutableSet()
            for (mineId in minesContainsFirst) {
                mines[mineId]!!.sites = newSet
            }
            for (mineId in minesContainsSecond) {
                mines[mineId]!!.sites = newSet
            }
        }
    }

    fun setAverageWeights(setId: Int) {
        val setNeighbors = mutableSetOf<Int>()
        for (site in getSites(setId)) {
            setNeighbors.addAll(getNeighbors(site).keys
                    .filter { key -> getNeighbors(site)[key] == VertexState.Neutral && !ourSites.contains(key) })
        }
        val setMines = getAllMines().filter { getSites(it) === getSites(setId) }.toSet()
        for (site in setNeighbors) {
            setAverageWeight(site, setMines.sumBy { getShortestWays(site)[it]!! }.toDouble() / setMines.size)
        }
        getAllSites().forEach { site ->
            setAverageWeight(site, setMines.sumBy { getShortestWays(site)[it]!! }.toDouble() / setMines.size) }
    }

    fun findSitesWeights(mine: Int) {
        val queue = mutableListOf<Int>()
        queue.add(mine)
        vertices[mine]!!.shortestWays.put(mine, 0)
        val visited = mutableSetOf(mine)
        while (queue.isNotEmpty()) {
            val next = queue[0]
            queue.removeAt(0)
            for (neighbor in vertices[next]!!.neighbors.keys) {
                if (neighbor !in visited) {
                    vertices[neighbor]!!.shortestWays.put(mine, vertices[next]!!.shortestWays[mine]!! + 1)
                    queue.add(neighbor)
                    visited.add(neighbor)
                }
            }
        }
    }
}