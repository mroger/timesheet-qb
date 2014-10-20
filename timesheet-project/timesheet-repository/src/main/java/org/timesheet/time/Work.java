package org.timesheet.time;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.DateIntervalOvelapsException;
import br.org.matrix.timesheet.project.Employee;
import br.org.matrix.timesheet.project.Project;
import br.org.matrix.timesheet.time.WorkPeriod;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Work implements WorkRepository {

	private List<WorkPeriod> workUnits;
	private Map<LocalDate, List<WorkPeriod>> workUnitsByDate;
	private Map<Employee, List<WorkPeriod>> workUnitsByEmployee;
	private Map<Client, List<WorkPeriod>> workUnitsByClient;
	private Map<Project, List<WorkPeriod>> workUnitsByProject;
	
	public Work() {
		workUnits = Lists.newArrayList();
		workUnitsByDate = Maps.newHashMap();
		workUnitsByEmployee = Maps.newHashMap();
		workUnitsByClient = Maps.newHashMap();
		workUnitsByProject = Maps.newHashMap();
	}

	public void store(WorkPeriod workPeriod) {
		workUnits.add(workPeriod);
		
		storeWorkPeriodByDate(workPeriod);
		
		storeWorkPeriodByEmployee(workPeriod);
		
		storeWorkPeriodByClient(workPeriod);
		
		storeWorkPeriodByProject(workPeriod);
	}

	private void storeWorkPeriodByProject(WorkPeriod workPeriod) {
		store(workUnitsByProject, workPeriod, workPeriod.getProject());
	}

	private void storeWorkPeriodByEmployee(WorkPeriod workPeriod) {
		checkIfOverlapsForEmployee(workPeriod);
		store(workUnitsByEmployee, workPeriod, workPeriod.getEmployee());
	}

	private void checkIfOverlapsForEmployee(WorkPeriod workPeriod) {
		List<WorkPeriod> workPeriodsByEmployee = 
			new WorkPeriodFilter<Employee>(workUnitsByEmployee, workPeriod.getEmployee()).getResult();
		if (workPeriodsByEmployee != null) {
			if (workPeriod.overlaps(workPeriodsByEmployee)) {
				throw new DateIntervalOvelapsException(workPeriod.getEmployee().getId(), 
						workPeriod.getEmployee().getName(), workPeriod.getDate(), 
						workPeriod.getStartTime(), workPeriod.getStopTime());
			}
		}
		
	}

	private void storeWorkPeriodByClient(WorkPeriod workPeriod) {
		store(workUnitsByClient, workPeriod, workPeriod.getClient());
	}

	private void storeWorkPeriodByDate(WorkPeriod workPeriod) {
		store(workUnitsByDate, workPeriod, workPeriod.getDate());
	}
	
	private <T> void store(Map<T, List<WorkPeriod>> workUnits, WorkPeriod workPeriod, T key) {
		List<WorkPeriod> workPeriods = workUnits.get(key);
		if (workPeriods == null) {
			workPeriods = Lists.newArrayList();
			workPeriods.add(workPeriod);
			workUnits.put(key, workPeriods);
		} else {
			workPeriods.add(workPeriod);
		}
	}
	
	//TODO Write tests
	public void delete(WorkPeriod workPeriod) {
		workUnits.remove(workPeriod);
		
		removeItem(workPeriod, workUnitsByDate);
		
		removeItem(workPeriod, workUnitsByEmployee);
		
		removeItem(workPeriod, workUnitsByClient);
		
		removeItem(workPeriod, workUnitsByProject);
	}
	
	private <T> void removeItem(WorkPeriod workPeriod, Map<T, List<WorkPeriod>> workUnits) {
		for(T item : workUnits.keySet()) {
			List<WorkPeriod> workUnits_ = workUnits.get(item);
			if (workUnits_.contains(workPeriod)) {
				workUnits_.remove(workPeriod);
				break;
			}
		}
	}

	public List<WorkPeriod> findByDate(LocalDate date) {
		return new WorkPeriodFilter<LocalDate>(workUnitsByDate, date)
			.getResult();
	}

	public List<WorkPeriod> findByEmployee(Employee employee) {
		return new WorkPeriodFilter<Employee>(workUnitsByEmployee, employee)
			.getResult();
	}

	public List<WorkPeriod> findByProject(Project project) {
		return new WorkPeriodFilter<Project>(workUnitsByProject, project)
			.getResult();
	}

	//TODO Duplicated with getWorkedMinutesByDateByEmployee()
	public List<WorkPeriod> findByDateAndEmployee(final LocalDate date, final Employee employee) {
		return new WorkPeriodFilter<LocalDate>(workUnitsByDate, date)
			.andFilterBy(employee)
			.getResult();
	}

	//TODO Duplicated with getWorkedMinutesByDateInterval()
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<WorkPeriod> findByDateInterval(final LocalDate startDate, final LocalDate finishDate) {
		return new WorkPeriodFilter(workUnits)
			.andFilterBy(startDate, finishDate)
			.getResult();
	}

	//TODO Duplicated with getWorkedMinutesByDateIntervalAndEmployee()
	public List<WorkPeriod> findByDateIntervalAndEmployee(final LocalDate startDate, final LocalDate finishDate, final Employee employee) {
		return new WorkPeriodFilter<Employee>(workUnitsByEmployee, employee)
			.andFilterBy(startDate, finishDate)
			.getResult();
	}

	public List<WorkPeriod> findByClient(Client client) {
		return new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.getResult();
	}

	public List<WorkPeriod> findByDateAndClient(LocalDate date, Client client) {
		return new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(date)
			.getResult();
	}

	public List<WorkPeriod> findByDateIntervalAndClient(LocalDate startDate, LocalDate finishDate, Client client) {
		return new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(startDate, finishDate)
			.getResult();
	}

	public List<WorkPeriod> findByMonthByClient(int month, Client client) {
		return new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(month)
			.getResult();
	}

	public List<WorkPeriod> findByMonthByEmployee(int month, Employee employee) {
		return new WorkPeriodFilter<Employee>(workUnitsByEmployee, employee)
			.andFilterBy(month)
			.getResult();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<WorkPeriod> findByMonthInterval(int startMonth, int finishMonth) {
		return new WorkPeriodFilter(workUnits)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
	}

	public List<WorkPeriod> findByMonthIntervalByClient(int startMonth, int finishMonth, Client client) {
		return new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
	}

	public List<WorkPeriod> findByMonthIntervalByEmployee(int startMonth, int finishMonth, Employee employee) {
		return new WorkPeriodFilter<Employee>(workUnitsByEmployee, employee)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
	}

	public void update(final WorkPeriod workPeriod, final LocalTime startTime, final LocalTime finishTime) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Employee>(workUnitsByEmployee, workPeriod.getEmployee())
			.andFilterBy(workPeriod)
			.getResult();
		
		checkArgument(!isEmpty(workPeriods), "Trying to change interval that does not exist.");
		//TODO Check for unique WorkPeriod
		WorkPeriod storedWorkPeriod = workPeriods.get(0);
		delete(storedWorkPeriod);
		WorkPeriod newWorkPeriod = new WorkPeriod(
				storedWorkPeriod.getDate(), startTime, finishTime, storedWorkPeriod.getAllocation());
		store(newWorkPeriod);
	}

	public int getWorkedMinutesByDate(LocalDate date) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<LocalDate>(workUnitsByDate, date)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByDateByEmployee(LocalDate date, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<LocalDate>(workUnitsByDate, date)
			.andFilterBy(employee)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int getWorkedMinutesByDateInterval(LocalDate startDate, LocalDate finishDate) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter(workUnits)
			.andFilterBy(startDate, finishDate)
			.getResult();		
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByDateIntervalAndEmployee(LocalDate startDate, LocalDate finishDate, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Employee>(workUnitsByEmployee, employee)
			.andFilterBy(startDate, finishDate)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	@SuppressWarnings("unchecked")
	public int getWorkedMinutesByMonth(int month) {
		@SuppressWarnings("rawtypes")
		List<WorkPeriod> workPeriods = new WorkPeriodFilter(workUnits)
			.andFilterBy(month)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthByEmployee(int month, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Employee>(workUnitsByEmployee, employee)
			.andFilterBy(month)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthByClient(int month, Client client) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(month)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthByClientByEmployee(int month, Client client, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(month)
			.andFilterBy(employee)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	//TODO Get rid of this @SuppressWarnings annotation
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int getWorkedMinutesByMonthInterval(int startMonth, int finishMonth) {
		//TODO Get rid of this @SuppressWarnings annotation
		List<WorkPeriod> workPeriods = new WorkPeriodFilter(workUnits)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthIntervalByEmployee(int startMonth, int finishMonth, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Employee>(workUnitsByEmployee, employee)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthIntervalByClient(int startMonth, int finishMonth, Client client) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthIntervalByClientByEmployee(int startMonth, int finishMonth, Client client, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Client>(workUnitsByClient, client)
			.andFilterBy(startMonth, finishMonth)
			.andFilterBy(employee)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthIntervalByProject(int startMonth, int finishMonth, Project project) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Project>(workUnitsByProject, project)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByProject(Project project) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Project>(workUnitsByProject, project)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByMonthIntervalByProjectByEmployee(int startMonth, int finishMonth, Project project, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Project>(workUnitsByProject, project)
			.andFilterBy(employee)
			.andFilterBy(startMonth, finishMonth)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	public int getWorkedMinutesByProjectByEmployee(Project project, Employee employee) {
		List<WorkPeriod> workPeriods = new WorkPeriodFilter<Project>(workUnitsByProject, project)
			.andFilterBy(employee)
			.getResult();
		
		return calculateTotalWorkedMinutes(workPeriods);
	}

	private int calculateTotalWorkedMinutes(List<WorkPeriod> workPeriods) {
		int total = 0;
		for (WorkPeriod workPeriod : workPeriods) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

}
