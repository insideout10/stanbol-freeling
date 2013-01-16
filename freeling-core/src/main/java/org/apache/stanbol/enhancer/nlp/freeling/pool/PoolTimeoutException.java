package org.apache.stanbol.enhancer.nlp.freeling.pool;

public class PoolTimeoutException extends Exception {

	PoolTimeoutException(final long timeOut, int poolSize, int numWaiting) {
		super(String.format("A timeout occurred while waiting for an Resource "
		    + "(timeout: %dms | pool size : %d | num waiting: %s ).",
						timeOut, poolSize, numWaiting));
	}

	private static final long serialVersionUID = 5493072017819568254L;

}
