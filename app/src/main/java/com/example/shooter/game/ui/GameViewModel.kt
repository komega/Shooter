package com.example.shooter.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shooter.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var lastFrameTime = 0L
    private var lastShotTime = 0L
    private val shotInterval = 300L // ms
    private var spawnTimer = 0L
    private val spawnInterval = 2000L // ms

    private var moveDirection = Vector2D()

    fun updateMoveDirection(direction: Vector2D) {
        moveDirection = direction
    }

    fun updateViewportSize(width: Float, height: Float) {
        _gameState.update { it.copy(viewportSize = Vector2D(width, height)) }
    }

    fun onFrame(frameTimeNanos: Long) {
        if (_gameState.value.isGameOver) return

        if (lastFrameTime == 0L) {
            lastFrameTime = frameTimeNanos
            return
        }

        val deltaTime = (frameTimeNanos - lastFrameTime) / 1_000_000f // ms
        lastFrameTime = frameTimeNanos

        updateGame(deltaTime)
    }

    private fun updateGame(deltaTime: Float) {
        _gameState.update { state ->
            // 1. Update Player
            val speed = 0.3f * deltaTime
            val newPlayerPos = state.player.position + (moveDirection * speed)
            
            // Constrain player to screen
            val constrainedPos = Vector2D(
                newPlayerPos.x.coerceIn(0f, state.viewportSize.x),
                newPlayerPos.y.coerceIn(0f, state.viewportSize.y)
            )
            val updatedPlayer = state.player.copy(position = constrainedPos)

            // 2. Spawn Enemies
            spawnTimer += deltaTime.toLong()
            var currentEnemies = state.enemies.toMutableList()
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0
                currentEnemies.add(spawnEnemy(state.viewportSize))
            }

            // 3. Auto Shooting
            val currentTime = System.currentTimeMillis()
            var currentBullets = state.bullets.toMutableList()
            if (currentTime - lastShotTime >= shotInterval) {
                val target = findClosestEnemy(updatedPlayer.position, currentEnemies)
                if (target != null) {
                    val direction = (target.position - updatedPlayer.position).normalize()
                    currentBullets.add(Bullet(updatedPlayer.position, direction * 0.8f))
                    lastShotTime = currentTime
                }
            }

            // 4. Update Bullets
            currentBullets = currentBullets.map { it.copy(position = it.position + (it.velocity * deltaTime)) }
                .filter { it.position.x in 0f..state.viewportSize.x && it.position.y in 0f..state.viewportSize.y }
                .toMutableList()

            // 5. Update Enemies
            currentEnemies = currentEnemies.map { 
                val direction = (updatedPlayer.position - it.position).normalize()
                it.copy(position = it.position + (direction * it.speed * (deltaTime / 16f)))
            }.toMutableList()

            // 6. Collision Detection
            val enemiesToRemove = mutableSetOf<Enemy>()
            val bulletsToRemove = mutableSetOf<Bullet>()
            var newScore = state.score
            var gameOver = state.isGameOver

            for (bullet in currentBullets) {
                for (enemy in currentEnemies) {
                    if (bullet.position.distanceTo(enemy.position) < bullet.radius + enemy.radius) {
                        enemiesToRemove.add(enemy)
                        bulletsToRemove.add(bullet)
                        newScore += 10
                    }
                }
            }

            for (enemy in currentEnemies) {
                if (enemy.position.distanceTo(updatedPlayer.position) < enemy.radius + updatedPlayer.radius) {
                    gameOver = true
                }
            }

            currentEnemies.removeAll(enemiesToRemove)
            currentBullets.removeAll(bulletsToRemove)

            state.copy(
                player = updatedPlayer,
                enemies = currentEnemies,
                bullets = currentBullets,
                score = newScore,
                isGameOver = gameOver
            )
        }
    }

    private fun spawnEnemy(viewport: Vector2D): Enemy {
        val random = Random()
        val side = random.nextInt(4)
        val pos = when (side) {
            0 -> Vector2D(random.nextFloat() * viewport.x, 0f) // Top
            1 -> Vector2D(viewport.x, random.nextFloat() * viewport.y) // Right
            2 -> Vector2D(random.nextFloat() * viewport.x, viewport.y) // Bottom
            else -> Vector2D(0f, random.nextFloat() * viewport.y) // Left
        }
        return Enemy(position = pos)
    }

    private fun findClosestEnemy(playerPos: Vector2D, enemies: List<Enemy>): Enemy? {
        return enemies.minByOrNull { it.position.distanceTo(playerPos) }
    }
    
    fun resetGame() {
        _gameState.value = GameState()
        lastFrameTime = 0
    }
}
