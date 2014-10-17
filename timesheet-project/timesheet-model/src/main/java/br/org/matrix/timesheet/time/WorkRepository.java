package br.org.matrix.timesheet.time;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.Employee;
import br.org.matrix.timesheet.project.Project;

public interface WorkRepository {

	void store(WorkPeriod workPeriod);

	List<WorkPeriod> findByDate(LocalDate date);

	List<WorkPeriod> findByEmployee(Employee employee);

	List<WorkPeriod> findByProject(Project project);

	List<WorkPeriod> findByDateAndEmployee(LocalDate yesterday, Employee employee);

	List<WorkPeriod> findByDateInterval(LocalDate startDate, LocalDate finishDate);

	List<WorkPeriod> findByDateIntervalAndEmployee(LocalDate startDate, LocalDate finishDate, Employee employee);

	List<WorkPeriod> findByClient(Client client);

	List<WorkPeriod> findByDateAndClient(LocalDate date, Client client);

	List<WorkPeriod> findByDateIntervalAndClient(LocalDate startDate, LocalDate finishDate, Client client);

	List<WorkPeriod> findByMonthByClient(int month, Client client);

	List<WorkPeriod> findByMonthByEmployee(int month, Employee employee);

	List<WorkPeriod> findByMonthInterval(int startMonth, int finishMonth);

	List<WorkPeriod> findByMonthIntervalByClient(int startMonth, int finishMonth, Client client);

	List<WorkPeriod> findByMonthIntervalByEmployee(int startMonth, int finishMonth, Employee employee);

	/**
	 * Updates workperiod interval. 
	 * 
	 * @param workedInterval
	 * @param startTime
	 * @param finishTime
	 */
	void update(WorkPeriod workedInterval, LocalTime startTime, LocalTime finishTime);

	void delete(WorkPeriod workPeriod);

	/**
	 * Returns total number of worked minutes of all employees in a day. 
	 * 
	 * @param date
	 * @return
	 */
	int getWorkedMinutesByDate(LocalDate date);

	int getWorkedMinutesByDateByEmployee(LocalDate date, Employee employee);

	int getWorkedMinutesByDateInterval(LocalDate startDate, LocalDate finishDate);

	int getWorkedMinutesByDateIntervalAndEmployee(LocalDate startDate, LocalDate finishDate, Employee employee);

	int getWorkedMinutesByMonth(int month);

	int getWorkedMinutesByMonthByEmployee(int month, Employee employee);

	int getWorkedMinutesByMonthByClient(int month, Client client);

	int getWorkedMinutesByMonthByClientByEmployee(int month, Client client,	Employee employee);

	int getWorkedMinutesByMonthInterval(int startMonth, int finishMonth);

	int getWorkedMinutesByMonthIntervalByEmployee(int startMonth, int finishMonth, Employee employee);

	int getWorkedMinutesByMonthIntervalByClient(int startMonth, int finishMonth, Client client);

	int getWorkedMinutesByMonthIntervalByClientByEmployee(int startMonth, int finishMonth, Client client, Employee employee);

	int getWorkedMinutesByProject(Project project);

	int getWorkedMinutesByMonthIntervalByProject(int startMonth, int finishMonth, Project project);

	int getWorkedMinutesByProjectByEmployee(Project project, Employee employee);

	int getWorkedMinutesByMonthIntervalByProjectByEmployee(int startMonth, int finishMonth, Project project, Employee employee);
	
}
