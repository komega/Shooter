package com.example.shooter.game.model

import androidx.compose.ui.graphics.Color

sealed class Entity(
    open val position: Vector2D,
    open val radius: Float,
    open val color: Color
)

data class Player(
    override val position: Vector2D,
    override val radius: Float = 20f,
    override val color: Color = Color.Green,
    val health: Float = 100f
) : Entity(position, radius, color)

data class Enemy(
    override val position: Vector2D,
    override val radius: Float = 15f,
    override val color: Color = Color.Red,
    val health: Float = 20f,
    val speed: Float = 2f
) : Entity(position, radius, color)

data class Bullet(
    override val position: Vector2D,
    val velocity: Vector2D,
    override val radius: Float = 5f,
    override val color: Color = Color.Yellow
) : Entity(position, radius, color)
