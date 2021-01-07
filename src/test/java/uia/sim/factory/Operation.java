package uia.sim.factory;

import java.util.Vector;

public class Operation {

	private Vector<String> products;
	
	public Operation() {
		this.products = new Vector<>();
	}
	
	public synchronized void enqueue(String productsId) {
		this.products.add(productsId);
	}
	
	public synchronized String dequeue() {
		return this.products.isEmpty() ? null : this.products.remove(0);
	}
	
	
	
}
