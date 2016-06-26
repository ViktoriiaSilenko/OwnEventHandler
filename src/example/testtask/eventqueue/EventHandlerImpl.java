package example.testtask.eventqueue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.management.RuntimeErrorException;

public class EventHandlerImpl implements EventHandler {

	private final int EVENT_PRODUCER_COUNT = 3;
	private final int EVENT_COUNT = 10;

	private Queue<MyEvent> events = new LinkedList<>();

	private List<Thread> producerThreads; // event sources
	private Thread consumerThread; // event handler

	/**
	 * Scheduling new event.
	 * 
	 * @param event
	 * @throws Exception
	 *             In case if handler is not running.
	 */
	@Override
	public synchronized void addEvent(MyEvent event) throws Exception { // producer
		if (!consumerThread.isAlive()) {
			throw new IllegalStateException("Cannot add event if handler is not running");
		}
		events.add(event);
		notify();
	}

	/**
	 * Starts event handler engine.
	 *
	 * @throws Exception
	 *             In case of any error.
	 */
	@Override
	public void startHandler() throws Exception {
		producerThreads = new ArrayList<>();

		for (int j = 0; j < EVENT_PRODUCER_COUNT; j++) {
			final String infoMessage = "Producer" + j + " thread started...";
			final String putEventMessage = "Producer" + j + " thread put event";

			Thread producerThread = new Thread() {
				@Override
				public void run() {
					System.out.println(infoMessage);
					for (int i = 1; i <= EVENT_COUNT; i++) {
						System.out.println(putEventMessage + i);
						try {
							addEvent(new MyEvent("event" + i + " in " + System.nanoTime()));
						} catch (Exception ex) {
							throw new RuntimeException("Cannot add event if handler is not running");
						}

						try {
							sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				}
			};

			producerThreads.add(producerThread);
		}

		consumerThread = new Thread() {
			@Override
			public void run() {
				System.out.println("Consumer thread started...");

				while (true) {
					MyEvent event = getEvent();
					handleEvent(event);

				}
			}
		};

		consumerThread.start();
		if (producerThreads != null && !producerThreads.isEmpty()) {
			for (Thread producerThread : producerThreads) {
				producerThread.start();
			}
		}

		// consumerThread.start();

	}

	/**
	 * Stops event handler engine. This method should wait until all scheduled
	 * events are complete.
	 *
	 * @throws Exception
	 *             In case of any error.
	 */
	@Override
	public void stopHandler() throws Exception {
		if (consumerThread != null) {
			if (events.isEmpty()) {
				try {
					consumerThread.interrupt();
				} catch (SecurityException ex) {
					throw ex;
				}
			} else {
				for (MyEvent event : events) {
					handleEvent(event);
				}
				try {
					consumerThread.interrupt();
				} catch (SecurityException ex) { 
					throw ex;
				}
			}
		}

	}

	private synchronized MyEvent getEvent() { // consumer
		while (events.isEmpty()) { // no new message
			try {
				wait(); // release the lock of this object
			} catch (InterruptedException e) {
				System.out.println("InterruptedException in EventHandlerImpl.getEvent()");
			}
		}

		return events.poll();

	}

	private void handleEvent(MyEvent event) {
		System.out.println("Consumer thread get " + event.getName());
		try {
			event.execute();
		} catch (InterruptedException e) {

			System.out.println("InterruptedException in MyEvent.execute()");
		}
	}
}
