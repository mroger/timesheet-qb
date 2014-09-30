package br.org.matrix.timesheet.time;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Work implements WorkRepository {

	private Map<LocalDate, List<WorkPeriod>> workUnits;
	
	public Work() {
		workUnits = Maps.newHashMap();
	}

	public void store(WorkPeriod workPeriod) {
		List<WorkPeriod> workPeriods = workUnits.get(workPeriod.getDate());
		if (workPeriods == null) {
			workPeriods = Lists.newArrayList();
		}
		workPeriods.add(workPeriod);
		workUnits.put(workPeriod.getDate(), workPeriods);
	}

	public List<WorkPeriod> findByDate(LocalDate date) {
		return workUnits.get(date);
	}
}
