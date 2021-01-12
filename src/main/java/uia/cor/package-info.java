/**
 * The Yield-Generator API.
 * 
 * <p>
 * The example below inherits from Yieldable to run the Yield-Generator program.
 * </p>
 * <pre>{@code
 * public class CallFor extends Yieldable<Integer> {
 *
 *     protected void run() {
 *         for(int i = 0; i < 10; i++) {
 *             yield(i);
 *         }
 *     }
 * }
 * 
 * Generator<Integer> gen = Yield.accept(new CallFor());
 * int i = 0;
 * while(gen.next()) {
 *     System.out.println("value=" + gen.getValue());
 *     Assert.assertEquals(i, gen.getValue().intValue());
 *     i++;
 * }
 * 
 *}</pre>
 * 
 */
package uia.cor;