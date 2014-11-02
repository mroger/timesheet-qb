package br.org.matrix.timesheet.time;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.google.common.base.Objects;

public class Period {

	private LocalDate date;
	private LocalTime startTime;
	private LocalTime stopTime;
	private int startMillisOfDay;
	private int stopMillisOfDay;

	public Period(LocalDate date, LocalTime startTime, LocalTime stopTime) {
		checkState(date != null, "Date cannot be null.");
		checkState(startTime != null, "Start time cannot be null.");
		checkState(stopTime != null, "Stop time cannot be null.");
		checkArgument(startTime.compareTo(stopTime) < 0, "StartTime must be less than StopTime.");

		this.date = date;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.startMillisOfDay = startTime.getMillisOfDay();
		this.stopMillisOfDay = stopTime.getMillisOfDay();
	}

	public boolean overlap(Period period) {
		if (period.getDate().equals(this.getDate())) {
			if (this.contains(period) || period.contains(this)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean contains(Period period) {
		if (((getStartMillisOfDay() <= period.getStartMillisOfDay()) 
				&& (getStopMillisOfDay() >= period.getStartMillisOfDay())) 
				|| ((getStartMillisOfDay() <= period.getStopMillisOfDay()) 
				&& (getStopMillisOfDay() >= period.getStopMillisOfDay()))) {
			return true;
		}
		return false;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public int getStartMillisOfDay() {
		return startMillisOfDay;
	}

	public LocalTime getStopTime() {
		return stopTime;
	}

	public int getStopMillisOfDay() {
		return stopMillisOfDay;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(date, startTime, stopTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Period other = (Period) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
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
