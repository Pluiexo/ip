package duke.parser;

import duke.DukeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateHandlerTest {

    @Test
    public void check_date_success() throws DukeException {
        assertEquals(LocalDate.of(2023, 4, 12),
                DateHandler.checkDate("12-04-23").orElse(LocalDate.of(1999, 1, 1)));
    }

    @Test
    public void checkTime_success() {
        assertEquals(LocalTime.of(18, 0),
                DateHandler.checkTime("1800 12-04-23").orElse(LocalTime.of(0, 0)));
    }
}