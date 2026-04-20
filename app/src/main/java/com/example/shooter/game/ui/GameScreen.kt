package com.example.shooter.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shooter.game.model.Vector2D

@Composable
fun GameScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    // Game Loop
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                viewModel.onFrame(frameTimeNanos)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { size ->
                viewModel.updateViewportSize(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        // Game World
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Player
            drawCircle(
                color = gameState.player.color,
                radius = gameState.player.radius,
                center = Offset(gameState.player.position.x, gameState.player.position.y)
            )

            // Draw Enemies
            gameState.enemies.forEach { enemy ->
                drawCircle(
                    color = enemy.color,
                    radius = enemy.radius,
                    center = Offset(enemy.position.x, enemy.position.y)
                )
            }

            // Draw Bullets
            gameState.bullets.forEach { bullet ->
                drawCircle(
                    color = bullet.color,
                    radius = bullet.radius,
                    center = Offset(bullet.position.x, bullet.position.y)
                )
            }
        }

        // HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(text = "Score: ${gameState.score}", color = Color.White, fontSize = 24.sp)
        }

        // Joystick
        Joystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(48.dp)
                .size(150.dp),
            onMove = { direction ->
                viewModel.updateMoveDirection(direction)
            }
        )

        // Game Over Overlay
        if (gameState.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "GAME OVER", color = Color.Red, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Final Score: ${gameState.score}", color = Color.White, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Row {
                        Button(onClick = { viewModel.resetGame() }) {
                            Text("Restart")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = onExit) {
                            Text("Menu")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Joystick(
    modifier: Modifier = Modifier,
    onMove: (Vector2D) -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    val radius = 150f // Define visual radius for calculation

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        offset = Offset.Zero
                        onMove(Vector2D(0f, 0f))
                    },
                    onDragCancel = {
                        offset = Offset.Zero
                        onMove(Vector2D(0f, 0f))
                    },
                    onDrag = { change, dragAmount ->
                        val newOffset = offset + dragAmount
                        val dist = newOffset.getDistance()
                        
                        offset = if (dist > radius) {
                            newOffset * (radius / dist)
                        } else {
                            newOffset
                        }
                        
                        val normalized = Vector2D(offset.x, offset.y).normalize()
                        onMove(normalized)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = size.width / 2,
                center = center
            )
            
            // Handle
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = size.width / 4,
                center = center + offset
            )
        }
    }
}
