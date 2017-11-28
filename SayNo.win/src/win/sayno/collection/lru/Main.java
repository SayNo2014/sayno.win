package win.sayno.collection.lru;

public class Main {

	public static void main(String[] args) {
		LRUCache lruCache = new LRUCache(10);
		for(int i=0; i<100;i++) {
			lruCache.put("index" + i, new Object());
		}

		lruCache.get("index90");
		lruCache.put("index100", new Object());
		
		for (String value : lruCache.keySet()) {
			System.out.println(value);
		}
		
	}

}
