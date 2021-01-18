package uia.sim.factory;

import java.util.Vector;

import uia.sim.Observable;
import uia.sim.Processable;

public class Equip extends Processable {

    private final Vector<Operation> operations;

    private Observable<Operation> observable;

    public Equip(String id) {
        super(id);
        this.operations = new Vector<>();
    }

    public void bind(Operation operation) {
        this.operations.add(operation);
    }

    @Override
    protected void run() {
        while (yield().isAlive()) {
            String productionId = null;
            for (Operation op : this.operations) {
                productionId = op.dequeue();
                if (productionId != null) {
                    this.operations.remove(op);
                    this.operations.add(op);
                    break;
                }
            }
            if (productionId == null) {
                System.out.println(String.format("%4d> %s idle", now(), getId()));
                for (Operation o : this.operations) {
                    o.bindObservable(this.observable);
                }
                Object value = yield(this.observable.ask(proc()));
                System.out.println(String.format("%4d> %s enqueue", now(), value));
            }
            else {
                System.out.println(String.format("%4d> %s move in  %s", now(), getId(), productionId));
                yield(env().timeout(50));
                System.out.println(String.format("%4d> %s move out %s", now(), getId(), productionId));
            }
        }
    }

    @Override
    protected void doBind() {
        this.observable = new Observable<Operation>(env(), getId() + "_op_obvr");
    }
}
