package br.org.matrix.timesheet.project;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;

public class Allocation {

	private Employee employee;
	private Project project;
	
	public Allocation(Employee employee, Project project) {
		super();
		
		checkState(employee!=null, "Employee cannot be null.");
		checkState(project!=null, "Project cannot be null.");
		
		this.employee = employee;
		this.project = project;
	}
	
	public Employee getEmployee() {
		return employee;
	}
	public Project getProject() {
		return project;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(employee, project);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Allocation other = (Allocation) obj;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}
	
	
}
