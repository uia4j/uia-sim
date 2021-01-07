package uia.sim.factory;

import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.Event;
import uia.sim.Processable;

public class Job extends Processable {
	
	private Yield2Way<Event, Object> yield;
	
	private Equipment equip;
	
	Job(Env env, String id, Equipment equip) {
		super(env, id);
		this.equip = equip;
	}
	
	public void run(Yield2Way<Event, Object> yield) {
		this.yield = yield;
		try {
			System.out.println(String.format("%4d> %s move in  %s", this.env.getNow(), this.id, this.equip.getId()));
			yield.call(env.timeout(1000, this));
			System.out.println(String.format("%4d> %s move out %s", this.env.getNow(), this.id, this.equip.getId()));
		} catch (Exception e) {

		}
	}
	
	public void stop() {
		this.yield.close();
	}
	
	public String toString() {
		return this.id;
	}
}
