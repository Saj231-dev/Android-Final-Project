package com.android.lastproj

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.ScreenUtils

class MainGame : ApplicationAdapter() {
    enum class State { START, DIFFICULTY, PLAYING, GAME_OVER, WIN }
    private var gameState = State.START

    private lateinit var batch: SpriteBatch
    private lateinit var playerTexture: Texture
    private lateinit var ballTexture: Texture
    private lateinit var catchSound: Sound
    private lateinit var font: BitmapFont
    private val textLayout = GlyphLayout()

    private lateinit var playerRect: Rectangle
    private lateinit var ballRect: Rectangle

    private var baseBallSpeed = 0f
    private var currentBallSpeed = 0f
    private var score = 0

    private var speedMultiplier = 1.0f

    private var targetScore = 10
    private var chosenDifficultyName = "EASY"

    private lateinit var easyBtn: Rectangle
    private lateinit var medBtn: Rectangle
    private lateinit var hardBtn: Rectangle

    private val monotoneColor = Color(0.2f, 0.22f, 0.25f, 1f)
    private val vibrantColor = Color(0.1f, 0.6f, 0.6f, 1f)
    private val activeBgColor = Color()

    override fun create() {
        batch = SpriteBatch()
        playerTexture = Texture("player.png")
        ballTexture = Texture("ball.png")
        catchSound = Gdx.audio.newSound(Gdx.files.internal("catch.wav"))

        font = BitmapFont()
        font.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        font.data.setScale(4f)

        val playerSize = Gdx.graphics.width * 0.20f
        playerRect = Rectangle(Gdx.graphics.width / 2f - playerSize / 2f, Gdx.graphics.height * 0.05f, playerSize, playerSize)

        val ballSize = Gdx.graphics.width * 0.12f
        ballRect = Rectangle(0f, 0f, ballSize, ballSize)

        baseBallSpeed = Gdx.graphics.height * 0.35f

        val btnW = Gdx.graphics.width * 0.4f
        val btnH = Gdx.graphics.height * 0.1f
        val btnX = Gdx.graphics.width / 2f - btnW / 2f

        easyBtn = Rectangle(btnX, Gdx.graphics.height * 0.6f, btnW, btnH)
        medBtn = Rectangle(btnX, Gdx.graphics.height * 0.45f, btnW, btnH)
        hardBtn = Rectangle(btnX, Gdx.graphics.height * 0.3f, btnW, btnH)

        resetBall()
    }

    override fun render() {
        handleStateLogic()

        when (gameState) {
            State.START, State.DIFFICULTY -> ScreenUtils.clear(0.12f, 0.12f, 0.2f, 1f)
            State.GAME_OVER -> ScreenUtils.clear(0.6f, 0.1f, 0.1f, 1f)
            State.WIN -> ScreenUtils.clear(0.1f, 0.5f, 0.1f, 1f)
            State.PLAYING -> {
                val progress = score.toFloat() / targetScore.toFloat()
                activeBgColor.set(monotoneColor)
                activeBgColor.lerp(vibrantColor, progress)
                ScreenUtils.clear(activeBgColor)
            }
        }

        batch.begin()

        if (gameState == State.PLAYING) {
            batch.color = Color.WHITE
            batch.draw(playerTexture, playerRect.x, playerRect.y, playerRect.width, playerRect.height)

            batch.color = Color.ORANGE
            batch.draw(ballTexture, ballRect.x, ballRect.y, ballRect.width, ballRect.height)

            batch.color = Color.WHITE
            font.color = Color.WHITE
            font.draw(batch, "Score: $score/$targetScore", 50f, Gdx.graphics.height - 50f)
        }

        batch.color = Color.WHITE
        drawMenuTexts()

        batch.end()
    }

    private fun handleStateLogic() {
        when (gameState) {
            State.START -> {
                if (Gdx.input.justTouched()) {
                    gameState = State.DIFFICULTY
                }
            }
            State.DIFFICULTY -> {
                if (Gdx.input.justTouched()) {
                    val touchX = Gdx.input.x.toFloat()
                    val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

                    if (easyBtn.contains(touchX, touchY)) {
                        speedMultiplier = 1.0f
                        targetScore = 10
                        chosenDifficultyName = "EASY"
                        startGame()
                    } else if (medBtn.contains(touchX, touchY)) {
                        speedMultiplier = 1.6f
                        targetScore = 20
                        chosenDifficultyName = "MEDIUM"
                        startGame()
                    } else if (hardBtn.contains(touchX, touchY)) {
                        speedMultiplier = 2.3f
                        targetScore = 30
                        chosenDifficultyName = "HARD"
                        startGame()
                    }
                }
            }
            State.PLAYING -> {
                handleInput()
                updateBall(Gdx.graphics.deltaTime)
            }
            State.GAME_OVER -> {
                if (Gdx.input.justTouched()) {
                    gameState = State.START
                }
            }
            State.WIN -> {
                if (Gdx.input.justTouched()) {
                    gameState = State.START
                }
            }
        }
    }

    private fun startGame() {
        score = 0
        currentBallSpeed = baseBallSpeed * speedMultiplier
        resetBall()
        gameState = State.PLAYING
    }

    private fun drawMenuTexts() {
        when (gameState) {
            State.START -> {
                font.color = Color.CYAN
                drawCenteredText("TAP TO START", Gdx.graphics.height * 0.50f)

                font.color = Color.GOLD
                drawCenteredText("BUCKET BLITZ", Gdx.graphics.height * 0.70f)

                font.color = Color.GRAY
                drawCenteredText("Catch the falling spheres to win!", Gdx.graphics.height * 0.35f)
            }
            State.DIFFICULTY -> {
                font.color = Color.WHITE
                drawCenteredText("SELECT DIFFICULTY", Gdx.graphics.height * 0.75f)

                font.color = Color.GREEN
                drawCenteredText("EASY (Goal: 10)", easyBtn.y + easyBtn.height / 2f)

                font.color = Color.YELLOW
                drawCenteredText("MEDIUM (Goal: 20)", medBtn.y + medBtn.height / 2f)

                font.color = Color.RED
                drawCenteredText("HARD (Goal: 30)", hardBtn.y + hardBtn.height / 2f)
            }
            State.GAME_OVER -> {
                font.color = Color.WHITE
                drawCenteredText("GAME OVER", Gdx.graphics.height * 0.65f)
                drawCenteredText("Final Score: $score", Gdx.graphics.height * 0.55f)
                font.color = Color.LIGHT_GRAY
                drawCenteredText("Tap to restart", Gdx.graphics.height * 0.45f)
            }
            State.WIN -> {
                font.color = Color.WHITE
                drawCenteredText("YOU WIN!", Gdx.graphics.height * 0.70f)
                drawCenteredText("Beat $chosenDifficultyName Mode!", Gdx.graphics.height * 0.58f)
                drawCenteredText("Final Score: $score", Gdx.graphics.height * 0.48f)
                font.color = Color.YELLOW
                drawCenteredText("Tap to play again", Gdx.graphics.height * 0.35f)
            }
            State.PLAYING -> {}
        }
    }

    private fun drawCenteredText(text: String, yPos: Float) {
        textLayout.setText(font, text)
        font.draw(batch, text, Gdx.graphics.width / 2f - textLayout.width / 2f, yPos + textLayout.height / 2f)
    }

    private fun handleInput() {
        if (Gdx.input.isTouched) {
            playerRect.x = Gdx.input.x.toFloat() - playerRect.width / 2f
        }
        if (playerRect.x < 0) playerRect.x = 0f
        if (playerRect.x > Gdx.graphics.width - playerRect.width) {
            playerRect.x = Gdx.graphics.width - playerRect.width
        }
    }

    private fun updateBall(deltaTime: Float) {
        ballRect.y -= currentBallSpeed * deltaTime

        if (ballRect.overlaps(playerRect)) {
            score++
            catchSound.play(1.0f)

            if (score >= targetScore) {
                gameState = State.WIN
            } else {
                resetBall()
                currentBallSpeed += (Gdx.graphics.height * 0.015f) * speedMultiplier
            }
        }

        if (ballRect.y + ballRect.height < 0) {
            gameState = State.GAME_OVER
        }
    }

    private fun resetBall() {
        ballRect.y = Gdx.graphics.height.toFloat()
        ballRect.x = MathUtils.random(0f, Gdx.graphics.width - ballRect.width)
    }

    override fun dispose() {
        batch.dispose()
        playerTexture.dispose()
        ballTexture.dispose()
        catchSound.dispose()
        font.dispose()
    }
}
