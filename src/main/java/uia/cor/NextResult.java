package uia.cor;

public final class NextResult<T> {

	public final boolean hasNext;
	
	public final T value;
	
	public NextResult(boolean hasNext, T value) {
		this.hasNext = hasNext;
		this.value = value;
	}
}
