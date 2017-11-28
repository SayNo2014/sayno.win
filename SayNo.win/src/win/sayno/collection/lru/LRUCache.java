package win.sayno.collection.lru;

import java.util.LinkedHashMap;

public class LRUCache extends LinkedHashMap<String,Object> {
	
	private static final long serialVersionUID = -4690312725507512374L;
	
	private volatile int cacheSize;
	
	public LRUCache(int capacity) {
		super(capacity, 0.75f, true);
		cacheSize = capacity;
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String,Object> eldest) {
		return this.size() > cacheSize;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
