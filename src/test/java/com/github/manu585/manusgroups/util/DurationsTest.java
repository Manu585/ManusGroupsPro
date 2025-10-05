package com.github.manu585.manusgroups.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DurationsTest {
    @Test
    void parsesSimpleUnits() {
        assertThat(Durations.parse("7m")).isEqualTo(Duration.ofMinutes(7));
        assertThat(Durations.parse("1h")).isEqualTo(Duration.ofHours(1));
        assertThat(Durations.parse("4d")).isEqualTo(Duration.ofDays(4));
    }

    @Test
    void parsesCompound() {
        assertThat(Durations.parse("3h52m20s")).isEqualTo(Duration.ofHours(3).plusMinutes(52).plusSeconds(20));
    }

    @Test
    void rejectsBadInputs() {
        assertThatThrownBy(() -> Durations.parse("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Durations.parse("abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Durations.parse("-5s")).isInstanceOf(IllegalArgumentException.class);
    }
}
