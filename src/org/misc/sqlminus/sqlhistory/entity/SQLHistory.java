package org.misc.sqlminus.sqlhistory.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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
