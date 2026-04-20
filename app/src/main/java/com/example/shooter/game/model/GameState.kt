package com.example.shooter.game.model

data class GameState(
    val player: Player = Player(Vector2D(500f, 500f)),
    val enemies: List<Enemy> = emptyList(),
    val bullets: List<Bullet> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val viewportSize: Vector2D = Vector2D(1000f, 1000f)
)
