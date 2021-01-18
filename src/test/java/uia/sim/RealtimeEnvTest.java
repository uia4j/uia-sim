package uia.sim;

import org.junit.Test;

import uia.cor.Yield2Way;

public class RealtimeEnvTest {

    private RealtimeEnv env;

    private Event classBegin;

    private Event classEnd;

    public RealtimeEnvTest() {
        this.env = new RealtimeEnv();
        this.classBegin = this.env.event("classBegin");
        this.classEnd = this.env.event("classEnd");
        this.env.process("pupil-1", this::pupil);
        this.env.process("pupil-2", this::pupil);
        this.env.process("pupil-3", this::pupil);
        this.env.process("bell", this::bell);
    }

    public void bell(Yield2Way<Event, Object> yield) {
        while (yield.isAlive()) {
            this.classBegin.succeed(null);
            this.classBegin = this.env.event("classBegin");	// update event
            System.out.println(String.format("\n%3d> begin", this.env.getNow()));

            yield.call(this.env.timeout(45));

            this.classEnd.succeed(null);
            this.classEnd = this.env.event("classEnd");		// update event
            System.out.println(String.format("\n%3d> end", this.env.getNow()));

            yield.call(this.env.timeout(15));
        }
    }

    public void pupil(Yield2Way<Event, Object> yield) {
        while (yield.isAlive()) {
            yield.call(this.classBegin);
            System.out.print("|O| ");
            yield.call(this.classEnd);
            System.out.print("\\o/ ");
        }
    }

    @Test
    public void test1() throws Exception {
        this.env.run(350);
    }
}
