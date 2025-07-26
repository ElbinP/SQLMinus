package org.misc.sqlminus.session;

import com.google.gson.annotations.SerializedName;

public class SessionEntity {

	@SerializedName("connectionString")
	private final String connectionString;

	@SerializedName("userName")
	private final String userName;

	@SerializedName("password")
	private final String password;

	private SessionEntity(Builder builder) {
		this.connectionString = builder.connectionString;
		this.userName = builder.userName;
		this.password = builder.password;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String connectionString;
		private String userName;
		private String password;

		public Builder connectionString(String connectionString) {
			this.connectionString = connectionString;
			return this;
		}

		public Builder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public SessionEntity build() {
			return new SessionEntity(this);
		}
	}
}
