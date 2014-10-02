package br.org.matrix.timesheet.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import br.org.matrix.timesheet.project.DateIntervalOvelapsException;
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
	public void shouldFindStoredWorkPeriodEvenIfDifferentObjects() {
		LocalDate date = today();
		WorkPeriod workPeriodStored = new WorkPeriod(date, time(11, 0), time(15, 0), new Employee(1, "emp01"));
		WorkPeriod workPeriodEqualToStored = new WorkPeriod(date, time(11, 0), time(15, 0), new Employee(1, "emp01"));
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriodStored);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, contains(workPeriodEqualToStored));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodByDate() {
		LocalDate today = today();
		LocalDate yesterday = yesterday();
		WorkPeriod workedToday1 = new WorkPeriod(today, time(8, 0), time(12, 0), new Employee(1, "emp01"));
		WorkPeriod workedToday2 = new WorkPeriod(today, time(13, 0), time(17, 0), new Employee(1, "emp01"));
		WorkPeriod workedYesterday1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), new Employee(1, "emp01"));
		WorkPeriod workedYesterday2 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), new Employee(1, "emp01"));
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedToday1);
		workRepository.store(workedToday2);
		workRepository.store(workedYesterday1);
		workRepository.store(workedYesterday2);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(yesterday);
		
		assertThat(workPeriods.size(), equalTo(2));
		assertTrue(workPeriods.contains(workedYesterday1));
		assertTrue(workPeriods.contains(workedYesterday2));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodByEmployee() {
		LocalDate today = today();
		Employee employee1 = new Employee(1, "emp01");
		WorkPeriod workedToday = new WorkPeriod(today, time(8, 0), time(12, 0), employee1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedToday);
		
		List<WorkPeriod> workPeriods = workRepository.findByEmployee(employee1);
		
		assertTrue(workPeriods.contains(workedToday));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodByDateAndEmployee() {
		LocalDate today = today();
		LocalDate yesterday = yesterday();
		Employee employee1 = new Employee(1, "emp01");
		Employee employee2 = new Employee(2, "emp02");
		
		WorkPeriod workedToday1Eployee1 = new WorkPeriod(today, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedToday2Eployee1 = new WorkPeriod(today, time(13, 0), time(17, 0), employee1);
		WorkPeriod workedYesterday1Eployee1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedYesterday2Eployee1 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), employee1);
		
		WorkPeriod workedToday1Eployee2 = new WorkPeriod(today, time(8, 0), time(12, 0), employee2);
		WorkPeriod workedToday2Eployee2 = new WorkPeriod(today, time(13, 0), time(17, 0), employee2);
		WorkPeriod workedYesterday1Eployee2 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), employee2);
		WorkPeriod workedYesterday2Eployee2 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), employee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedToday1Eployee1);
		workRepository.store(workedToday2Eployee1);
		workRepository.store(workedYesterday1Eployee1);
		workRepository.store(workedYesterday2Eployee1);
		
		workRepository.store(workedToday1Eployee2);
		workRepository.store(workedToday2Eployee2);
		workRepository.store(workedYesterday1Eployee2);
		workRepository.store(workedYesterday2Eployee2);
		
		List<WorkPeriod> workPeriods = workRepository.findByDateAndEmployee(yesterday, employee1);
		assertThat(workPeriods.size(), equalTo(2));
		assertThat(workPeriods.get(0).getDate(), equalTo(yesterday));
		assertThat(workPeriods.get(1).getDate(), equalTo(yesterday));
		assertThat(workPeriods.get(0).getEmployee(), equalTo(employee1));
		assertThat(workPeriods.get(1).getEmployee(), equalTo(employee1));
		
	}
	
	@Test
	public void shouldFindStoredWorkPeriodForDateInterval() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 1, 4);
		LocalDate date5 = createDate(2000, 1, 5);
		LocalDate date6 = createDate(2000, 1, 6);
		Employee employee1 = new Employee(1, "emp01");
		
		WorkPeriod workedDate1Eployee1 = new WorkPeriod(date1, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate2Eployee1 = new WorkPeriod(date2, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate3Eployee1 = new WorkPeriod(date3, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate4Eployee1 = new WorkPeriod(date4, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate5Eployee1 = new WorkPeriod(date5, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate6Eployee1 = new WorkPeriod(date6, time(8, 0), time(12, 0), employee1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedDate1Eployee1);
		workRepository.store(workedDate2Eployee1);
		workRepository.store(workedDate3Eployee1);
		workRepository.store(workedDate4Eployee1);
		workRepository.store(workedDate5Eployee1);
		workRepository.store(workedDate6Eployee1);
		
		List<WorkPeriod> workPeriods = workRepository.findByDateInterval(date2, date5);
		assertThat(workPeriods.size(), equalTo(4));
		assertTrue(workPeriods.contains(workedDate2Eployee1));
		assertTrue(workPeriods.contains(workedDate3Eployee1));
		assertTrue(workPeriods.contains(workedDate4Eployee1));
		assertTrue(workPeriods.contains(workedDate5Eployee1));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodForDateIntervalAndEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 1, 4);
		LocalDate date5 = createDate(2000, 1, 5);
		LocalDate date6 = createDate(2000, 1, 6);
		Employee employee1 = new Employee(1, "emp01");
		Employee employee2 = new Employee(2, "emp02");
		
		WorkPeriod workedDate1Eployee1 = new WorkPeriod(date1, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate2Eployee1 = new WorkPeriod(date2, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate3Eployee1 = new WorkPeriod(date3, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate4Eployee1 = new WorkPeriod(date4, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate5Eployee1 = new WorkPeriod(date5, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate6Eployee1 = new WorkPeriod(date6, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedDate1Eployee2 = new WorkPeriod(date1, time(8, 0), time(12, 0), employee2);
		WorkPeriod workedDate2Eployee2 = new WorkPeriod(date2, time(8, 0), time(12, 0), employee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedDate1Eployee1);
		workRepository.store(workedDate2Eployee1);
		workRepository.store(workedDate3Eployee1);
		workRepository.store(workedDate4Eployee1);
		workRepository.store(workedDate5Eployee1);
		workRepository.store(workedDate6Eployee1);
		workRepository.store(workedDate1Eployee2);
		workRepository.store(workedDate2Eployee2);
		
		List<WorkPeriod> workPeriods = workRepository.findByDateIntervalAndEmployee(date2, date4, employee1);
		assertThat(workPeriods.size(), equalTo(3));
		assertTrue(workPeriods.contains(workedDate2Eployee1));
		assertTrue(workPeriods.contains(workedDate3Eployee1));
		assertTrue(workPeriods.contains(workedDate4Eployee1));
	}
	
	private LocalDate createDate(int year, int month, int day) {
		return new LocalDate(year, month, day);
	}
	
	@Test(expected=DateIntervalOvelapsException.class)
	public void shouldNotAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForTheSameEmployee() {
		LocalDate today = today();
		Employee employee1 = new Employee(1, "emp01");
		
		WorkPeriod workedInterval1Eployee1 = new WorkPeriod(today, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedInterval2Eployee1 = new WorkPeriod(today, time(10, 0), time(15, 0), employee1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Eployee1);
		workRepository.store(workedInterval2Eployee1);
	}
	
	@Test
	public void shouldAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForTheSameEmployeeAndDifferentDates() {
		LocalDate yesterday = yesterday();
		LocalDate today = today();
		Employee employee1 = new Employee(1, "emp01");
		
		WorkPeriod workedInterval1Eployee1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedInterval2Eployee1 = new WorkPeriod(today, time(10, 0), time(15, 0), employee1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Eployee1);
		workRepository.store(workedInterval2Eployee1);
	}
	
	@Test
	public void shouldAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForDifferentEmployees() {
		LocalDate today = today();
		Employee employee1 = new Employee(1, "emp01");
		Employee employee2 = new Employee(2, "emp02");
		
		WorkPeriod workedInterval1Eployee1 = new WorkPeriod(today, time(8, 0), time(12, 0), employee1);
		WorkPeriod workedInterval2Eployee2 = new WorkPeriod(today, time(10, 0), time(15, 0), employee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Eployee1);
		workRepository.store(workedInterval2Eployee2);
	}

	@Test
	public void shouldFindStoredWorkPeriodByClient() {
		
	}
	
	@Test
	public void shouldFindStoredWorkPeriodByDateAndClient() {
		
	}
	
	@Test
	public void shouldFindStoredWorkPeriodForDateIntervalAndClient() {
		
	}
	
	@Test
	public void shouldFindWorkPeriodOfAClientInAGivenMonth() {
		
	}
	
	@Test
	public void shouldFindWorkPeriodOfAClientBetweenTwoMonths() {
		
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
	
	private LocalDate yesterday() {
		return new LocalDate().minusDays(1);
	}
}
