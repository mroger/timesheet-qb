package br.org.matrix.timesheet.time;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import br.org.matrix.timesheet.project.AllocationDB;
import br.org.matrix.timesheet.project.AllocationRepository;
import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.DateIntervalOvelapsException;
import br.org.matrix.timesheet.project.Employee;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Work implements WorkRepository {

	private List<WorkPeriod> workUnits;
	private Map<LocalDate, List<WorkPeriod>> workUnitsByDate;
	private Map<Employee, List<WorkPeriod>> workUnitsByEmployee;
	private Map<Client, List<WorkPeriod>> workUnitsByClient;
	
	private AllocationRepository allocationRepository;
	
	public Work() {
		workUnits = Lists.newArrayList();
		workUnitsByDate = Maps.newHashMap();
		workUnitsByEmployee = Maps.newHashMap();
		workUnitsByClient = Maps.newHashMap();
		
		//TODO Find a better name for this in memory DB
		//TODO Inject from the tests
		allocationRepository = new AllocationDB();
	}

	public void store(WorkPeriod workPeriod) {
		workUnits.add(workPeriod);
		
		List<WorkPeriod> workPeriodsByDate = workUnitsByDate.get(workPeriod.getDate());
		if (workPeriodsByDate == null) {
			workPeriodsByDate = Lists.newArrayList();
			workPeriodsByDate.add(workPeriod);
			workUnitsByDate.put(workPeriod.getDate(), workPeriodsByDate);
		} else {
			workPeriodsByDate.add(workPeriod);
		}
		
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(workPeriod.getEmployee());
		if (workPeriodsByEmployee == null) {
			workPeriodsByEmployee = Lists.newArrayList();
			workPeriodsByEmployee.add(workPeriod);
			workUnitsByEmployee.put(workPeriod.getEmployee(), workPeriodsByEmployee);
		} else {
			//TODO Move to beginning of the method
			if (workPeriod.overlaps(workPeriodsByEmployee)) {
				throw new DateIntervalOvelapsException(workPeriod.getEmployee().getId(), 
						workPeriod.getEmployee().getName(), workPeriod.getDate(), 
						workPeriod.getStartTime(), workPeriod.getStopTime());
			}
			workPeriodsByEmployee.add(workPeriod);
		}
		
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(workPeriod.getClient());
		if (workPeriodsByClient == null) {
			workPeriodsByClient = Lists.newArrayList();
			workPeriodsByClient.add(workPeriod);
			workUnitsByClient.put(workPeriod.getClient(), workPeriodsByClient);
		} else {
			workPeriodsByClient.add(workPeriod);
		}
	}
	
	//TODO Write tests
	public void delete(WorkPeriod workPeriod) {
		workUnits.remove(workPeriod);
		
		//TODO Extract duplicate code to a generic class
		for(LocalDate localDate : workUnitsByDate.keySet()) {
			List<WorkPeriod> workUnits = workUnitsByDate.get(localDate);
			if (workUnits.contains(workPeriod)) {
				workUnits.remove(workPeriod);
				break;
			}
		}
		
		for(Employee employee : workUnitsByEmployee.keySet()) {
			List<WorkPeriod> workUnits = workUnitsByEmployee.get(employee);
			if (workUnits.contains(workPeriod)) {
				workUnits.remove(workPeriod);
				break;
			}
		}
		
		for(Client client : workUnitsByClient.keySet()) {
			List<WorkPeriod> workUnits = workUnitsByClient.get(client);
			if (workUnits.contains(workPeriod)) {
				workUnits.remove(workPeriod);
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

	public List<WorkPeriod> findByDateAndEmployee(final LocalDate date, final Employee employee) {
		Predicate<WorkPeriod> employeePredicate = createWorkPeriodPredicate(employee);
		List<WorkPeriod> workPeriodsByDate = workUnitsByDate.get(date);
		return Lists.newArrayList(Collections2.filter(workPeriodsByDate, employeePredicate));
	}

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

	public List<WorkPeriod> findByMonthAndClient(int month, Client client) {
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(month);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		return Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
	}

	public List<WorkPeriod> findByMonthIntervalAndClient(int startMonth, int finishMonth, Client client) {
		Predicate<WorkPeriod> dateMonthPredicate = createWorkPeriodPredicate(startMonth, finishMonth);
		List<WorkPeriod> workPeriodsByClient = workUnitsByClient.get(client);
		return Lists.newArrayList(Collections2.filter(workPeriodsByClient, dateMonthPredicate));
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
