package br.org.matrix.timesheet.project;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import br.org.matrix.timesheet.time.Allocation;

public class WorkUnit {

	private LocalDate date;
	private LocalTime startTime;
	private LocalTime stopTime;
	private Allocation allocation;
}
