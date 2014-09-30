package br.org.matrix.timesheet.time;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import br.org.matrix.timesheet.project.Employee;

public class WorkPeriodTest {

	@Test(expected=IllegalStateException.class)
	public void create_ClientNotSet() {
		new WorkPeriod(today(), now(), now(), null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void create_DateNotSet() {
		new WorkPeriod(null, now(), now(), new Employee(1, "emp01"));
	}
	
	@Test(expected=IllegalStateException.class)
	public void create_StartTimeNotSet() {
		new WorkPeriod(today(), null, now(), new Employee(1, "emp01"));
	}
	
	@Test(expected=IllegalStateException.class)
	public void create_StopTimeNotSet() {
		new WorkPeriod(today(), now(), null, new Employee(1, "emp01"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void create_StartTimeLessThanStopTime() {
		new WorkPeriod(today(), time(15, 0), time(11, 0), new Employee(1, "emp01"));
	}
	
	@Test
	public void create() {
		new WorkPeriod(today(), time(11, 0), time(15, 0), new Employee(1, "emp01"));
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
