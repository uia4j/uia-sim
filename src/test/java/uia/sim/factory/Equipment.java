package uia.sim.factory;

import java.util.Vector;

import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.Event;
import uia.sim.Processable;

public class Equipment extends Processable {

	private Yield2Way<Event, Object> yield;
		
	private final Vector<Operation> operations;
	
	Equipment(Env env, String id) {
		super(env, id);
		this.operations = new Vector<>();
	}
	
	public void bind(Operation operation) {
		this.operations.add(operation);
	}

	public void run(Yield2Way<Event, Object> yield) {
		this.yield = yield;
		while(this.yield.isAlive()) {
			String productionId = null;
			for(Operation op : this.operations) {
				productionId = op.dequeue();
				if(productionId != null) {
					this.operations.remove(op);
					this.operations.add(op);
					break;
				}
			}
			if(productionId == null) {
				yield.call(this.env.timeout(100));
			}
			else {
				yield.call(env.process(new Job(this.env, productionId, this)));
			}
		}
	}
	
	public void stop() {
		this.yield.close();
	}
}
