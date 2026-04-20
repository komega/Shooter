package com.example.shooter.game.model

import kotlin.math.sqrt

data class Vector2D(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vector2D(x / scalar, y / scalar)

    fun length() = sqrt(x * x + y * y)
    
    fun normalize(): Vector2D {
        val len = length()
        return if (len != 0f) this / len else Vector2D()
    }

    fun distanceTo(other: Vector2D) = (other - this).length()
}
