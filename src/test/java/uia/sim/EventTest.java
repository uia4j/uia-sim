package uia.sim;

import org.junit.Assert;
import org.junit.Test;

public class EventTest {

	@Test
	public void testSucceed() {
		final Env env = new Env();
		env.process("testSucceed", y1 -> {
			final Event parent = env.event("parent");

			env.process("child", y2 -> {
				// Yield2Way get the return value.
				Object value = y2.call(parent);		
				Assert.assertEquals("ohai", value);
				Assert.assertEquals(5, env.getNow());
			});

			y1.call(env.timeout(5));
			parent.succeed("ohai");
		});
		env.run();
	}
	
	@Test
	public void testFail() {
		final Env env = new Env();
		env.process("testSucceed", y1 -> {
			final Event parent = env.event("parent");

			env.process("child", y2 -> {
				try {
					y2.call(parent);
					Assert.assertTrue(false);
				} catch (Exception e) {
					Assert.assertEquals("test failed", e.getMessage());
				}
			});

			y1.call(env.timeout(5));
			parent.fail(new Exception("test failed"));
		});
		env.run();
	}
	
	@Test
	public void testTriggered() {
		final Env env = new Env();
		final Event event = env.event("testTriggered");
		event.succeed("I am stupid");
		
		env.process("pem", y -> {
			Object value = y.call(event);
			Assert.assertEquals("I am stupid", value);
		});
		env.run();
	}
		
	
	@Test
	public void testValue() {
		final Env env = new Env();
		Event event = env.timeout(100, 200);
		Assert.assertEquals(100, env.run());
		Assert.assertEquals(200, event.getValue());
		
	}
}
