package uia.sim.resources;

import java.util.ArrayList;

import uia.sim.Env;

public class Resource extends BaseResource<Resource> {
	
	private ArrayList<Request> requests;

	public Resource(Env env, int capacity) {
		super(env, capacity);
		this.requests = new ArrayList<>();
	}
	
	public Request request(String id) {
		return new Request(this, id);
	}
	
	public Release release(String id, Request request) {
		return new Release(this, id, request);
	}
	
	@Override
	public boolean doRequest(BaseRequest<Resource> request) {
		if(this.requests.size() < this.capacity) {
			this.requests.add((Request)request);
			request.succeed(null);
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean doRelease(BaseRelease<Resource> release) {
		this.requests.remove((Request)release.request);
		release.succeed(null);
		return true;
	}
	
	
	public static class Request extends BaseRequest<Resource> {
		
		public Request(Resource resource, String id) {
			super(resource, id);
			this.resource.addRequest(this);
		}
		
		@Override
		public void exit() {
			this.resource.release(getId(), this);
		}
	}
	
	public static class Release extends BaseRelease<Resource> {
		
		public Release(Resource resource, String id, Request request) {
			super(resource, id + "_release", request);
			this.resource.addRelease(this);
		}
		
		@Override
		public void exit() {
		}
	}

}
