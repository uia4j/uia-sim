package uia.sim;

public class InterruptException extends SimException {

	/**
	 * 
	 */ 
	private static final long serialVersionUID = -2067294123704844987L;

	public InterruptException(Process process, String message) {
		super(process, message);
	}

}
