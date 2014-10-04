package br.org.matrix.timesheet.time;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

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

	private Map<LocalDate, List<WorkPeriod>> workUnitsByDate;
	private Map<Employee, List<WorkPeriod>> workUnitsByEmployee;
	private Map<Client, List<WorkPeriod>> workUnitsByClient;
	
	private AllocationRepository allocationRepository;
	
	public Work() {
		workUnitsByDate = Maps.newHashMap();
		workUnitsByEmployee = Maps.newHashMap();
		workUnitsByClient = Maps.newHashMap();
		
		//TODO Find a better name for this in memory DB
		//TODO Inject from the tests
		allocationRepository = new AllocationDB();
	}

	public void store(WorkPeriod workPeriod) {
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
			// TODO Extract this to a rule class
			checkForOverlaps(workPeriodsByEmployee, workPeriod);
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

	private void checkForOverlaps(List<WorkPeriod> workPeriodsByEmployee, WorkPeriod workPeriod) {
		int startMillisOfDay = workPeriod.getStartTime().getMillisOfDay();
		int stopMillisOfDay = workPeriod.getStopTime().getMillisOfDay();
		for (WorkPeriod workPeriodStored : workPeriodsByEmployee) {
			if (workPeriodStored.getDate().equals(workPeriod.getDate()) &&
					workPeriodStored.getClient().equals(workPeriod.getClient())) {
				int startStoredMillisOfDay = workPeriodStored.getStartTime().getMillisOfDay();
				int stopStoredMillisOfDay = workPeriodStored.getStopTime().getMillisOfDay();
				if (((startMillisOfDay <= startStoredMillisOfDay) && (stopMillisOfDay >= startStoredMillisOfDay)) 
						|| ((startMillisOfDay <= stopStoredMillisOfDay) && (stopMillisOfDay >= stopStoredMillisOfDay))) {
					throw new DateIntervalOvelapsException(workPeriod.getEmployee().getId(), 
							workPeriod.getEmployee().getName(), workPeriod.getDate(), 
							workPeriod.getStartTime(), workPeriod.getStopTime());
				}
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

	private Predicate<LocalDate> createDatePredicate(final LocalDate startDate, final LocalDate finishDate) {
		Predicate<LocalDate> dateIntervalPredicate = new Predicate<LocalDate>() {
			public boolean apply(LocalDate date) {
				return (date.compareTo(startDate) >= 0) && (date.compareTo(finishDate) <= 0);
			}
		};
		return dateIntervalPredicate;
	}
}
