package uia.sim.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.sim.Env;
import uia.sim.Event;

public abstract class BaseResource<T extends BaseResource<T>> {

    private static final Logger logger = LogManager.getLogger(BaseResource.class);

    protected final Env env;
	
	protected final int capacity;
	
	private final List<BaseRequest<T>> requestQueue;

	private final List<BaseRelease<T>> releaseQueue;
	
	private Consumer<Event> scanRequestCallable;
	
	private Consumer<Event> scanReleaseCallable;

	public BaseResource(Env env, int capacity) {
		this.env = env;
		this.capacity = capacity;
		this.requestQueue = new ArrayList<>();
		this.releaseQueue = new ArrayList<>();
		this.scanRequestCallable = this::scanRequest;
		this.scanReleaseCallable = this::scanRelease;
	}
	
	public Env getEnv() {
		return this.env;
	}

	protected void addRequest(BaseRequest<T> request) {
		this.requestQueue.add(request);
		request.addCallable(this.scanReleaseCallable);	// 
		scanRequest(request);					//
	}

	protected void removeRequest(BaseRequest<T> request) {
		this.requestQueue.remove(request);
	}

	protected void addRelease(BaseRelease<T> release) {
		this.releaseQueue.add(release);
		release.addCallable(this.scanRequestCallable);	//
		this.scanRelease(release);
	}

	protected void removeRelease(BaseRelease<T> get) {
		this.releaseQueue.remove(get);
	}
	
	protected abstract boolean doRequest(BaseRequest<T> request);

	protected abstract boolean doRelease(BaseRelease<T> release);

	/**
	 * This method is called once a new Request event has been created 
	 * or a Release event has been processed.<br>
	 * 
	 * @param by
	 */
	private void scanRequest(Event by) {
		// 對累績的 requests 進行檢查。
		int idx = 0;
		while(idx < this.requestQueue.size()) {
			BaseRequest<T> request = this.requestQueue.get(idx);
			// request 申請  resource 占用
			boolean proceed = doRequest(request);
			logger.debug(String.format("res> scanRequest(%s)> %s, triggered:%s, proceed:%s", 
					by,
					request,
					request.isTriggered(),
					proceed));
			if(!request.isTriggered()) {
				idx++;
			}
			else {
				// 將已經觸發完成的 request 移出佇列。
				if(this.requestQueue.remove(idx) != request) {
					throw new RuntimeException("Put queue invariant violated");
				}
			}
			
			// 當 request 無法處理，視同資源耗盡，中斷檢查作業。
			if(!proceed) {
				break;
			}
		}
	}

	/**
	 * This method is called once a new Get event has been created 
	 * or a Put event has been processed.<br>
	 * 
	 * @param by
	 */
	private void scanRelease(Event by) {
		// 對累績的 release 進行檢查。
		int idx = 0;
		while(idx < this.releaseQueue.size()) {
			BaseRelease<T> release = this.releaseQueue.get(idx);
			// 進行 release 作業
			// release 歸還  resource 占用
			boolean proceed = doRelease(release);
			logger.debug(String.format("res> scanRelease(%s)> %s, triggered:%s, proceed:%s", 
					by,
					release, 
					release.isTriggered(),
					proceed));
			if(!release.isTriggered()) {
				idx++;
			}
			else {
				// 將已經觸發完成的 release 移出佇列。
				if(this.releaseQueue.remove(idx) != release) {
					throw new RuntimeException("Get queue invariant violated");
				}
			}
			
			if(!proceed) {
				break;
			}
		}
	}
	
}
