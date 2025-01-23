package tesdtKt

import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferStrategy
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JFrame
import javax.swing.SwingUtilities

class Game : Canvas(), Runnable {

    companion object {
        const val NAME = "UNK Screen (Kotlin code, Engine JDK 8)"
        const val HEIGHT = 240
        const val WIDTH = HEIGHT * 16 / 9
        private val device = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices[0]

        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { // Ensure UI creation on EDT
                val game = Game()
                game.preferredSize = Dimension(WIDTH * 2, HEIGHT * 2)
                game.minimumSize = Dimension(WIDTH * 2, HEIGHT * 2)
                game.maximumSize = Dimension(WIDTH * 2, HEIGHT * 2)

                val frame = JFrame(NAME)
                frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                frame.layout = BorderLayout()
                frame.add(game)
                frame.pack()
                frame.isResizable = true
                frame.setLocationRelativeTo(null)
                frame.isVisible = true

                var fullscreen = false
                frame.addKeyListener(object : KeyAdapter() {
                    override fun keyPressed(e: KeyEvent) {
                        val keyCode = e.keyCode
                        if (keyCode == KeyEvent.VK_F11) {
                            fullscreen = !fullscreen
                            if (fullscreen) {
                                println("Entering Fullscreen!")
                                device.fullScreenWindow = frame
                            } else {
                                println("Exiting Fullscreen!")
                                device.fullScreenWindow = null
                                frame.isVisible = true
                                frame.pack()
                            }
                        } else if (keyCode == KeyEvent.VK_ESCAPE) {
                            println("ESC is pressed!")
                            System.exit(0)
                        }
                    }
                })
                game.start()
            }
        }
    }

    private val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
    private val pixels = (image.raster.dataBuffer as DataBufferInt).data
    private var running = false
    private var tickCount = 0

    init {
        // Initialize the Canvas settings here, before it's added to the frame
        ignoreRepaint = true // For more control over rendering
    }

    fun start() {
        running = true
        Thread(this).start()
    }

    fun stop() {
        running = false
    }

    override fun run() {
        var lastTime = System.nanoTime()
        var unprocessed = 0.0
        val nsPerTick = 1000000000.0 / 60.0
        var frames = 0
        var lastTimer = System.currentTimeMillis()

        while (running) {
            val now = System.nanoTime()
            unprocessed += (now - lastTime) / nsPerTick
            lastTime = now
            while (unprocessed >= 1) {
                tick()
                unprocessed -= 1
            }
            render() // Call render directly
            frames++

            if (System.currentTimeMillis() - lastTimer > 1000) {
                lastTimer += 1000
                println("$frames fps")
                frames = 0
            }
        }
    }

    fun tick() {
        tickCount++
    }

    private fun render() {
        var bs = bufferStrategy
        if (bs == null) {
            createBufferStrategy(3) // Create buffer strategy if it doesn't exist
            return
        }

        val g = bs.drawGraphics as Graphics2D // Use Graphics2D for more features

        try {
            // Clear the screen (important!)
            g.color = Color.BLACK
            g.fillRect(0, 0, width, height)

            // Modify pixels array here (example: fill with a color based on tickCount)
            for (i in pixels.indices) {
                pixels[i] = i + tickCount // Simple example
            }

            // Draw the image scaled to the Canvas size
            g.drawImage(image, 0, 0, width, height, null)

        } finally {
            g.dispose() // Always dispose graphics objects
            bs.show() // Show the buffer
        }
    }
}
