package br.org.matrix.timesheet.time;

import static br.org.matrix.timesheet.util.TimesheetUtil.createAllocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import br.org.matrix.timesheet.project.Allocation;
import br.org.matrix.timesheet.project.Client;
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
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		WorkPeriod workPeriod = new WorkPeriod(date, time(11, 0), time(15, 0), allocation);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriod);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, contains(workPeriod));
	}
	
	@Test
	public void shouldNotFindWorkPeriodDifferentFromStored() {
		LocalDate date = today();
		Allocation allocationEmployee1 = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = createAllocation(2, "emp02", 1, "project01", 1, "client01");
		WorkPeriod workPeriodStored = new WorkPeriod(date, time(11, 0), time(15, 0), allocationEmployee1);
		WorkPeriod workPeriodNotStored = new WorkPeriod(date, time(16, 0), time(20, 0), allocationEmployee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriodStored);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, not(contains(workPeriodNotStored)));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodEvenIfDifferentObjects() {
		LocalDate date = today();
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		WorkPeriod workPeriodStored = new WorkPeriod(date, time(11, 0), time(15, 0), allocation);
		WorkPeriod workPeriodEqualToStored = new WorkPeriod(date, time(11, 0), time(15, 0), allocation);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriodStored);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, contains(workPeriodEqualToStored));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodByDate() {
		LocalDate today = today();
		LocalDate yesterday = yesterday();
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		WorkPeriod workedToday1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocation);
		WorkPeriod workedToday2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocation);
		WorkPeriod workedYesterday1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), allocation);
		WorkPeriod workedYesterday2 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), allocation);
		
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
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		WorkPeriod workedToday = new WorkPeriod(today, time(8, 0), time(12, 0), allocation);
		
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
		
		Allocation allocationEmployee1 = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = createAllocation(2, "emp02", 1, "project01", 1, "client01");
		
		WorkPeriod workedToday1Employee1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedToday2Employee1 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationEmployee1);
		WorkPeriod workedYesterday1Employee1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedYesterday2Employee1 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), allocationEmployee1);
		
		WorkPeriod workedToday1Employee2 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationEmployee2);
		WorkPeriod workedToday2Employee2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationEmployee2);
		WorkPeriod workedYesterday1Employee2 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), allocationEmployee2);
		WorkPeriod workedYesterday2Employee2 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), allocationEmployee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedToday1Employee1);
		workRepository.store(workedToday2Employee1);
		workRepository.store(workedYesterday1Employee1);
		workRepository.store(workedYesterday2Employee1);
		
		workRepository.store(workedToday1Employee2);
		workRepository.store(workedToday2Employee2);
		workRepository.store(workedYesterday1Employee2);
		workRepository.store(workedYesterday2Employee2);
		
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
		
		Allocation allocation = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedDate1Employee1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocation);
		WorkPeriod workedDate2Employee1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocation);
		WorkPeriod workedDate3Employee1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocation);
		WorkPeriod workedDate4Employee1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocation);
		WorkPeriod workedDate5Employee1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocation);
		WorkPeriod workedDate6Employee1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocation);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedDate1Employee1);
		workRepository.store(workedDate2Employee1);
		workRepository.store(workedDate3Employee1);
		workRepository.store(workedDate4Employee1);
		workRepository.store(workedDate5Employee1);
		workRepository.store(workedDate6Employee1);
		
		List<WorkPeriod> workPeriods = workRepository.findByDateInterval(date2, date5);
		assertThat(workPeriods.size(), equalTo(4));
		assertTrue(workPeriods.contains(workedDate2Employee1));
		assertTrue(workPeriods.contains(workedDate3Employee1));
		assertTrue(workPeriods.contains(workedDate4Employee1));
		assertTrue(workPeriods.contains(workedDate5Employee1));
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
		
		Allocation allocationEmployee1 = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = createAllocation(2, "emp02", 1, "project01", 1, "client01");
		
		WorkPeriod workedDate1Employee1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedDate2Employee1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedDate3Employee1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedDate4Employee1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedDate5Employee1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedDate6Employee1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedDate1Employee2 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationEmployee2);
		WorkPeriod workedDate2Employee2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationEmployee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedDate1Employee1);
		workRepository.store(workedDate2Employee1);
		workRepository.store(workedDate3Employee1);
		workRepository.store(workedDate4Employee1);
		workRepository.store(workedDate5Employee1);
		workRepository.store(workedDate6Employee1);
		workRepository.store(workedDate1Employee2);
		workRepository.store(workedDate2Employee2);
		
		List<WorkPeriod> workPeriods = workRepository.findByDateIntervalAndEmployee(date2, date4, employee1);
		assertThat(workPeriods.size(), equalTo(3));
		assertTrue(workPeriods.contains(workedDate2Employee1));
		assertTrue(workPeriods.contains(workedDate3Employee1));
		assertTrue(workPeriods.contains(workedDate4Employee1));
	}
	
	private LocalDate createDate(int year, int month, int day) {
		return new LocalDate(year, month, day);
	}
	
	@Test(expected=DateIntervalOvelapsException.class)
	public void shouldNotAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForTheSameEmployee() {
		LocalDate today = today();
		
		Allocation allocationEmployee1 = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1Employee1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedInterval2Employee1 = new WorkPeriod(today, time(10, 0), time(15, 0), allocationEmployee1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Employee1);
		workRepository.store(workedInterval2Employee1);
	}
	
	@Test
	public void shouldAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForTheSameEmployeeAndDifferentDates() {
		LocalDate yesterday = yesterday();
		LocalDate today = today();
		
		Allocation allocationEmployee1 = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1Employee1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedInterval2Employee1 = new WorkPeriod(today, time(10, 0), time(15, 0), allocationEmployee1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Employee1);
		workRepository.store(workedInterval2Employee1);
	}
	
	@Test
	public void shouldAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForDifferentEmployees() {
		LocalDate today = today();
		
		Allocation allocationEmployee1 = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = createAllocation(2, "emp02", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1Employee1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedInterval2Employee2 = new WorkPeriod(today, time(10, 0), time(15, 0), allocationEmployee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Employee1);
		workRepository.store(workedInterval2Employee2);
	}

	@Test
	public void shouldFindStoredWorkPeriodByClient() {
		LocalDate today = today();
		
		Allocation allocationClient1 = createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Client1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Client1 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Client2 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Client2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Client1);
		workRepository.store(workedInterval2Client1);
		workRepository.store(workedInterval1Client2);
		workRepository.store(workedInterval2Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByClient(new Client(2, "client02"));
		assertThat(workPeriods.size(), equalTo(2));
		assertTrue(workPeriods.contains(workedInterval1Client2));
		assertTrue(workPeriods.contains(workedInterval2Client2));
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
