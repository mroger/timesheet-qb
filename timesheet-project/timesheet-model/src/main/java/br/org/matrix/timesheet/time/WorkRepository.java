package br.org.matrix.timesheet.time;

import java.util.List;

import org.joda.time.LocalDate;

public interface WorkRepository {

	void store(WorkPeriod workPeriod);

	List<WorkPeriod> findByDate(LocalDate date);
}
