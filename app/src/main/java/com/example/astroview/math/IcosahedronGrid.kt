package com.example.astroview.math

import kotlin.math.sqrt

class IcosahedronGrid(private val maxLevel: Int) {
    // Create a sphere by subdividing the icosahedron.
    // Step 1. Create an icosahedron;
    // Step 2. Subdivide icosahedron faces into triangles;
    // Step 3. Push the faces outwards onto the enclosing sphere.
    // "level" controls how many subdivisions are done.
    // Reference: https://github.com/Stellarium/stellarium/blob/fc4c6bbee6493d45149e408d9dd9acfd90c57568/src/core/StelGeodesicGrid.cpp

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
                Triple(1, 0, 10),
                Triple(0, 1, 9),
                Triple(0, 9, 6),
                Triple(9, 8, 6),
                Triple(0, 7, 10),
                Triple(6, 7, 0),
                Triple(7, 6, 3),
                Triple(6, 8, 3),
                Triple(11, 10, 7),
                Triple(7, 3, 11),
                Triple(3, 2, 11),
                Triple(2, 3, 8),
                Triple(10, 11, 4),
                Triple(2, 4, 11),
                Triple(5, 4, 2),
                Triple(2, 8, 5),
                Triple(4, 1, 10),
                Triple(4, 5, 1),
                Triple(5, 9, 1),
                Triple(8, 9, 5)
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
}