package com.railson.recipes.core.ui.format

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class TimeFormatTest {

    @Test
    fun `less than a minute shows seconds`() {
        assertThat(30.seconds.formatTime()).isEqualTo("30 s")
    }

    @Test
    fun `whole minutes show no seconds`() {
        assertThat(60.seconds.formatTime()).isEqualTo("1 min")
    }

    @Test
    fun `minutes and seconds show both`() {
        assertThat(90.seconds.formatTime()).isEqualTo("1 min 30 s")
    }

    @Test
    fun `whole hours show only hours`() {
        assertThat(3600.seconds.formatTime()).isEqualTo("1 h")
    }

    @Test
    fun `hours and minutes show both`() {
        assertThat(5400.seconds.formatTime()).isEqualTo("1 h 30 min")
    }

    @Test
    fun `only the two largest units are shown`() {
        assertThat(3690.seconds.formatTime()).isEqualTo("1 h 1 min")
    }

    @Test
    fun `seconds after a zero minute unit are dropped`() {
        assertThat(3630.seconds.formatTime()).isEqualTo("1 h")
    }

    @Test
    fun `zero shows zero seconds, never an empty text`() {
        assertThat(0.seconds.formatTime()).isEqualTo("0 s")
    }
}
