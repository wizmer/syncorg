package com.coste.syncorg.orgdata;

import org.junit.Test;

import static org.junit.Assert.*;


public class OrgNodeTimeDateTest {

    @Test
    public void thatAScheduledDateWithWeekdayIsParsed() throws Exception {
        String line = "  SCHEDULED: <2017-03-15 Wed>";
        OrgNodeTimeDate orgNodeTimeDate = new OrgNodeTimeDate(OrgNodeTimeDate.TYPE.Scheduled, line);
        assertEquals(2017, orgNodeTimeDate.year);
        assertEquals(2, orgNodeTimeDate.monthOfYear);
        assertEquals(15, orgNodeTimeDate.dayOfMonth);
        assertEquals(OrgNodeTimeDate.TYPE.Scheduled ,orgNodeTimeDate.type);
    }

    @Test
    public void thatADeadlineDateWithWeekdayIsParsed() throws Exception {
        String line = "  DEADLINE: <2017-01-15 Wed>";
        OrgNodeTimeDate orgNodeTimeDate = new OrgNodeTimeDate(OrgNodeTimeDate.TYPE.Deadline, line);
        assertEquals(2017, orgNodeTimeDate.year);
        assertEquals(0, orgNodeTimeDate.monthOfYear);
        assertEquals(15, orgNodeTimeDate.dayOfMonth);
    }

    @Test
    public void thatADeadlineAndScheduledDateWithAndWithoutWeekdayAreParsed() throws Exception {
        String line = "  DEADLINE: <2017-04-03 Mon> SCHEDULED: <2017-03-30>";
        OrgNodeTimeDate orgNodeTimeDate = new OrgNodeTimeDate(OrgNodeTimeDate.TYPE.Scheduled, line);
        assertEquals(2017, orgNodeTimeDate.year);
        assertEquals(2, orgNodeTimeDate.monthOfYear);
        assertEquals(30, orgNodeTimeDate.dayOfMonth);

        orgNodeTimeDate = new OrgNodeTimeDate(OrgNodeTimeDate.TYPE.Deadline, line);
        assertEquals(2017, orgNodeTimeDate.year);
        assertEquals(3, orgNodeTimeDate.monthOfYear);
        assertEquals(3, orgNodeTimeDate.dayOfMonth);
    }
}