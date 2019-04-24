package edu.northeastern.ccs.im;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingWorker;

/**
 * Class that continuously scans for incoming chatterMessages. Because of the CPU
 * overhead that this entails, we place this work off onto a separate worker
 * thread. This has the side effect of not interfering with the event dispatch
 * thread. Like all <code>SwingWorker</code>s each instance should be executed
 * exactly once.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public final class ScanForMessagesWorker extends SwingWorker<Void, ChatterMessage> {
	/**
	 * IM connection instance for which we are working.
	 */
	private final IMConnection imConnection;

	/**
	 * List holding all chatterMessages we have read and not yet pushed out to the
	 * user.
	 */
	private List<ChatterMessage> chatterMessages;

	/** Instance which actually performs the low-level I/O with the server. */
	private ChatterSocketNB realConnection;

	/**
	 * Create an initialize the worker thread we will use to scan for incoming
	 * chatterMessages.
	 * 
	 * @param cimConnection
	 *            Instance to which this will be attached.
	 * @param sock
	 *            Socket instance which really hosts the connection to the
	 *            server
	 */
	ScanForMessagesWorker(IMConnection cimConnection, ChatterSocketNB sock) {
		// Record the instance and connection we will be using
		imConnection = cimConnection;
		realConnection = sock;
		// Create the queue that will hold the chatterMessages received from over the
		// network
		chatterMessages = new CopyOnWriteArrayList<ChatterMessage>();
	}

	/**
	 * Executes the steps needed to switch which method is being executed.
	 * 
	 * @return Null value needed to comply with original method definition
	 */
	@Override
	protected Void doInBackground() {
		while (!isCancelled()) {
			realConnection.enqueueMessages(chatterMessages);
			if (!chatterMessages.isEmpty()) {
				// Add this message into our queue
				publish(chatterMessages.remove(0));
			}
		}
		return null;
	}

	@Override
	protected void process(List<ChatterMessage> mess) {
		List<ChatterMessage> publishList = new LinkedList<ChatterMessage>();
		boolean flagForClosure = false;
		for (ChatterMessage m : mess) {
			switch (m.getType()) {
			case QUIT:
				flagForClosure = true;
				break;
			case BROADCAST:
				publishList.add(m);
				break;
			case NO_ACKNOWLEDGE:
				cancel(false);
				realConnection = null;
				imConnection.fireStatusChange(imConnection.getUserName());
				break;
			case ACKNOWLEDGE:
				imConnection.fireStatusChange(imConnection.getUserName());
				break;
			default:
				// ChatterMessage does need to do anything; do nothing.
			}
		}
		if (!publishList.isEmpty()) {
			imConnection.fireSendMessages(publishList);
		}
		if (flagForClosure) {
			cancel(false);
			realConnection = null;
			imConnection.loggedOut();
			imConnection.fireStatusChange(imConnection.getUserName());
		}
	}
}