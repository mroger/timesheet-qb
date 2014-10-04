package br.org.matrix.timesheet.project;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;

public class Client {

	private Integer id;
	private String name;
	
	public Client(Integer id, String name) {
		super();
		
		checkState(id!=null, "Id cannot be null.");
		checkState(name!=null, "Name cannot be null.");
		
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
