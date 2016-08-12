package com.hantek.ttia.module.handshake;

public class AutoResetEvent {
	private final Object _monitor = new Object();
	private volatile boolean _isOpen = false;

	public AutoResetEvent(boolean open) {
		_isOpen = open;
	}

	public void waitOne() throws InterruptedException {
		synchronized (_monitor) {
			while (!_isOpen) {
				_monitor.wait();
			}
			_isOpen = false;
		}
	}

	public boolean waitOne(long timeout) throws InterruptedException {
		synchronized (_monitor) {
			long t = System.currentTimeMillis();
			while (!_isOpen) {
				_monitor.wait(timeout);
				// Check for timeout
				if (System.currentTimeMillis() - t >= timeout)
					return false;
				else
					return true;
			}
			_isOpen = false;
		}

		return true;
	}

	public void set() {
		synchronized (_monitor) {
			_isOpen = true;
			_monitor.notify();
		}
	}

	public void reset() {
		_isOpen = false;
	}
}