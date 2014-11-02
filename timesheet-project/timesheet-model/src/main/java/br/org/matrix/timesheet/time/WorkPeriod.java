package br.org.matrix.timesheet.time;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import br.org.matrix.timesheet.project.Allocation;
import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.Employee;
import br.org.matrix.timesheet.project.Project;

import com.google.common.base.Objects;

public class WorkPeriod extends Period {

	private Allocation allocation;

	public WorkPeriod(LocalDate date, LocalTime startTime, LocalTime stopTime,
			Allocation allocation) {
		super(date, startTime, stopTime);
		checkState(allocation != null, "Allocation cannot be null.");
		checkState(allocation.getEmployee() != null, "Employee cannot be null.");
		checkState(allocation.getProject() != null, "Project cannot be null.");

		this.allocation = allocation;
	}

	/**
	 * Returns an immutable allocation.
	 * 
	 * @return Allocation
	 */
	public Allocation getAllocation() {
		return allocation;
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
	 * Checks if this period overlaps any of the periods in the collection
	 * passed.
	 * 
	 * @param workPeriodsByEmployee
	 * @return true if an overlap is found, false otherwise
	 */
	public void overlaps(List<WorkPeriod> workPeriodsByEmployee) {
		if (workPeriodsByEmployee != null) {
			for (WorkPeriod workPeriodStored : workPeriodsByEmployee) {
				if (workPeriodStored.getEmployee().equals(this.getEmployee())
						&& super.overlap(workPeriodStored)) {
					throw new DateIntervalOvelapsException(getEmployee().getId(),
							getEmployee().getName(), getDate(), getStartTime(),
							getStopTime());
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), allocation);
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
		if (allocation == null) {
			if (other.allocation != null)
				return false;
		} else if (!allocation.equals(other.allocation))
			return false;
		return super.equals(obj);
	}

	
	
}
