package br.org.matrix.timesheet.project;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class DateIntervalOvelapsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DateIntervalOvelapsException() {
		super();
	}
	
	public DateIntervalOvelapsException(Integer employeeId, String employeeName, LocalDate date, LocalTime startTime, LocalTime stopTime) {
		// TODO Use better formatter
		super("Interval " + date + " - " + startTime + " -> " + stopTime + " overlaps existing interval for employee " + employeeId + " - " + employeeName);
	}

}
