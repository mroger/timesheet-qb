package br.org.matrix.timesheet.time;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import br.org.matrix.timesheet.project.Employee;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Work implements WorkRepository {

	private Map<LocalDate, List<WorkPeriod>> workUnitsByDate;
	private Map<Employee, List<WorkPeriod>> workUnitsByEmployee;
	
	public Work() {
		workUnitsByDate = Maps.newHashMap();
		workUnitsByEmployee = Maps.newHashMap();
	}

	public void store(WorkPeriod workPeriod) {
		List<WorkPeriod> workPeriodsByDate = workUnitsByDate.get(workPeriod.getDate());
		if (workPeriodsByDate == null) {
			workPeriodsByDate = Lists.newArrayList();
		}
		workPeriodsByDate.add(workPeriod);
		workUnitsByDate.put(workPeriod.getDate(), workPeriodsByDate);
		
		List<WorkPeriod> workPeriodsByEmployee = workUnitsByEmployee.get(workPeriod.getDate());
		if (workPeriodsByEmployee == null) {
			workPeriodsByEmployee = Lists.newArrayList();
		}
		workPeriodsByEmployee.add(workPeriod);
		workUnitsByEmployee.put(workPeriod.getEmployee(), workPeriodsByEmployee);
	}

	public List<WorkPeriod> findByDate(LocalDate date) {
		return workUnitsByDate.get(date);
	}

	public List<WorkPeriod> findByEmployee(Employee employee) {
		return workUnitsByEmployee.get(employee);
	}

	public List<WorkPeriod> findByDateAndEmployee(LocalDate date, Employee employee) {
		Predicate<WorkPeriod> employeePredicate = createEmployeePredicate(employee);
		List<WorkPeriod> workPeriodsByDate = workUnitsByDate.get(date);
		List<WorkPeriod> workPeriodsByDateAndEmployee = Lists.newArrayList(Collections2.filter(workPeriodsByDate, employeePredicate));
		return workPeriodsByDateAndEmployee;
	}

	private Predicate<WorkPeriod> createEmployeePredicate(final Employee employee) {
		Predicate<WorkPeriod> employeePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.getEmployee().equals(employee);
			}
		};
		return employeePredicate;
	}
}
