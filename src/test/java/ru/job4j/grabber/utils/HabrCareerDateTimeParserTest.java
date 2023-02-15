package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    public void parseWithoutTimeZonesCompleted() {
        String date = "2023-02-14T17:59:14+03:00";
        LocalDateTime result = new HabrCareerDateTimeParser().parse(date);
        assertThat(result).isEqualTo("2023-02-14T17:59:14");
    }

    @Test
    public void parseEmptyStringThenException() {
        assertThatException().isThrownBy(() -> {
            String date = "";
            LocalDateTime result = new HabrCareerDateTimeParser().parse(date);
        }).isInstanceOf(DateTimeParseException.class);
    }
}