package com.railson.recipes.core.ui.format

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class TimeFormatTest {

    @Test
    fun `less than a minute shows seconds`() {
        assertThat(30.seconds.formatTime()).isEqualTo("30s")
    }

    @Test
    fun `whole minutes show no seconds`() {
        assertThat(60.seconds.formatTime()).isEqualTo("1m")
    }

    @Test
    fun `minutes and seconds show both`() {
        assertThat(90.seconds.formatTime()).isEqualTo("1m 30s")
    }

    @Test
    fun `whole minutes above ten show no seconds`() {
        assertThat(1800.seconds.formatTime()).isEqualTo("30m")
    }

    @Test
    fun `whole hours show only hours`() {
        assertThat(3600.seconds.formatTime()).isEqualTo("1h")
    }

    @Test
    fun `hours and minutes show both`() {
        assertThat(5400.seconds.formatTime()).isEqualTo("1h 30m")
    }

    @Test
    fun `only the two largest units are shown`() {
        assertThat(3690.seconds.formatTime()).isEqualTo("1h 1m")
    }

    @Test
    fun `seconds after a zero minute unit are dropped`() {
        assertThat(3630.seconds.formatTime()).isEqualTo("1h")
    }

    @Test
    fun `zero shows zero seconds, never an empty text`() {
        assertThat(0.seconds.formatTime()).isEqualTo("0s")
    }
}
