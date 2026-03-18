package main.java.com.carrental.util;

import java.sql.SQLException;

public class Exceptions {
	
	public static class InvalidCredentialsException extends RuntimeException {
		public InvalidCredentialsException(String message) {
			super(message);
		}
	}
	public static class EmailAlreadyUsedException extends SQLException {
		public EmailAlreadyUsedException(String message) {
			super(message);
		}
	}
	
}
