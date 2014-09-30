package br.org.matrix.timesheet.time;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

import br.org.matrix.timesheet.project.Employee;

public class WorkPeriod {

	private LocalDate date;
	private LocalTime startTime;
	private LocalTime stopTime;
	private Employee employee;
	
	public WorkPeriod(LocalDate date, LocalTime startTime, LocalTime stopTime,
			Employee employee) {
		super();
		checkState(date!=null, "Date cannot be null.");
		checkState(startTime!=null, "Start time cannot be null.");
		checkState(stopTime!=null, "Stop time cannot be null.");
		checkState(employee!=null, "Client cannot be null.");
		checkArgument(startTime.compareTo(stopTime) < 0, "StartTime must be less than StopTime.");
		
		this.date = date;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.employee = employee;
	}

	public LocalDate getDate() {
		return date;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(date, startTime, stopTime, employee);
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
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
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
