//package com.android.lastproj
//
//import com.badlogic.gdx.ApplicationAdapter
//
///** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
//class MainGame : ApplicationAdapter()

package com.android.lastproj

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport

class MainGame : Game() {
    private lateinit var stage: Stage
    private lateinit var skin: Skin

    override fun create() {
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        // 1. Create a dynamic, code-only skin
        skin = Skin()

        // 2. Generate a default system font
        val font = BitmapFont()
        skin.add("default", font)

        // 3. Programmatically paint a plain color texture for the button background
        val pixmap = Pixmap(100, 100, Pixmap.Format.RGBA8888).apply {
            setColor(Color.DARK_GRAY)
            fill()
        }
        val buttonTexture = Texture(pixmap)
        pixmap.dispose() // Clean up memory native resource

        // 4. Register the generated texture into our custom skin
        skin.add("button-up", buttonTexture)

        // 5. Configure the button style using our generated items
        val buttonStyle = TextButtonStyle().apply {
            up = skin.newDrawable("button-up")
            down = skin.newDrawable("button-up", Color.LIGHT_GRAY) // Tint when clicked
            this.font = skin.getFont("default")
            fontColor = Color.WHITE
        }
        skin.add("default", buttonStyle)

        // 6. Arrange the UI Layout using a Table grid
        val table = Table().apply {
            setFillParent(true)
            center() // Places items perfectly in the center of the screen
        }
        stage.addActor(table)

        // 7. Add a functioning test button
        val testButton = TextButton("CLICK ME", skin)
        testButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.log("UI_TEST", "The button works perfectly!")
                testButton.setText("SUCCESS!")
            }
        })

        // Add to view layout grid (Width: 250 units, Height: 100 units)
        table.add(testButton).width(250f).height(100f)
    }

    override fun render() {
        // Clear the background canvas with a solid background color
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.25f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Draw and update the framework stage
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        skin.dispose()
    }
}

