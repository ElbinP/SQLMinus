package org.misc.sqlminus.sqlhistory.entity;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;



@XmlRootElement
public class SQLHistory {

	private List<String> sqlCommands;

	public List<String> getSqlCommands() {
		return sqlCommands;
	}

	@XmlElement
	public void setSqlCommands(List<String> sqlCommands) {
		this.sqlCommands = sqlCommands;
	}

}
