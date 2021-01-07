package uia.sim.factory;

import java.util.Vector;

import uia.sim.Processable;

public class Equipment extends Processable {
		
	private final Vector<Operation> operations;
	
	public Equipment(String id) {
		super(id);
		this.operations = new Vector<>();
	}
	
	public void bind(Operation operation) {
		this.operations.add(operation);
	}

	@Override
	protected void run() {
		while(yield().isAlive()) {
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
				yield(env().timeout(100));
			}
			else {
				yield(env().process(new Job(productionId, this)));
			}
		}
	}
}
