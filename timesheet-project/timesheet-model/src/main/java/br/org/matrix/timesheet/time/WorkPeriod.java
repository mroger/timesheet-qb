package br.org.matrix.timesheet.time;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import br.org.matrix.timesheet.project.Allocation;
import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.Employee;
import br.org.matrix.timesheet.project.Project;

import com.google.common.base.Objects;

public class WorkPeriod {

	private LocalDate date;
	private LocalTime startTime;
	private LocalTime stopTime;
	private Allocation allocation;
	
	public WorkPeriod(LocalDate date, LocalTime startTime, LocalTime stopTime, Allocation allocation) {
		super();
		checkState(date!=null, "Date cannot be null.");
		checkState(startTime!=null, "Start time cannot be null.");
		checkState(stopTime!=null, "Stop time cannot be null.");
		checkState(allocation!=null, "Allocation cannot be null.");
		checkState(allocation.getEmployee()!=null, "Employee cannot be null.");
		checkState(allocation.getProject()!=null, "Project cannot be null.");
		checkArgument(startTime.compareTo(stopTime) < 0, "StartTime must be less than StopTime.");
		
		this.date = date;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.allocation = allocation;
	}

	/**
	 * Returns an immutable date.
	 * 
	 * @return LocalDate
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * Returns an immutable allocation.
	 * 
	 * @return Allocation
	 */
	public Allocation getAllocation() {
		return allocation;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getStopTime() {
		return stopTime;
	}

	/**
	 * Delegates to Allocation to return employee.
	 * 
	 * @return Employee
	 */
	public Employee getEmployee() {
		return allocation.getEmployee();
	}

	public Client getClient() {
		return allocation.getProject().getClient();
	}

	public Project getProject() {
		return allocation.getProject();
	}

	/**
	 * Checks if this period overlaps any of the periods in the collection passed. 
	 * 
	 * @param workPeriodsByEmployee
	 * @return true if an overlap is found, false otherwise
	 */
	public boolean overlaps(List<WorkPeriod> workPeriodsByEmployee) {
		//TODO Improve this search for overlaps
		int startMillisOfDay = this.getStartTime().getMillisOfDay();
		int stopMillisOfDay = this.getStopTime().getMillisOfDay();
		for (WorkPeriod workPeriodStored : workPeriodsByEmployee) {
			if (workPeriodStored.getDate().equals(this.getDate()) &&
					workPeriodStored.getEmployee().equals(this.getEmployee())) {
				int startStoredMillisOfDay = workPeriodStored.getStartTime().getMillisOfDay();
				int stopStoredMillisOfDay = workPeriodStored.getStopTime().getMillisOfDay();
				if (((startMillisOfDay <= startStoredMillisOfDay) && (stopMillisOfDay >= startStoredMillisOfDay)) 
						|| ((startMillisOfDay <= stopStoredMillisOfDay) && (stopMillisOfDay >= stopStoredMillisOfDay))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(date, startTime, stopTime, allocation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkPeriod other = (WorkPeriod) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (allocation == null) {
			if (other.allocation != null)
				return false;
		} else if (!allocation.equals(other.allocation))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (stopTime == null) {
			if (other.stopTime != null)
				return false;
		} else if (!stopTime.equals(other.stopTime))
			return false;
		return true;
	}
}
