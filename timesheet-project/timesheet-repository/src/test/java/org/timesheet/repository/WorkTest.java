package org.timesheet.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.timesheet.time.Work;
import org.timesheet.time.WorkRepository;

import br.org.matrix.timesheet.project.Allocation;
import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.DateIntervalOvelapsException;
import br.org.matrix.timesheet.project.Employee;
import br.org.matrix.timesheet.project.Project;
import br.org.matrix.timesheet.time.WorkPeriod;

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
		Allocation allocation = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		WorkPeriod workPeriod = new WorkPeriod(date, time(11, 0), time(15, 0), allocation);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workPeriod);
		
		List<WorkPeriod> workPeriods = workRepository.findByDate(date);
		assertThat(workPeriods, contains(workPeriod));
	}
	
	@Test
	public void shouldNotFindWorkPeriodDifferentFromStored() {
		LocalDate date = today();
		Allocation allocationEmployee1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = Allocation.createAllocation(2, "emp02", 1, "project01", 1, "client01");
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
		Allocation allocation = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
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
		Allocation allocation = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
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
		Allocation allocation = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		WorkPeriod workedToday = new WorkPeriod(today, time(8, 0), time(12, 0), allocation);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedToday);
		
		List<WorkPeriod> workPeriods = workRepository.findByEmployee(employee1);
		
		assertTrue(workPeriods.contains(workedToday));
	}

	@Test
	public void shouldFindStoredWorkPeriodByProject() {
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Client1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Client1 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Client2 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Client2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Client1);
		workRepository.store(workedInterval2Client1);
		workRepository.store(workedInterval1Client2);
		workRepository.store(workedInterval2Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByProject(new Project(2, "project02", new Client(2, "client02")));
		assertThat(workPeriods.size(), equalTo(2));
		assertTrue(workPeriods.contains(workedInterval1Client2));
		assertTrue(workPeriods.contains(workedInterval2Client2));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodByDateAndEmployee() {
		LocalDate today = today();
		LocalDate yesterday = yesterday();
		Employee employee1 = new Employee(1, "emp01");
		
		Allocation allocationEmployee1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = Allocation.createAllocation(2, "emp02", 1, "project01", 1, "client01");
		
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
		
		Allocation allocation = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
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
		
		Allocation allocationEmployee1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = Allocation.createAllocation(2, "emp02", 1, "project01", 1, "client01");
		
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
	
	@Test (expected=DateIntervalOvelapsException.class)
	public void shouldNotAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForTheSameEmployee() {
		LocalDate today = today();
		
		Allocation allocationEmployee1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
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
		
		Allocation allocationEmployee1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1Employee1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedInterval2Employee1 = new WorkPeriod(today, time(10, 0), time(15, 0), allocationEmployee1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Employee1);
		workRepository.store(workedInterval2Employee1);
	}
	
	@Test
	public void shouldAcceptWorkPeriodThatOverlapsWithExistingWorkPeriodForDifferentEmployees() {
		LocalDate today = today();
		
		Allocation allocationEmployee1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = Allocation.createAllocation(2, "emp02", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1Employee1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationEmployee1);
		WorkPeriod workedInterval2Employee2 = new WorkPeriod(today, time(10, 0), time(15, 0), allocationEmployee2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Employee1);
		workRepository.store(workedInterval2Employee2);
	}

	@Test
	public void shouldFindStoredWorkPeriodByClient() {
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
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
		LocalDate yesterday = yesterday();
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(yesterday, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(yesterday, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByDateAndClient(yesterday, new Client(2, "client02"));
		assertThat(workPeriods.size(), equalTo(2));
		assertTrue(workPeriods.contains(workedInterval1Date1Client2));
		assertTrue(workPeriods.contains(workedInterval2Date1Client2));
	}
	
	@Test
	public void shouldFindStoredWorkPeriodForDateIntervalAndClient() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 1, 4);
		LocalDate date5 = createDate(2000, 1, 5);
		LocalDate date6 = createDate(2000, 1, 6);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client2 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date5Client2 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client2);
		workRepository.store(workedInterval2Date5Client2);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByDateIntervalAndClient(date3, date6, new Client(2, "client02"));
		assertThat(workPeriods.size(), equalTo(8));
		assertTrue(workPeriods.contains(workedInterval1Date3Client2));
		assertTrue(workPeriods.contains(workedInterval2Date3Client2));
		assertTrue(workPeriods.contains(workedInterval1Date4Client2));
		assertTrue(workPeriods.contains(workedInterval2Date4Client2));
		assertTrue(workPeriods.contains(workedInterval1Date5Client2));
		assertTrue(workPeriods.contains(workedInterval2Date5Client2));
		assertTrue(workPeriods.contains(workedInterval1Date6Client2));
		assertTrue(workPeriods.contains(workedInterval2Date6Client2));
	}
	
	@Test
	public void shouldFindWorkPeriodOfAClientInAGivenMonth() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 2, 1);
		LocalDate date5 = createDate(2000, 2, 2);
		LocalDate date6 = createDate(2000, 2, 3);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client2 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date5Client2 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client2);
		workRepository.store(workedInterval2Date5Client2);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByMonthByClient(2, new Client(2, "client02"));
		assertThat(workPeriods.size(), equalTo(6));
		assertTrue(workPeriods.contains(workedInterval1Date4Client2));
		assertTrue(workPeriods.contains(workedInterval2Date4Client2));
		assertTrue(workPeriods.contains(workedInterval1Date5Client2));
		assertTrue(workPeriods.contains(workedInterval2Date5Client2));
		assertTrue(workPeriods.contains(workedInterval1Date6Client2));
		assertTrue(workPeriods.contains(workedInterval2Date6Client2));
		
	}
	
	@Test
	public void shouldFindWorkPeriodOfAnEmployeeInAGivenMonth() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 2, 1);
		LocalDate date5 = createDate(2000, 2, 2);
		LocalDate date6 = createDate(2000, 2, 3);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client2 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date5Client2 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client2);
		workRepository.store(workedInterval2Date5Client2);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByMonthByEmployee(2, new Employee(2, "emp02"));
		assertThat(workPeriods.size(), equalTo(6));
		assertTrue(workPeriods.contains(workedInterval1Date4Client2));
		assertTrue(workPeriods.contains(workedInterval2Date4Client2));
		assertTrue(workPeriods.contains(workedInterval1Date5Client2));
		assertTrue(workPeriods.contains(workedInterval2Date5Client2));
		assertTrue(workPeriods.contains(workedInterval1Date6Client2));
		assertTrue(workPeriods.contains(workedInterval2Date6Client2));
	}
	
	@Test
	public void shouldFindWorkPeriodBetweenTwoMonths() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		LocalDate date7 = createDate(2000, 4, 1);
		LocalDate date8 = createDate(2000, 4, 2);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date7Client1 = new WorkPeriod(date7, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date7Client1 = new WorkPeriod(date7, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date8Client1 = new WorkPeriod(date8, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date8Client1 = new WorkPeriod(date8, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client2 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date5Client2 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date7Client2 = new WorkPeriod(date7, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date7Client2 = new WorkPeriod(date7, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date8Client2 = new WorkPeriod(date8, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date8Client2 = new WorkPeriod(date8, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		workRepository.store(workedInterval1Date7Client1);
		workRepository.store(workedInterval2Date7Client1);
		workRepository.store(workedInterval1Date8Client1);
		workRepository.store(workedInterval2Date8Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client2);
		workRepository.store(workedInterval2Date5Client2);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		workRepository.store(workedInterval1Date7Client2);
		workRepository.store(workedInterval2Date7Client2);
		workRepository.store(workedInterval1Date8Client2);
		workRepository.store(workedInterval2Date8Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByMonthInterval(2, 4);
		assertThat(workPeriods.size(), equalTo(24));
		assertTrue(workPeriods.contains(workedInterval1Date3Client1));
		assertTrue(workPeriods.contains(workedInterval2Date3Client1));
		assertTrue(workPeriods.contains(workedInterval1Date4Client1));
		assertTrue(workPeriods.contains(workedInterval2Date4Client1));
		assertTrue(workPeriods.contains(workedInterval1Date5Client1));
		assertTrue(workPeriods.contains(workedInterval2Date5Client1));
		assertTrue(workPeriods.contains(workedInterval1Date6Client1));
		assertTrue(workPeriods.contains(workedInterval2Date6Client1));
		assertTrue(workPeriods.contains(workedInterval1Date7Client1));
		assertTrue(workPeriods.contains(workedInterval2Date7Client1));
		assertTrue(workPeriods.contains(workedInterval1Date8Client1));
		assertTrue(workPeriods.contains(workedInterval2Date8Client1));
		assertTrue(workPeriods.contains(workedInterval1Date3Client2));
		assertTrue(workPeriods.contains(workedInterval2Date3Client2));
		assertTrue(workPeriods.contains(workedInterval1Date4Client2));
		assertTrue(workPeriods.contains(workedInterval2Date4Client2));
		assertTrue(workPeriods.contains(workedInterval1Date5Client2));
		assertTrue(workPeriods.contains(workedInterval2Date5Client2));
		assertTrue(workPeriods.contains(workedInterval1Date6Client2));
		assertTrue(workPeriods.contains(workedInterval2Date6Client2));
		assertTrue(workPeriods.contains(workedInterval1Date7Client2));
		assertTrue(workPeriods.contains(workedInterval2Date7Client2));
		assertTrue(workPeriods.contains(workedInterval1Date8Client2));
		assertTrue(workPeriods.contains(workedInterval2Date8Client2));
	}
	
	@Test
	public void shouldFindWorkPeriodOfAClientBetweenTwoMonths() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		LocalDate date7 = createDate(2000, 4, 1);
		LocalDate date8 = createDate(2000, 4, 2);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date7Client1 = new WorkPeriod(date7, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date7Client1 = new WorkPeriod(date7, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date8Client1 = new WorkPeriod(date8, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date8Client1 = new WorkPeriod(date8, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client2 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date5Client2 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date7Client2 = new WorkPeriod(date7, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date7Client2 = new WorkPeriod(date7, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date8Client2 = new WorkPeriod(date8, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date8Client2 = new WorkPeriod(date8, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		workRepository.store(workedInterval1Date7Client1);
		workRepository.store(workedInterval2Date7Client1);
		workRepository.store(workedInterval1Date8Client1);
		workRepository.store(workedInterval2Date8Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client2);
		workRepository.store(workedInterval2Date5Client2);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		workRepository.store(workedInterval1Date7Client2);
		workRepository.store(workedInterval2Date7Client2);
		workRepository.store(workedInterval1Date8Client2);
		workRepository.store(workedInterval2Date8Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByMonthIntervalByClient(2, 4, new Client(2, "client02"));
		assertThat(workPeriods.size(), equalTo(12));
		assertTrue(workPeriods.contains(workedInterval1Date3Client2));
		assertTrue(workPeriods.contains(workedInterval2Date3Client2));
		assertTrue(workPeriods.contains(workedInterval1Date4Client2));
		assertTrue(workPeriods.contains(workedInterval2Date4Client2));
		assertTrue(workPeriods.contains(workedInterval1Date5Client2));
		assertTrue(workPeriods.contains(workedInterval2Date5Client2));
		assertTrue(workPeriods.contains(workedInterval1Date6Client2));
		assertTrue(workPeriods.contains(workedInterval2Date6Client2));
		assertTrue(workPeriods.contains(workedInterval1Date7Client2));
		assertTrue(workPeriods.contains(workedInterval2Date7Client2));
		assertTrue(workPeriods.contains(workedInterval1Date8Client2));
		assertTrue(workPeriods.contains(workedInterval2Date8Client2));
	}
	
	@Test
	public void shouldFindWorkPeriodOfAnEmployeeBetweenTwoMonths() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		LocalDate date7 = createDate(2000, 4, 1);
		LocalDate date8 = createDate(2000, 4, 2);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date7Client1 = new WorkPeriod(date7, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date7Client1 = new WorkPeriod(date7, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date8Client1 = new WorkPeriod(date8, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date8Client1 = new WorkPeriod(date8, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client2 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date5Client2 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date7Client2 = new WorkPeriod(date7, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date7Client2 = new WorkPeriod(date7, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date8Client2 = new WorkPeriod(date8, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date8Client2 = new WorkPeriod(date8, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		workRepository.store(workedInterval1Date7Client1);
		workRepository.store(workedInterval2Date7Client1);
		workRepository.store(workedInterval1Date8Client1);
		workRepository.store(workedInterval2Date8Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client2);
		workRepository.store(workedInterval2Date5Client2);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		workRepository.store(workedInterval1Date7Client2);
		workRepository.store(workedInterval2Date7Client2);
		workRepository.store(workedInterval1Date8Client2);
		workRepository.store(workedInterval2Date8Client2);
		
		List<WorkPeriod> workPeriods = workRepository.findByMonthIntervalByEmployee(2, 4, new Employee(2, "emp02"));
		assertThat(workPeriods.size(), equalTo(12));
		assertTrue(workPeriods.contains(workedInterval1Date3Client2));
		assertTrue(workPeriods.contains(workedInterval2Date3Client2));
		assertTrue(workPeriods.contains(workedInterval1Date4Client2));
		assertTrue(workPeriods.contains(workedInterval2Date4Client2));
		assertTrue(workPeriods.contains(workedInterval1Date5Client2));
		assertTrue(workPeriods.contains(workedInterval2Date5Client2));
		assertTrue(workPeriods.contains(workedInterval1Date6Client2));
		assertTrue(workPeriods.contains(workedInterval2Date6Client2));
		assertTrue(workPeriods.contains(workedInterval1Date7Client2));
		assertTrue(workPeriods.contains(workedInterval2Date7Client2));
		assertTrue(workPeriods.contains(workedInterval1Date8Client2));
		assertTrue(workPeriods.contains(workedInterval2Date8Client2));
	}
	
	@Test
	public void shouldUpdateEmployeeWorkPeriodInterval() {
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1);
		workRepository.store(workedInterval2);
		
		workRepository.update(workedInterval2, time(13, 33), time(18, 45));
		
		WorkPeriod expected = new WorkPeriod(today, time(13, 33), time(18, 45), allocationClient1);
		
		List<WorkPeriod> workPeriods = workRepository.findByEmployee(new Employee(1, "emp01"));
		assertThat(workPeriods.size(), equalTo(2));
		assertTrue(workPeriods.contains(workedInterval1));
		assertTrue(workPeriods.contains(expected));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldRaiseExceptionWhenUpdateWorkPeriodThatDoesNotExist() {
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval3 = new WorkPeriod(today, time(18, 0), time(21, 0), allocationClient1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1);
		workRepository.store(workedInterval2);
		
		workRepository.update(workedInterval3, time(13, 33), time(18, 45));
	}
	
	@Test
	public void shouldDeleteEmployeeWorkPeriod() {
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1 = new WorkPeriod(today, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2 = new WorkPeriod(today, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval3 = new WorkPeriod(today, time(18, 0), time(21, 0), allocationClient1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1);
		workRepository.store(workedInterval2);
		workRepository.store(workedInterval3);
		
		List<WorkPeriod> workPeriods = workRepository.findByEmployee(new Employee(1, "emp01"));
		assertThat(workPeriods.size(), equalTo(3));
		assertTrue(workPeriods.contains(workedInterval1));
		assertTrue(workPeriods.contains(workedInterval2));
		assertTrue(workPeriods.contains(workedInterval3));
		
		workRepository.delete(workedInterval3);
		
		List<WorkPeriod> workPeriodsNew = workRepository.findByEmployee(new Employee(1, "emp01"));
		assertThat(workPeriodsNew.size(), equalTo(2));
		assertTrue(workPeriodsNew.contains(workedInterval1));
		assertTrue(workPeriodsNew.contains(workedInterval2));
	}
	
	//TODO Create more variations of this
	@Test
	public void shouldReturnWorkedMinutesInADay() {
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1 = new WorkPeriod(today, time(8, 12), time(12, 17), allocationClient1); // 245 min
		WorkPeriod workedInterval2 = new WorkPeriod(today, time(13, 27), time(17, 12), allocationClient1); // 225 min
		WorkPeriod workedInterval3 = new WorkPeriod(today, time(18, 3), time(21, 19), allocationClient1); // 196 min
																										  // 666 min
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1);
		workRepository.store(workedInterval2);
		workRepository.store(workedInterval3);
		
		int workMinutesInADay = workRepository.getWorkedMinutesByDate(today);
		
		assertThat(workMinutesInADay, equalTo(666));
	}
	
	@Test
	public void shouldReturnWorkedHoursInADayByAnEmployee() {
		LocalDate yesterday = yesterday();
		LocalDate today = today();
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(yesterday, time(8, 11), time(12, 22), allocationClient1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(yesterday, time(13, 10), time(17, 20), allocationClient1); // 250 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(today, time(8, 8), time(12, 18), allocationClient1); // 250 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(today, time(13, 5), time(17, 15), allocationClient1); // 250 min
		WorkPeriod workedInterval1Date1Client2 = new WorkPeriod(yesterday, time(8, 3), time(12, 13), allocationClient2); // 250 min
		WorkPeriod workedInterval2Date1Client2 = new WorkPeriod(yesterday, time(13, 7), time(17, 27), allocationClient2); // 260 min
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(today, time(8, 9), time(12, 39), allocationClient2); // 270 min
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(today, time(13, 40), time(17, 50), allocationClient2); // 250 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date1Client2);
		workRepository.store(workedInterval2Date1Client2);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByDateByEmployee(today, new Employee(1, "emp01"));
		
		assertThat(workMinutesByEmployee, equalTo(500));
	}
	
	@Test
	public void shouldReturnWorkedHoursInADateInterval() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 1, 4);
		LocalDate date5 = createDate(2000, 1, 5);
		LocalDate date6 = createDate(2000, 1, 6);
		
		Allocation allocation = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedDate1Employee1 = new WorkPeriod(date1, time(8, 10), time(12, 20), allocation); // 250 min
		WorkPeriod workedDate2Employee1 = new WorkPeriod(date2, time(8, 20), time(12, 30), allocation); // 250 min
		WorkPeriod workedDate3Employee1 = new WorkPeriod(date3, time(8, 40), time(12, 50), allocation); // 250 min
		WorkPeriod workedDate4Employee1 = new WorkPeriod(date4, time(8, 30), time(12, 40), allocation); // 250 min
		WorkPeriod workedDate5Employee1 = new WorkPeriod(date5, time(8, 20), time(12, 30), allocation); // 250 min
		WorkPeriod workedDate6Employee1 = new WorkPeriod(date6, time(8, 10), time(12, 20), allocation); // 250 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedDate1Employee1);
		workRepository.store(workedDate2Employee1);
		workRepository.store(workedDate3Employee1);
		workRepository.store(workedDate4Employee1);
		workRepository.store(workedDate5Employee1);
		workRepository.store(workedDate6Employee1);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByDateInterval(date2, date4);
		
		assertThat(workMinutesByEmployee, equalTo(750));
	}
	
	@Test
	public void shouldReturnWorkedHoursInADateIntervalByAnEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 1, 4);
		LocalDate date5 = createDate(2000, 1, 5);
		LocalDate date6 = createDate(2000, 1, 6);
		
		Allocation allocationEmployee1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee2 = Allocation.createAllocation(2, "emp02", 1, "project01", 1, "client01");
		
		WorkPeriod workedDate1Employee1 = new WorkPeriod(date1, time(8, 0), time(12, 30), allocationEmployee1); // 270 min
		WorkPeriod workedDate2Employee1 = new WorkPeriod(date2, time(8, 10), time(12, 25), allocationEmployee1); // 255 min
		WorkPeriod workedDate3Employee1 = new WorkPeriod(date3, time(8, 20), time(12, 30), allocationEmployee1); // 250 min
		WorkPeriod workedDate4Employee1 = new WorkPeriod(date4, time(8, 30), time(12, 40), allocationEmployee1); // 250 min
		WorkPeriod workedDate5Employee1 = new WorkPeriod(date5, time(8, 40), time(12, 50), allocationEmployee1); // 250 min
		WorkPeriod workedDate6Employee1 = new WorkPeriod(date6, time(8, 0), time(12, 10), allocationEmployee1); // 250 min
		WorkPeriod workedDate1Employee2 = new WorkPeriod(date1, time(8, 10), time(12, 50), allocationEmployee2); // 280 min
		WorkPeriod workedDate2Employee2 = new WorkPeriod(date2, time(8, 20), time(13, 0), allocationEmployee2); // 280 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedDate1Employee1);
		workRepository.store(workedDate2Employee1);
		workRepository.store(workedDate3Employee1);
		workRepository.store(workedDate4Employee1);
		workRepository.store(workedDate5Employee1);
		workRepository.store(workedDate6Employee1);
		workRepository.store(workedDate1Employee2);
		workRepository.store(workedDate2Employee2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByDateIntervalAndEmployee(date1, date2, new Employee(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(560));
	}
	
	@Test
	public void shouldReturnWorkedHoursInAGivenMonth() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 2, 1);
		LocalDate date5 = createDate(2000, 2, 2);
		LocalDate date6 = createDate(2000, 2, 3);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationClient1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationClient1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationClient1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationClient1); // 249 min
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationClient1); // 245 min
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationClient1); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationClient1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationClient1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationClient1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationClient1); // 263 min
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationClient1); // 272 min
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationClient1); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonth(2);
		
		assertThat(workMinutesByEmployee, equalTo(1583));
	}
	
	@Test
	public void shouldReturnWorkedHoursInAMonthByAnEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 2, 1);
		LocalDate date5 = createDate(2000, 2, 2);
		LocalDate date6 = createDate(2000, 2, 3);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationClient1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationClient1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationClient1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationClient1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationClient2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationClient2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationClient1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationClient1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationClient1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationClient1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationClient2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationClient2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthByEmployee(2, new Employee(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(537));
	}
	
	@Test
	public void shouldReturnWorkedHoursInAMonthInAClient() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 2, 1);
		LocalDate date5 = createDate(2000, 2, 2);
		LocalDate date6 = createDate(2000, 2, 3);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationClient1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationClient1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationClient1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationClient1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationClient2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationClient2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationClient1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationClient1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationClient1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationClient1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationClient2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationClient2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthByClient(2, new Client(1, "client01"));
		
		assertThat(workMinutesByEmployee, equalTo(1046));
	}
	
	@Test
	public void shouldReturnWorkedHoursInAMonthByAnEmployeeInAClient() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 1, 3);
		LocalDate date4 = createDate(2000, 2, 1);
		LocalDate date5 = createDate(2000, 2, 2);
		LocalDate date6 = createDate(2000, 2, 3);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthByClientByEmployee(2, new Client(1, "client01"), new Employee(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(533));
	}
	
	@Test
	public void shouldReturnWorkedHoursBetweenTwoMonths() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client1 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date6Client1 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient1);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client1);
		workRepository.store(workedInterval2Date6Client1);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthInterval(2, 3);
		
		assertThat(workMinutesByEmployee, equalTo(1920));
	}
	
	@Test
	public void shouldReturnWorkedHoursBetweenTwoMonthsByAnEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client2 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date2Client2 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client2);
		workRepository.store(workedInterval2Date2Client2);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthIntervalByEmployee(2, 3, new Employee(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(960));
	}
	
	@Test
	public void shouldReturnWorkedHoursBetweenTwoMonthsInAClient() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationClient1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationClient2 = Allocation.createAllocation(2, "emp02", 2, "project02", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date3Client1 = new WorkPeriod(date3, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date3Client1 = new WorkPeriod(date3, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date4Client2 = new WorkPeriod(date4, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date4Client2 = new WorkPeriod(date4, time(13, 0), time(17, 0), allocationClient2);
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 0), time(12, 0), allocationClient1);
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 0), time(17, 0), allocationClient1);
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 0), time(12, 0), allocationClient2);
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 0), time(17, 0), allocationClient2);
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client1);
		workRepository.store(workedInterval2Date3Client1);
		workRepository.store(workedInterval1Date4Client2);
		workRepository.store(workedInterval2Date4Client2);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthIntervalByClient(1, 2, new Client(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(480));
	}
	
	@Test
	public void shouldReturnWorkedHoursBetweenTwoMonthsByAnEmployeeInAClient() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthIntervalByClientByEmployee(2, 3, new Client(1, "client01"), new Employee(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(533));
	}
	
	@Test
	public void shouldReturnWorkedHoursInAProject() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByProject(new Project(3, "project03", new Client(1, "client01")));
		
		assertThat(workMinutesByEmployee, equalTo(1025));
	}
	
	@Test
	public void shouldReturnWorkedHoursBetweenTwoMonthsInAProject() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthIntervalByProject(2, 3, new Project(3, "project03", new Client(1, "client01")));
		
		assertThat(workMinutesByEmployee, equalTo(533));
	}
	
	@Test
	public void shouldReturnWorkedHoursInAProjectByAnEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByProjectByEmployee(new Project(3, "project03", new Client(2, "client02")), new Employee(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(1025));
	}
	
	@Test
	public void shouldReturnZeroWorkedHoursInAProjectByAnEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByProjectByEmployee(new Project(3, "project03", new Client(2, "client02")), new Employee(1, "emp01"));
		
		assertThat(workMinutesByEmployee, equalTo(0));
	}
	
	@Test
	public void shouldReturnWorkedHoursBetweenTwoMonthsInAProjectByAnEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthIntervalByProjectByEmployee(2, 3, new Project(3, "project03", new Client(2, "client02")), new Employee(2, "emp02"));
		
		assertThat(workMinutesByEmployee, equalTo(533));
	}
	
	@Test
	public void shouldReturnZeroWorkedHoursBetweenTwoMonthsInAProjectByAnEmployee() {
		LocalDate date1 = createDate(2000, 1, 1);
		LocalDate date2 = createDate(2000, 1, 2);
		LocalDate date3 = createDate(2000, 2, 1);
		LocalDate date4 = createDate(2000, 2, 2);
		LocalDate date5 = createDate(2000, 3, 1);
		LocalDate date6 = createDate(2000, 3, 2);
		
		Allocation allocationEmployee1Client1 = Allocation.createAllocation(1, "emp01", 1, "project01", 1, "client01");
		Allocation allocationEmployee1Client2 = Allocation.createAllocation(1, "emp01", 2, "project02", 2, "client02");
		Allocation allocationEmployee2Client1 = Allocation.createAllocation(2, "emp02", 3, "project03", 1, "client01");
		Allocation allocationEmployee2Client2 = Allocation.createAllocation(2, "emp02", 4, "project04", 2, "client02");
		
		WorkPeriod workedInterval1Date1Client1 = new WorkPeriod(date1, time(8, 11), time(12, 22), allocationEmployee1Client1); // 251 min
		WorkPeriod workedInterval2Date1Client1 = new WorkPeriod(date1, time(13, 13), time(17, 45), allocationEmployee1Client1); // 272 min
		WorkPeriod workedInterval1Date2Client1 = new WorkPeriod(date2, time(8, 18), time(12, 21), allocationEmployee2Client1); // 243 min
		WorkPeriod workedInterval2Date2Client1 = new WorkPeriod(date2, time(13, 3), time(17, 12), allocationEmployee2Client1); // 249 min
		WorkPeriod workedInterval1Date3Client2 = new WorkPeriod(date3, time(8, 8), time(12, 13), allocationEmployee1Client2); // 245 min
		WorkPeriod workedInterval2Date3Client2 = new WorkPeriod(date3, time(13, 6), time(17, 34), allocationEmployee1Client2); // 268 min
		WorkPeriod workedInterval1Date4Client1 = new WorkPeriod(date4, time(8, 21), time(12, 46), allocationEmployee1Client1); // 265 min
		WorkPeriod workedInterval2Date4Client1 = new WorkPeriod(date4, time(13, 24), time(17, 32), allocationEmployee1Client1); // 248 min
		WorkPeriod workedInterval1Date5Client1 = new WorkPeriod(date5, time(8, 2), time(12, 32), allocationEmployee2Client1); // 270 min
		WorkPeriod workedInterval2Date5Client1 = new WorkPeriod(date5, time(13, 5), time(17, 28), allocationEmployee2Client1); // 263 min
		WorkPeriod workedInterval1Date6Client2 = new WorkPeriod(date6, time(8, 5), time(12, 37), allocationEmployee2Client2); // 272 min
		WorkPeriod workedInterval2Date6Client2 = new WorkPeriod(date6, time(13, 1), time(17, 26), allocationEmployee2Client2); // 265 min
		
		WorkRepository workRepository = new Work();
		workRepository.store(workedInterval1Date1Client1);
		workRepository.store(workedInterval2Date1Client1);
		workRepository.store(workedInterval1Date2Client1);
		workRepository.store(workedInterval2Date2Client1);
		workRepository.store(workedInterval1Date3Client2);
		workRepository.store(workedInterval2Date3Client2);
		workRepository.store(workedInterval1Date4Client1);
		workRepository.store(workedInterval2Date4Client1);
		workRepository.store(workedInterval1Date5Client1);
		workRepository.store(workedInterval2Date5Client1);
		workRepository.store(workedInterval1Date6Client2);
		workRepository.store(workedInterval2Date6Client2);
		
		int workMinutesByEmployee = workRepository.getWorkedMinutesByMonthIntervalByProjectByEmployee(2, 3, new Project(3, "project03", new Client(2, "client02")), new Employee(1, "emp01"));
		
		assertThat(workMinutesByEmployee, equalTo(0));
	}
	
	/**
	 * Looks for intervals with only start time
	 */
	@Test
	public void shouldFindIntervalsThatAreOpen() {
		
	}
	
	@Test
	public void shouldFindProjects() {
		
	}
	
	@Test
	public void shouldFindProjectsByEmployee() {
		
	}
	
	@Test
	public void shouldFindProjectsByClient() {
		
	}
	
	@Test
	public void shouldFindAllocations() {
		
	}
	
	@Test
	public void shouldFindAllocationsByEmployee() {
		
	}
	
	@Test
	public void shouldFindClients() {
		
	}
	
	@Test
	public void shouldFindClientsByEmployee() {
		
	}
	
	@Test
	public void shouldFindAllocationsByClient() {
		
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
