package br.org.matrix.timesheet.time;

import java.util.List;

import org.joda.time.LocalDate;

import br.org.matrix.timesheet.project.Employee;

public interface WorkRepository {

	void store(WorkPeriod workPeriod);

	List<WorkPeriod> findByDate(LocalDate date);

	List<WorkPeriod> findByEmployee(Employee employee);

	List<WorkPeriod> findByDateAndEmployee(LocalDate yesterday, Employee employee1);

	List<WorkPeriod> findByDateInterval(LocalDate startDate, LocalDate finishDate);

	List<WorkPeriod> findByDateIntervalAndEmployee(LocalDate startDate, LocalDate finishDate, Employee employee);
}
