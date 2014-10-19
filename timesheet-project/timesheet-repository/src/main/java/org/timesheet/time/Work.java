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
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(workPeriod.getEmployee());
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
		return workUnitsByDate.get(date);
	}

	public List<WorkPeriod> findByEmployee(Employee employee) {
		return workUnitsByEmployee.get(employee);
	}

	public List<WorkPeriod> findByProject(Project project) {
		return workUnitsByProject.get(project);
	}

	//TODO Duplicated with getWorkedMinutesByDateByEmployee()
	public List<WorkPeriod> findByDateAndEmployee(final LocalDate date, final Employee employee) {
		Predicate<WorkPeriod> employeePredicate = createWorkPeriodPredicate(employee);
		List<WorkPeriod> workPeriodsByDate = workUnitsByDate.get(date);
		return Lists.newArrayList(Collections2.filter(workPeriodsByDate, employeePredicate));
	}

	//TODO Duplicated with getWorkedMinutesByDateInterval()
	public List<WorkPeriod> findByDateInterval(final LocalDate startDate, final LocalDate finishDate) {
		Predicate<LocalDate> dateIntervalPredicate = createDatePredicate(startDate, finishDate);
		Map<LocalDate, List<WorkPeriod>> _workUnits = Maps.filterKeys(workUnitsByDate, dateIntervalPredicate);
		List<WorkPeriod> workPeriods = Lists.newArrayList();
		for (LocalDate date : _workUnits.keySet()) {
			List<WorkPeriod> _workPeriods = _workUnits.get(date);
			workPeriods.addAll(_workPeriods);
		}
		return workPeriods;
	}

	//TODO Duplicated with getWorkedMinutesByDateIntervalAndEmployee()
	public List<WorkPeriod> findByDateIntervalAndEmployee(final LocalDate startDate, final LocalDate finishDate, final Employee employee) {
		Predicate<WorkPeriod> dateIntervalPredicate = createWorkPeriodPredicate(startDate, finishDate);
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(employee);
		return Lists.newArrayList(Collections2.filter(workPeriodsByEmployee, dateIntervalPredicate));
	}

	public List<WorkPeriod> findByClient(Client client) {
		return workUnitsByClient.get(client);
	}

	public List<WorkPeriod> findByDateAndClient(LocalDate date, Client client) {
		Predicate<WorkPeriod> datePredicate = createWorkPeriodPredicate(date);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		return Lists.newArrayList(Collections2.filter(workPeriodsByClient, datePredicate));
	}

	public List<WorkPeriod> findByDateIntervalAndClient(LocalDate startDate, LocalDate finishDate, Client client) {
		Predicate<WorkPeriod> dateIntervalPredicate = createWorkPeriodPredicate(startDate, finishDate);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		return Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateIntervalPredicate));
	}

	public List<WorkPeriod> findByMonthByClient(int month, Client client) {
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(month);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		return Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
	}

	public List<WorkPeriod> findByMonthByEmployee(int month, Employee employee) {
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(month);
		List<WorkPeriod> workPeriodsByClient = workUnitsByEmployee.get(employee);
		return Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
	}

	public List<WorkPeriod> findByMonthInterval(int startMonth, int finishMonth) {
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		return Lists.newArrayList(Collections2.filter(workUnits, dateMonthPredicate));
	}

	public List<WorkPeriod> findByMonthIntervalByClient(int startMonth, int finishMonth, Client client) {
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		return Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
	}

	public List<WorkPeriod> findByMonthIntervalByEmployee(int startMonth, int finishMonth, Employee employee) {
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(employee);
		return Lists.newArrayList(Collections2.filter(workPeriodsByEmployee, dateMonthPredicate));
	}

	public void update(final WorkPeriod workPeriod, final LocalTime startTime, final LocalTime finishTime) {
		Predicate<WorkPeriod> workUnitPredicate = createWorkPeriodPredicate(workPeriod);
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(workPeriod.getEmployee());
		List<WorkPeriod> filteredWorkPeriod = Lists.newArrayList(Collections2.filter(workPeriodsByEmployee, workUnitPredicate));
		checkArgument(!isEmpty(filteredWorkPeriod), "Trying to change interval that does not exist.");
		WorkPeriod storedWorkPeriod = filteredWorkPeriod.get(0);
		delete(storedWorkPeriod);
		WorkPeriod newWorkPeriod = new WorkPeriod(
				storedWorkPeriod.getDate(), startTime, finishTime, storedWorkPeriod.getAllocation());
		store(newWorkPeriod);
	}

	public int getWorkedMinutesByDate(LocalDate date) {
		int total = 0;
		List<WorkPeriod> workPeriodsByDate = workUnitsByDate.get(date);
		for (WorkPeriod workPeriod : workPeriodsByDate) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByDateByEmployee(LocalDate date, Employee employee) {
		int total = 0;
		Predicate<WorkPeriod> employeePredicate = createWorkPeriodPredicate(employee);
		List<WorkPeriod> workPeriodsByDate = workUnitsByDate.get(date);
		List<WorkPeriod> workPeriodsByEmployee =  Lists.newArrayList(Collections2.filter(workPeriodsByDate, employeePredicate));
		for (WorkPeriod workPeriod : workPeriodsByEmployee) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByDateInterval(LocalDate startDate, LocalDate finishDate) {
		int total = 0;
		Predicate<LocalDate> dateIntervalPredicate = createDatePredicate(startDate, finishDate);
		Map<LocalDate, List<WorkPeriod>> _workUnits = Maps.filterKeys(workUnitsByDate, dateIntervalPredicate);
		List<WorkPeriod> workPeriods = Lists.newArrayList();
		for (LocalDate date : _workUnits.keySet()) {
			List<WorkPeriod> _workPeriods = _workUnits.get(date);
			workPeriods.addAll(_workPeriods);
		}
		for (WorkPeriod workPeriod : workPeriods) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByDateIntervalAndEmployee(LocalDate startDate, LocalDate finishDate, Employee employee) {
		int total = 0;
		Predicate<WorkPeriod> dateIntervalPredicate = createWorkPeriodPredicate(startDate, finishDate);
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(employee);
		List<WorkPeriod> workPeriodsByInterval = Lists.newArrayList(Collections2.filter(workPeriodsByEmployee, dateIntervalPredicate));
		for (WorkPeriod workPeriod : workPeriodsByInterval) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonth(int month) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(month);
		List<WorkPeriod> workPeriodsByMonth = Lists.newArrayList(Collections2.filter(workUnits, dateMonthPredicate));
		for (WorkPeriod workPeriod : workPeriodsByMonth) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthByEmployee(int month, Employee employee) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(month);
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(employee);
		List<WorkPeriod> workPeriodsByMonth = Lists.newArrayList(Collections2.filter(workPeriodsByEmployee, dateMonthPredicate));
		for (WorkPeriod workPeriod : workPeriodsByMonth) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthByClient(int month, Client client) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(month);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		List<WorkPeriod> workPeriodsByMonth = Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
		for (WorkPeriod workPeriod : workPeriodsByMonth) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthByClientByEmployee(int month, Client client, Employee employee) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(month);
		Predicate<WorkPeriod> employeePredicate = createWorkPeriodPredicate(employee);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		List<WorkPeriod> workPeriodsByMonth = Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
		List<WorkPeriod> workPeriodsByEmployee = Lists.newArrayList(Collections2.filter(workPeriodsByMonth, employeePredicate));
		for (WorkPeriod workPeriod : workPeriodsByEmployee) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthInterval(int startMonth, int finishMonth) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByMonthInterval = Lists.newArrayList(Collections2.filter(workUnits, dateMonthPredicate));
		for (WorkPeriod workPeriod : workPeriodsByMonthInterval) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthIntervalByEmployee(int startMonth, int finishMonth, Employee employee) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(employee);
		List<WorkPeriod> workPeriodsByMonthInterval = Lists.newArrayList(Collections2.filter(workPeriodsByEmployee, dateMonthPredicate));
		for (WorkPeriod workPeriod : workPeriodsByMonthInterval) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthIntervalByClient(int startMonth, int finishMonth, Client client) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		List<WorkPeriod> workPeriodsByMonthInterval = Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
		for (WorkPeriod workPeriod : workPeriodsByMonthInterval) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthIntervalByClientByEmployee(int startMonth, int finishMonth, Client client, Employee employee) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		Predicate<WorkPeriod> employeePredicate = createWorkPeriodPredicate(employee);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		List<WorkPeriod> workPeriodsByMonth = Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
		List<WorkPeriod> workPeriodsByEmployee = Lists.newArrayList(Collections2.filter(workPeriodsByMonth, employeePredicate));
		for (WorkPeriod workPeriod : workPeriodsByEmployee) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthIntervalByProject(int startMonth, int finishMonth, Project project) {
		int total = 0;
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByProject = workUnitsByProject.get(project);
		List<WorkPeriod> workPeriodsByMonth = Lists.newArrayList(Collections2.filter(workPeriodsByProject, dateMonthPredicate));
		for (WorkPeriod workPeriod : workPeriodsByMonth) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByProject(Project project) {
		int total = 0;
		List<WorkPeriod> workPeriodsByProject = workUnitsByProject.get(project);
		for (WorkPeriod workPeriod : workPeriodsByProject) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByMonthIntervalByProjectByEmployee(int startMonth, int finishMonth, Project project, Employee employee) {
		int total = 0;
		List<WorkPeriod> workPeriodsByProject = workUnitsByProject.get(project);
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByMonth = Lists.newArrayList(Collections2.filter(workPeriodsByProject, dateMonthPredicate));
		Predicate<WorkPeriod> employeePredicate = createWorkPeriodPredicate(employee);
		List<WorkPeriod> workPeriodsByEmployee = Lists.newArrayList(Collections2.filter(workPeriodsByMonth, employeePredicate));
		for (WorkPeriod workPeriod : workPeriodsByEmployee) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	public int getWorkedMinutesByProjectByEmployee(Project project, Employee employee) {
		int total = 0;		
		List<WorkPeriod> workPeriodsByProject = workUnitsByProject.get(project);
		Predicate<WorkPeriod> employeePredicate = createWorkPeriodPredicate(employee);
		List<WorkPeriod> workPeriodsByEmployee = Lists.newArrayList(Collections2.filter(workPeriodsByProject, employeePredicate));
		for (WorkPeriod workPeriod : workPeriodsByEmployee) {
			total += Minutes.minutesBetween(workPeriod.getStartTime(), workPeriod.getStopTime()).getMinutes();
		}
		return total;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final Employee employee) {
		Predicate<WorkPeriod> employeePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.getEmployee().equals(employee);
			}
		};
		return employeePredicate;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final LocalDate startDate, final LocalDate finishDate) {
		Predicate<WorkPeriod> employeePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return (workPeriod.getDate().compareTo(startDate) >= 0) && 
					(workPeriod.getDate().compareTo(finishDate) <= 0);
			}
		};
		return employeePredicate;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final LocalDate date) {
		Predicate<WorkPeriod> datePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.getDate().equals(date);
			}
		};
		return datePredicate;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final int month) {
		Predicate<WorkPeriod> datePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.getDate().monthOfYear().get() == month;
			}
		};
		return datePredicate;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final int startMonth, final int finishMonth) {
		Predicate<WorkPeriod> datePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return (workPeriod.getDate().monthOfYear().get() >= startMonth) && 
					(workPeriod.getDate().monthOfYear().get() <= finishMonth);
			}
		};
		return datePredicate;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final WorkPeriod workPeriodParam) {
		Predicate<WorkPeriod> workPeriodPredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.equals(workPeriodParam);
			}
		};
		return workPeriodPredicate;
	}

	private Predicate<LocalDate> createDatePredicate(final LocalDate startDate, final LocalDate finishDate) {
		Predicate<LocalDate> dateIntervalPredicate = new Predicate<LocalDate>() {
			public boolean apply(LocalDate date) {
				return (date.compareTo(startDate) >= 0) && (date.compareTo(finishDate) <= 0);
			}
		};
		return dateIntervalPredicate;
	}
}
