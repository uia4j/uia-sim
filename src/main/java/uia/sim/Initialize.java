package uia.sim;

public class Initialize extends Event {

	private final Process process;
	
	public Initialize(Env env, Process process) {
		super(env, "Initialize");
		this.process = process;
		addCallable(e -> {
			this.process.resume();
		});

		env.schedule(this, PriorityType.URGENT, 0);
	}
	
	@Override
	public String toString() {
		return "Initialize:" + this.process.getId();
	}
}
