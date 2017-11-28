package win.sayno.collection.fifo;

import java.util.LinkedHashMap;

public class FIFOCache extends LinkedHashMap<String,Object> {
	
	private static final long serialVersionUID = -4690312725507512374L;
	
	private volatile int cacheSize;
	
	public FIFOCache(int capacity) {
		super(capacity, 0.75f, false);
		cacheSize = capacity;
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String,Object> eldest) {
		return this.size() > cacheSize;
	}
}
