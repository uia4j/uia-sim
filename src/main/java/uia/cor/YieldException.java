package uia.cor;

public class YieldException extends RuntimeException {

	private static final long serialVersionUID = -6230714829511628983L;

	public YieldException(String message) {
		super(message);
	}

	public YieldException(String message, Throwable th) {
		super(message, th);
	}
}
