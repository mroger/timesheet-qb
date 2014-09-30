package br.org.matrix.timesheet.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import br.org.matrix.timesheet.project.Employee;

public class WorkTest {

//	@Test
//	public void shouldReturnEmptyWorkUnit() {
//		Work workDay = new Work();
//		List<WorkPeriod> workUnits = workDay.findByDate(today());
//		assertThat(workUnits.size(), equalTo(0));
//	}
//
//	private LocalDate today() {
//		return new LocalDate();
//	}
	
	@Test
	public void shouldStoreWorkUnit() {
		LocalDate date = today();
		WorkPeriod workPeriod = new WorkPeriod(date, time(11, 0), time(15, 0), new Employee(1, "emp01"));
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriod);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, contains(workPeriod));
	}
	
	@Test
	public void shouldNotFindWorkPeriodDifferentFromStored() {
		LocalDate date = today();
		WorkPeriod workPeriodStored = new WorkPeriod(date, time(11, 0), time(15, 0), new Employee(1, "emp01"));
		WorkPeriod workPeriodNotStored = new WorkPeriod(date, time(16, 0), time(20, 0), new Employee(2, "emp02"));
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriodStored);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, not(contains(workPeriodNotStored)));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodEvenIfDifferentObject() {
		LocalDate date = today();
		WorkPeriod workPeriodStored = new WorkPeriod(date, time(11, 0), time(15, 0), new Employee(1, "emp01"));
		WorkPeriod workPeriodEqualToStored = new WorkPeriod(date, time(11, 0), time(15, 0), new Employee(1, "emp01"));
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriodStored);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, contains(workPeriodEqualToStored));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodForDateAndClient() {
		
	}
	
	@Test
	public void shouldFindStoredWorkPeriodForDateIntervalAndClient() {
		
	}
	
	@Test
	public void shouldNotAcceptWorkPeriodThatOverlapsWithExistingWorkPeriod() {
		
	}

	private LocalDate today() {
		return new LocalDate();
	}

	private LocalTime now() {
		return new LocalTime();
	}

	private LocalTime time(int hour, int minute) {
		return new LocalTime(hour, minute);
	}
}
