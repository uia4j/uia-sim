package uia.sim.factory;

import java.util.ArrayList;
import java.util.Vector;

import uia.sim.Observable;

public class Operation {
	
	private String id;

	private Vector<String> products;
	
	private ArrayList<Observable<Operation>> observables;
	
	public Operation(String id) {
		this.id = id;
		this.products = new Vector<>();
		this.observables = new ArrayList<>();
	}
	
	public void bindObservable(Observable<Operation> observable) {
		this.observables.add(observable);
	}
		
	public synchronized void enqueue(String productsId) {
		this.products.add(productsId);
		if(this.products.size() == 1) {
			this.observables.forEach(o -> o.available(this));
		}
	}
	
	public synchronized String dequeue() {
		return this.products.isEmpty() ? null : this.products.remove(0);
	}
	
	@Override
	public String toString() {
		return this.id;
	}
}
