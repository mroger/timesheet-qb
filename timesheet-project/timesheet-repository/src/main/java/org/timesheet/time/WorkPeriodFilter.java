package org.timesheet.time;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import br.org.matrix.timesheet.project.Employee;
import br.org.matrix.timesheet.time.WorkPeriod;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class WorkPeriodFilter<T> {
	
	private List<WorkPeriod> workPeriods;
	
	public WorkPeriodFilter(Map<T, List<WorkPeriod>> workUnits, T key) {
		workPeriods = workUnits.get(key);
	}

	public WorkPeriodFilter(List<WorkPeriod> workPeriods) {
		this.workPeriods = workPeriods;
	}
	
	public WorkPeriodFilter<T> andFilterBy(final Employee employee) {
		Predicate<WorkPeriod> predicate = createWorkPeriodPredicate(employee);
		workPeriods = Lists.newArrayList(Collections2.filter(workPeriods, predicate));
		return this;
	}
	
	public WorkPeriodFilter<T> andFilterBy(final int startMonth, final int finishMonth) {
		Predicate<WorkPeriod> predicate = createWorkPeriodPredicate(startMonth, finishMonth);
		workPeriods = Lists.newArrayList(Collections2.filter(workPeriods, predicate));
		return this;
	}
	
	public WorkPeriodFilter<T> andFilterBy(int month) {
		Predicate<WorkPeriod> predicate = createWorkPeriodPredicate(month);
		workPeriods = Lists.newArrayList(Collections2.filter(workPeriods, predicate));
		return this;
	}
	
	public WorkPeriodFilter<T>  andFilterBy(LocalDate startDate, LocalDate finishDate) {
		Predicate<WorkPeriod> predicate = createWorkPeriodPredicate(startDate, finishDate);
		workPeriods = Lists.newArrayList(Collections2.filter(workPeriods, predicate));
		return this;
	}

	public WorkPeriodFilter<T> andFilterBy(WorkPeriod workPeriod) {
		Predicate<WorkPeriod> predicate = createWorkPeriodPredicate(workPeriod);
		workPeriods = Lists.newArrayList(Collections2.filter(workPeriods, predicate));
		return this;
	}

	public WorkPeriodFilter<T> andFilterBy(LocalDate date) {
		Predicate<WorkPeriod> predicate = createWorkPeriodPredicate(date);
		workPeriods = Lists.newArrayList(Collections2.filter(workPeriods, predicate));
		return this;
	}
	
	public List<WorkPeriod> getResult() {
		return workPeriods;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final Employee employee) {
		Predicate<WorkPeriod> employeePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.getEmployee().equals(employee);
			}
		};
		return employeePredicate;
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

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final int month) {
		Predicate<WorkPeriod> datePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.getDate().monthOfYear().get() == month;
			}
		};
		return datePredicate;
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

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final WorkPeriod workPeriodParam) {
		Predicate<WorkPeriod> workPeriodPredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.equals(workPeriodParam);
			}
		};
		return workPeriodPredicate;
	}

	private Predicate<WorkPeriod> createWorkPeriodPredicate(final LocalDate date) {
		Predicate<WorkPeriod> datePredicate = new Predicate<WorkPeriod>() {
			public boolean apply(WorkPeriod workPeriod) {
				return workPeriod.getDate().equals(date);
			}
		};
		return datePredicate;
	}
}
