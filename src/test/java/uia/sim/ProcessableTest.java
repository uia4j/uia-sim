package uia.sim;

import org.junit.Test;

public class ProcessableTest {

	@Test
	public void testSimple1() {
		Env env = new Env();
		env.process(new Test1("test1", 10));
		env.run(401);
	}

	@Test
	public void testSimple2() {
		Env env = new Env();
		env.process(new Test2("test1", 3));
		env.run();
	}
	
	public static class Test1 extends Processable {

		private int repeat;
		
		protected Test1(String id, int repeat) {
			super(id);
			this.repeat = repeat;
		}

		@Override
		protected void run() {
			for(int i =0; i<this.repeat; i++) {
				yield(env().timeout(100));
				System.out.println(now());
			}
		}
		
		@Override
		public void initial() {
			System.out.println("env is ready");
		}
	}
	
	public static class Test2 extends Processable {

		private int repeat;
		
		protected Test2(String id, int repeat) {
			super(id);
			this.repeat = repeat;
		}

		@Override
		protected void run() {
			yield(env().timeout(50));
			System.out.println(now());
			if(this.repeat > 0) {
				env().process(new Test2(getId(), this.repeat - 1));
			}
		}
		
		@Override
		public void initial() {
			System.out.println("env is ready");
		}
	}
}
