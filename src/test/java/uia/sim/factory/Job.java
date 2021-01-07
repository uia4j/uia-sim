package uia.sim.factory;

import uia.sim.Processable;

public class Job extends Processable {
	
	private Equipment equip;
	
	public Job(String id, Equipment equip) {
		super(id);
		this.equip = equip;
	}
	
	@Override
	protected void run() {
		try {
			System.out.println(String.format("%4d> %s move in  %s", env().getNow(), getId(), this.equip.getId()));
			yield(env().timeout(1000, this));
			System.out.println(String.format("%4d> %s move out %s", env().getNow(), getId(), this.equip.getId()));
		} catch (Exception e) {

		}
	}
}
