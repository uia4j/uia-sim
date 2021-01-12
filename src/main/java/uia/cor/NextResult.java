package uia.cor;

/**
 * The next information.
 * 
 * @author Kan
 *
 * @param <T> The data type exchanges to the generator.
 */
public final class NextResult<T> {

	/**
	 * If there is a next iteration.
	 */
	public final boolean hasNext;
	
	/**
	 * The value.
	 */
	public final T value;
	
	protected NextResult(boolean hasNext, T value) {
		this.hasNext = hasNext;
		this.value = value;
	}
}
