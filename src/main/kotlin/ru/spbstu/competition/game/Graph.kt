package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.Claim
import ru.spbstu.competition.protocol.data.Setup

enum class SiteState{ Our, Enemy, Neutral }

class Graph {
    private var myId = -1
    private val sites = mutableMapOf<Int, Site>()
    private val mines = mutableSetOf<Int>()
    private val setsOfMines = mutableMapOf<Int, SetOfMines>()
    val ourSites = mutableSetOf<Int>()
    //val allRivers = mutableSetOf<River>()

    private class Site {
        val neighbors = mutableMapOf<Int, SiteState>()
        val distance = mutableMapOf<Int, Long>()
        var weight = 0L
    }

    private class SetOfMines(id: Int) {
        var mines = mutableSetOf(id)
        var sites = mutableSetOf(id)
        var incompatibleSets = mutableSetOf<Int>()
    }

    fun init(setup: Setup) {
        myId = setup.punter
        for ((id) in setup.map.sites) {
            addSite(id)
        }
        for ((source, target) in setup.map.rivers) {
            connect(source, target)
            setNeighborsState(source, target, SiteState.Neutral)
            //allRivers.add(river)
        }
        for (mineId in setup.map.mines) {
            mines.add(mineId)
            addSetOfMines(mineId)
            findSitesDistances(mineId)
        }
        sites.values.forEach { site -> site.distance.keys
                .forEach { key -> site.distance[key] = site.distance[key]!! * site.distance[key]!!}}
    }

    fun getAllMines() = mines

    fun getAllSetsOfMines() = setsOfMines.keys

    fun getAllSites() = sites.keys

    fun getNeighbors(id: Int) = sites[id]!!.neighbors

    fun getDistance(id: Int) = sites[id]!!.distance

    fun getSitesBySetId(setId: Int) = setsOfMines[setId]!!.sites

    fun getMinesBySetId(setId: Int) = setsOfMines[setId]!!.mines

    fun getIncompatibleSets(setId: Int): MutableSet<Int> = setsOfMines[setId]!!.incompatibleSets

    fun setIncompatibleSets(firstSetId: Int, secondSetId: Int) {
        getIncompatibleSets(firstSetId).add(secondSetId)
        getIncompatibleSets(secondSetId).add(firstSetId)
    }

    fun getWeight(id: Int) = sites[id]!!.weight

    fun setWeight(id: Int, value: Long) {
        sites[id]!!.weight = value
    }

    private fun addSite(id: Int) {
        sites[id] = Site()
    }

    private fun connect(first: Int, second: Int) {
        sites[first]!!.neighbors.put(second, SiteState.Neutral)
        sites[second]!!.neighbors.put(first, SiteState.Neutral)
    }

    private fun addSetOfMines(id: Int) {
        setsOfMines[id] = SetOfMines(id)
        sites.values.forEach { site -> site.distance.put(id, 0) }
    }

    private fun setNeighborsState(first: Int, second: Int, state: SiteState) {
        sites[first]!!.neighbors[second] = state
        sites[second]!!.neighbors[first] = state
    }

    fun update(claim: Claim) {
        when (claim.punter) {
            myId -> {
                setNeighborsState(claim.source, claim.target, SiteState.Our)
                ourSites.add(claim.source)
                ourSites.add(claim.target)
                updateSites(claim.source, claim.target)
                println("${claim.source}   ${claim.target} \n")
            }
            else -> {
                setNeighborsState(claim.source, claim.target, SiteState.Enemy)
            }
        }
    }

    private fun updateSites(first: Int, second: Int) {
        var setContainsFirst = -1
        var setContainsSecond = -1
        for ((id, setOfMines) in setsOfMines) {
            if (setOfMines.sites.contains(first)) setContainsFirst = id
            if (setOfMines.sites.contains(second)) setContainsSecond = id
        }
        if (setContainsFirst == -1 && setContainsSecond == -1) return
        if (setContainsFirst == -1) {
            //setsOfMines[setContainsSecond]!!.sites.addAll(getPartOfGraph(first))
            setsOfMines[setContainsSecond]!!.sites.add(first)
            return
        }
        if (setContainsSecond == -1) {
            //setsOfMines[setContainsSecond]!!.sites.addAll(getPartOfGraph(second))
            setsOfMines[setContainsFirst]!!.sites.add(second)
            return
        }
        if (setContainsFirst != setContainsSecond) {
            //set1 + set2
            val newSetOfSites = (setsOfMines[setContainsFirst]!!.sites + setsOfMines[setContainsSecond]!!.sites).toMutableSet()
            val newSetOfMines = (setsOfMines[setContainsFirst]!!.mines + setsOfMines[setContainsSecond]!!.mines).toMutableSet()
            val newSetOfIncompatibleSets = (setsOfMines[setContainsFirst]!!.incompatibleSets
                    + setsOfMines[setContainsSecond]!!.incompatibleSets).toMutableSet()
            setsOfMines.remove(setContainsSecond)
            setsOfMines[setContainsFirst]!!.sites = newSetOfSites
            setsOfMines[setContainsFirst]!!.mines = newSetOfMines
            setsOfMines[setContainsFirst]!!.incompatibleSets = newSetOfIncompatibleSets
            for (incompatibleSet in newSetOfIncompatibleSets) {
                setsOfMines[incompatibleSet]!!.incompatibleSets.remove(setContainsSecond)
                setsOfMines[incompatibleSet]!!.incompatibleSets.add(setContainsFirst)
            }
        }
    }

    private fun getPartOfGraph(site: Int): Set<Int> {
        val queue = mutableListOf<Int>()
        queue.add(site)
        val visited = mutableSetOf(site)
        while (queue.isNotEmpty()) {
            val next = queue[0]
            queue.removeAt(0)
            for ((id, neighbor) in sites[next]!!.neighbors) {
                if (id !in visited && neighbor == SiteState.Our) {
                    queue.add(id)
                    visited.add(id)
                }
            }
        }
        return visited
    }

    fun setWeights(setId: Int) {
        val setOfNeighbors = mutableSetOf<Int>()
        getSitesBySetId(setId).forEach { site -> setOfNeighbors.addAll(getNeighbors(site).keys
                .filter { key -> getNeighbors(site)[key] == SiteState.Neutral && !getSitesBySetId(setId).contains(key) }) }
        setOfNeighbors.forEach { neighbor -> setWeight(neighbor, getMinesBySetId(setId).map { getDistance(neighbor)[it]!! }.sum()) }
    }

    private fun findSitesDistances(mine: Int) {
        val queue = mutableListOf<Int>()
        queue.add(mine)
        sites[mine]!!.distance.put(mine, 0)
        val visited = mutableSetOf(mine)
        while (queue.isNotEmpty()) {
            val next = queue[0]
            queue.removeAt(0)
            for (neighbor in sites[next]!!.neighbors.keys) {
                if (neighbor !in visited) {
                    sites[neighbor]!!.distance.put(mine, sites[next]!!.distance[mine]!! + 1)
                    queue.add(neighbor)
                    visited.add(neighbor)
                }
            }
        }
    }
}