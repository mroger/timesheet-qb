package br.org.matrix.timesheet.util;

import br.org.matrix.timesheet.project.Allocation;
import br.org.matrix.timesheet.project.Client;
import br.org.matrix.timesheet.project.Employee;
import br.org.matrix.timesheet.project.Project;

public class TimesheetUtil {

	/**
	 * Helper method to build {@link Allocation} object from its parts.
	 * 
	 * @param idEmployee
	 * @param nameEmployee
	 * @param idProject
	 * @param nameProject
	 * @param idClient
	 * @param nameClient
	 * @return Allocation object
	 */
	public static Allocation createAllocation(int idEmployee, String nameEmployee, int idProject, String nameProject, int idClient, String nameClient) {
		Employee employee = new Employee(idEmployee, nameEmployee);
		Client client = new Client(idClient, nameClient);
		Project project = new Project(idProject, nameProject, client);
		return new Allocation(employee, project);
	}
}
