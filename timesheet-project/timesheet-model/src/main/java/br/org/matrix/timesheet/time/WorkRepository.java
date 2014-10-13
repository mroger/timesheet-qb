package br.org.matrix.timesheet.time;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.Employee;

public interface WorkRepository {

	void store(WorkPeriod workPeriod);

	List<WorkPeriod> findByDate(LocalDate date);

	List<WorkPeriod> findByEmployee(Employee employee);

	List<WorkPeriod> findByDateAndEmployee(LocalDate yesterday, Employee employee);

	List<WorkPeriod> findByDateInterval(LocalDate startDate, LocalDate finishDate);

	List<WorkPeriod> findByDateIntervalAndEmployee(LocalDate startDate, LocalDate finishDate, Employee employee);

	List<WorkPeriod> findByClient(Client client);

	List<WorkPeriod> findByDateAndClient(LocalDate date, Client client);

	List<WorkPeriod> findByDateIntervalAndClient(LocalDate startDate, LocalDate finishDate, Client client);

	List<WorkPeriod> findByMonthAndClient(int month, Client client);

	List<WorkPeriod> findByMonthIntervalAndClient(int startMonth, int finishMonth, Client client);

	/**
	 * Updates workperiod interval. 
	 * 
	 * @param workedInterval
	 * @param startTime
	 * @param finishTime
	 */
	void update(WorkPeriod workedInterval, LocalTime startTime, LocalTime finishTime);

	void delete(WorkPeriod workPeriod);
	
}
