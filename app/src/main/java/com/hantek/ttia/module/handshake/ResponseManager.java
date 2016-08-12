package com.hantek.ttia.module.handshake;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wsh on 2015/8/12.
 */
public class ResponseManager {
	private static ResponseManager ourInstance = new ResponseManager();

	public static ResponseManager getInstance() {
		return ourInstance;
	}

	private Map<Integer, String> responseMap = new HashMap<Integer, String>();
	private Map<Integer, AutoResetEvent> lockMap = new HashMap<Integer, AutoResetEvent>();

	private ResponseManager() {
	}

	public boolean waitResponse(Integer key, int timeout) throws InterruptedException {
		System.out.println("Wait: " + key + " Timeout:" + timeout);
		AutoResetEvent resetEvent = new AutoResetEvent(false);
		synchronized (lockMap) {
			if (lockMap.containsKey(key))
				return false;

			lockMap.put(key, resetEvent);
		}

		boolean result = resetEvent.waitOne(timeout);
		if (!result)
			synchronized (lockMap) {
				if (lockMap.containsKey(key))
					lockMap.remove(key);
			}
		return result;
	}

	public void set(Integer key, String response) {
		System.out.println("set: " + key + " Resp:" + response);
		synchronized (responseMap) {
			responseMap.put(key, response);
		}
		synchronized (lockMap) {
			if (lockMap.containsKey(key)) {
				lockMap.get(key).set();
				lockMap.remove(key);
			}
		}
	}

	public Object getResponse(Integer key) {
		System.out.println("getResponse: " + key);
		String result = null;
		synchronized (responseMap) {
			if (responseMap.containsKey(key)) {
				result = responseMap.get(key);
			}
		}

		set(key, result); // avoid duplicate
		return result;
	}
}
