package br.org.matrix.timesheet.time;

import static br.org.matrix.timesheet.util.TimesheetUtil.createAllocation;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import br.org.matrix.timesheet.project.Allocation;

public class WorkPeriodTest {

	@Test(expected=IllegalStateException.class)
	public void create_ClientNotSet() {
		new WorkPeriod(today(), now(), now(), null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void create_DateNotSet() {
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		new WorkPeriod(null, now(), now(), allocation);
	}

	@Test(expected=IllegalStateException.class)
	public void create_StartTimeNotSet() {
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		new WorkPeriod(today(), null, now(), allocation);
	}
	
	@Test(expected=IllegalStateException.class)
	public void create_StopTimeNotSet() {
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		new WorkPeriod(today(), now(), null, allocation);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void create_StartTimeLessThanStopTime() {
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		new WorkPeriod(today(), time(15, 0), time(11, 0), allocation);
	}
	
	@Test
	public void create() {
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		new WorkPeriod(today(), time(11, 0), time(15, 0), allocation);
	}

	private LocalTime time(int hour, int minute) {
		return new LocalTime(hour, minute);
	}

	private LocalDate today() {
		return new LocalDate();
	}

	private LocalTime now() {
		return new LocalTime();
	}
}
