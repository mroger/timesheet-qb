package br.org.matrix.timesheet.time;

import static org.junit.Assert.*;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

public class PeriodTest {

	@Test
	public void overlap() {
		Period period = new Period(today(), time(10, 00), time(11, 00));
		boolean overlaps = new Period(today(), time(10, 00), time(11, 00)).overlap(period);
		
		assertTrue(overlaps);
	}

	@Test
	public void overlap1() {
		Period period = new Period(today(), time(9, 00), time(10, 00));
		boolean overlaps = new Period(today(), time(10, 00), time(11, 00)).overlap(period);
		
		assertTrue(overlaps);
	}
	
	@Test
	public void overlap2() {
		Period period = new Period(today(), time(11, 00), time(12, 00));
		boolean overlaps = new Period(today(), time(10, 00), time(11, 00)).overlap(period);
		
		assertTrue(overlaps);
	}

	@Test
	public void overlap3() {
		Period period = new Period(today(), time(8, 00), time(12, 00));
		boolean overlaps = new Period(today(), time(6, 00), time(10, 00)).overlap(period);
		
		assertTrue(overlaps);
	}

	@Test
	public void overlap4() {
		Period period = new Period(today(), time(8, 00), time(12, 00));
		boolean overlaps = new Period(today(), time(11, 00), time(14, 00)).overlap(period);
		
		assertTrue(overlaps);
	}

	@Test
	public void overlap5() {
		Period period = new Period(today(), time(8, 00), time(12, 00));
		boolean overlaps = new Period(today(), time(8, 00), time(12, 00)).overlap(period);
		
		assertTrue(overlaps);
	}

	@Test
	public void overlap6() {
		Period period = new Period(today(), time(8, 00), time(12, 00));
		boolean overlaps = new Period(today(), time(10, 00), time(11, 00)).overlap(period);
		
		assertTrue(overlaps);
	}

	@Test
	public void overlap7() {
		Period period = new Period(today(), time(8, 00), time(12, 00));
		boolean overlaps = new Period(today(), time(6, 00), time(14, 00)).overlap(period);
		
		assertTrue(overlaps);
	}
	
	private LocalTime time(int hour, int minute) {
		return new LocalTime(hour, minute);
	}
	
	private LocalDate today() {
		return new LocalDate();
	}
	
}
