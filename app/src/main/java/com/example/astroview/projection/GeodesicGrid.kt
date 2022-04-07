package com.example.astroview.projection

import com.example.astroview.core.CoreConstants
import com.example.astroview.math.SphericalCap
import com.example.astroview.math.Triangle
import com.example.astroview.math.Vec3
import com.example.astroview.stars.data.DetailedStar
import com.example.astroview.stars.StarManager
import com.example.astroview.util.ArrayTriple
import com.example.astroview.util.Intersection
import kotlin.math.min
import kotlin.math.sqrt

class GeodesicGrid(val maxLevel: Int) {
    // Create a sphere by subdividing the icosahedron.
    // Step 1. Create an icosahedron;
    // Step 2. Subdivide icosahedron faces into triangles;
    // Step 3. Push the faces outwards onto the enclosing sphere.
    // "level" controls how many subdivisions are done.
    // Reference: https://github.com/Stellarium/stellarium/blob/master/src/core/StelGeodesicGrid.cpp

    companion object {
        private val goldenRatio = 0.5 * (1.0 + sqrt(5.0))
        private val icosahedronB = 1.0 / sqrt(1.0 + goldenRatio * goldenRatio)
        private val icosahedronA = icosahedronB * goldenRatio

        private val icosahedronCorners =
            arrayOf(
                Vec3.fromXYZ(icosahedronA, -icosahedronB, 0),
                Vec3.fromXYZ(icosahedronA, icosahedronB, 0),
                Vec3.fromXYZ(-icosahedronA, icosahedronB, 0),
                Vec3.fromXYZ(-icosahedronA, -icosahedronB, 0),
                Vec3.fromXYZ(0, icosahedronA, -icosahedronB),
                Vec3.fromXYZ(0, icosahedronA, icosahedronB),
                Vec3.fromXYZ(0, -icosahedronA, icosahedronB),
                Vec3.fromXYZ(0, -icosahedronA, -icosahedronB),
                Vec3.fromXYZ(-icosahedronB, 0, icosahedronA),
                Vec3.fromXYZ(icosahedronB, 0, icosahedronA),
                Vec3.fromXYZ(icosahedronB, 0, -icosahedronA),
                Vec3.fromXYZ(-icosahedronB, 0, -icosahedronA)
            )

        // Stores triangles based on which corners they are connected to.
        private val icosahedronTriangles =
            arrayOf(
                ArrayTriple(1, 0, 10),
                ArrayTriple(0, 1, 9),
                ArrayTriple(0, 9, 6),
                ArrayTriple(9, 8, 6),
                ArrayTriple(0, 7, 10),
                ArrayTriple(6, 7, 0),
                ArrayTriple(7, 6, 3),
                ArrayTriple(6, 8, 3),
                ArrayTriple(11, 10, 7),
                ArrayTriple(7, 3, 11),
                ArrayTriple(3, 2, 11),
                ArrayTriple(2, 3, 8),
                ArrayTriple(10, 11, 4),
                ArrayTriple(2, 4, 11),
                ArrayTriple(5, 4, 2),
                ArrayTriple(2, 8, 5),
                ArrayTriple(4, 1, 10),
                ArrayTriple(4, 5, 1),
                ArrayTriple(5, 9, 1),
                ArrayTriple(8, 9, 5)
            )

    }

    private val triangles: Array<Array<Triangle>>

    init {
        if (maxLevel <= 0) {
            throw IllegalArgumentException("Level should be greater than zero")
        }
        var noOfTriangles = 5
        triangles = Array(maxLevel) {
            noOfTriangles *= 4
            Array(noOfTriangles) {
                Triangle.NULL
            }
        }
        for (i in 0 until 20) {
            initTriangle(
                0, i,
                Triangle(
                    icosahedronCorners[icosahedronTriangles[i][0]],
                    icosahedronCorners[icosahedronTriangles[i][1]],
                    icosahedronCorners[icosahedronTriangles[i][2]]
                )
            )
        }
    }

    private fun initTriangle(level: Int, index: Int, t: Triangle) {
        val newT = Triangle(
            (t.c1 + t.c2).norm(),
            (t.c2 + t.c0).norm(),
            (t.c0 + t.c1).norm()
        )
        triangles[level][index] = newT
        val newLevel = level + 1
        if (newLevel < maxLevel) {
            val newIndex = index * 4
            initTriangle(
                newLevel, newIndex,
                Triangle(
                    t.c0, newT.c2, newT.c1
                )
            )
            initTriangle(
                newLevel, newIndex + 1,
                Triangle(
                    newT.c2, t.c1, newT.c0
                )
            )
            initTriangle(
                newLevel, newIndex + 2,
                Triangle(
                    newT.c1, newT.c0, t.c2
                )
            )
            initTriangle(newLevel, newIndex + 3, newT)
        }
    }

    /**
     * Get the zone index for a point at a specified level.
     * @param v Point to find zone for
     * @param searchLevel Level to find zone for
     * @return Zone that this vector belongs in
     */
    fun getZoneNumberForPoint(v: Vec3, searchLevel: Int): Int {
        var i = 0
        while (i < 20) {
            val corners = icosahedronTriangles[i]
            val c0 = icosahedronCorners[corners[0]]
            val c1 = icosahedronCorners[corners[1]]
            val c2 = icosahedronCorners[corners[2]]
            if (c0.cross(c1).dot(v) >= 0 && c1.cross(c2).dot(v) >= 0 && c2.cross(c0).dot(v) >= 0) {
                // v is inside this triangle
                for (lev in 0 until searchLevel) {
                    val t = triangles[lev][i]
                    i.shl(2)
                    i += when {
                        t.c1.cross(t.c2).dot(v) <= 0 -> 0
                        t.c2.cross(t.c0).dot(v) <= 0 -> 1
                        t.c0.cross(t.c1).dot(v) <= 0 -> 2
                        else -> 3
                    }
                }
                return i
            }
            i++
        }
        throw IllegalStateException("Zone not found for point $v")
    }

    fun visitTriangles(maxVisLevel: Int, context: StarManager) {
        val maxVisitLevel = min(maxVisLevel, maxLevel)
        for (i in 0 until 20) {
            val corners = icosahedronTriangles[i]
            visitTriangles(
                0, i, Triangle(
                    icosahedronCorners[corners[0]],
                    icosahedronCorners[corners[1]],
                    icosahedronCorners[corners[2]]
                ), maxVisitLevel, context
            )
        }
    }

    private fun visitTriangles(
        level: Int,
        index: Int,
        t: Triangle,
        maxVisLevel: Int,
        context: StarManager
    ) {
        context.initTriangle(level, index, t)
        var lev = level
        var i = index
        val nextT = triangles[lev][index]
        lev++
        if (lev < maxVisLevel) {
            i *= 4
            visitTriangles(lev, i + 0, Triangle(t.c0, nextT.c2, nextT.c1), maxVisLevel, context)
            visitTriangles(lev, i + 1, Triangle(nextT.c2, t.c1, nextT.c0), maxVisLevel, context)
            visitTriangles(lev, i + 2, Triangle(nextT.c1, nextT.c0, t.c2), maxVisLevel, context)
            visitTriangles(lev, i + 3, nextT, maxVisLevel, context)
        }
    }

    fun searchAround(
        maxVisLevel: Int,
        v: Vec3,
        cosLimFov: Double,
        context: StarManager,
        resultList: MutableList<DetailedStar>
    ) {
        val maxVisitLevel = min(maxVisLevel, maxLevel)
        val zonesToSearch = arrayListOf(arrayListOf<Int>())
        getZonesForCap(maxVisitLevel, SphericalCap(v, CoreConstants.CAP_COS_FOV), zonesToSearch)
        for (level in zonesToSearch.indices) {
            for (zone in zonesToSearch[level]) {
                context.searchAround(level, zone, v, cosLimFov, resultList)
            }
        }
    }

    /**
     * Get all zones intersected by a given cap.
     * @param maxVisLevel max level to visit.
     * @param cap The cap to find zones for.
     * @param resultArray An array to store results in.
     * @see SphericalCap
     * @return An array of levels, with an array of zones for each level
     */
    fun getZonesForCap(
        maxVisLevel: Int,
        cap: SphericalCap,
        resultArray: ArrayList<ArrayList<Int>>
    ) {
        resultArray.clear()
        val maxVisitLevel = min(maxVisLevel, maxLevel)
        for (i in 0 until maxVisitLevel) {
            resultArray.add(ArrayList())
        }
        for (i in 0 until 20) {
            val corners = icosahedronTriangles[i]
            val triangle = Triangle(
                icosahedronCorners[corners[0]],
                icosahedronCorners[corners[1]],
                icosahedronCorners[corners[2]]
            )
            when (cap.containsTriangle(triangle)) {
                Intersection.PARTIALLY -> {
                    resultArray[0].add(i)
                    getZonesForCap(0, i, maxVisitLevel, triangle, cap, false, resultArray)
                }
                Intersection.COMPLETELY -> {
                    resultArray[0].add(i)
                    getZonesForCap(0, i, maxVisitLevel, triangle, cap, true, resultArray)
                }
                Intersection.NONE -> {
                    //Log.e("sus", "$i not in")
                }
            }

        }
    }

    private fun getZonesForCap(
        level: Int,
        index: Int,
        maxVisLevel: Int,
        t: Triangle,
        cap: SphericalCap,
        totallyInside: Boolean,
        resultArray: ArrayList<ArrayList<Int>>
    ) {
        var lev = level
        var i = index
        val nextT = triangles[lev][i]
        lev++
        if (lev < maxVisLevel) {
            i *= 4
            val nextTriangles = arrayOf(
                Triangle(t.c0, nextT.c2, nextT.c1),
                Triangle(nextT.c2, t.c1, nextT.c0),
                Triangle(nextT.c1, nextT.c0, t.c2),
                nextT
            )
            for (triangle in nextTriangles) {
                if (totallyInside) {
                    resultArray[lev].add(i)
                } else {
                    when (cap.containsTriangle(triangle)) {
                        Intersection.COMPLETELY -> {
                            resultArray[lev].add(i)
                            getZonesForCap(lev, i, maxVisLevel, triangle, cap, true, resultArray)
                        }
                        Intersection.PARTIALLY -> {
                            resultArray[lev].add(i)
                            getZonesForCap(lev, i, maxVisLevel, triangle, cap, false, resultArray)
                        }
                        Intersection.NONE -> {
                            // do nothing
                        }
                    }
                }
                i++
            }
        }
    }
}