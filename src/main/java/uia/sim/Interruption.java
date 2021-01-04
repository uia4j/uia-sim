package uia.sim;

public class Interruption extends Event {

	private final Process process;
	
	public Interruption(Env env, Process process) {
		super(env, "Interruption");
		this.process = process;
		addCallable(e -> {
			this.process.resume();
		});
		setOk(false);
		
		env.schedule(this, PriorityType.URGENT, 0);
	}
	
	@Override
	public String toString() {
		return "Interruption:" + this.process.getId();
	}
}
