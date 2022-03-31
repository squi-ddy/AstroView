package com.example.astroview.math

import com.example.astroview.util.ArrayTriple
import java.lang.IllegalStateException
import kotlin.math.sqrt

class IcosahedronGrid(private val maxLevel: Int) {
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
                Vec3(icosahedronA, -icosahedronB, 0),
                Vec3(icosahedronA, icosahedronB, 0),
                Vec3(-icosahedronA, icosahedronB, 0),
                Vec3(-icosahedronA, -icosahedronB, 0),
                Vec3(0, icosahedronA, -icosahedronB),
                Vec3(0, icosahedronA, icosahedronB),
                Vec3(0, -icosahedronA, icosahedronB),
                Vec3(0, -icosahedronA, -icosahedronB),
                Vec3(-icosahedronB, 0, icosahedronA),
                Vec3(icosahedronB, 0, icosahedronA),
                Vec3(icosahedronB, 0, -icosahedronA),
                Vec3(-icosahedronB, 0, -icosahedronA)
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
            initTriangle(
                newLevel, newIndex + 3,
                Triangle(
                    newT.c0, newT.c1, newT.c2
                )
            )
        }
    }

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
}