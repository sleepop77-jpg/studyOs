package com.example.studyos.ui.lockdown

import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size

fun bustedParties(): List<Party> {
    return listOf(
        Party(
            speed = 1f,
            maxSpeed = 16f,
            damping = 0.9f,
            angle = 90,
            spread = 120,
            colors = listOf(
                0xFFFFD700.toInt(),
                0xFFC41C3B.toInt(),
                0xFFFF5252.toInt(),
                0xFF8B0000.toInt()
            ),
            shapes = listOf(Shape.Square, Shape.Circle),
            size = listOf(Size(12), Size(16)),
            timeToLive = 3000L,
            fadeOutEnabled = true,
            emitter = Emitter(400L, TimeUnit.MILLISECONDS).perSecond(120),
            position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
        )
    )
}
