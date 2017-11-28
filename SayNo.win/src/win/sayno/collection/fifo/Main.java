package win.sayno.collection.fifo;

import win.sayno.collection.fifo.FIFOCache;

public class Main {

	public static void main(String[] args) {
		
		FIFOCache filoCache = new FIFOCache(10);
		for(int i=0; i<100;i++) {
			filoCache.put("index" + i, new Object());
		}

		filoCache.get("index90");
		filoCache.put("index100", new Object());
		
		for (String value : filoCache.keySet()) {
			System.out.println(value);
		}
	}

}
