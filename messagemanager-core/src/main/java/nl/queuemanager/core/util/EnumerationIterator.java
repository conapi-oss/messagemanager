package nl.queuemanager.core.util;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<T> implements Iterator<T>, Iterable<T> {

	private Enumeration<T> delegate;
	
	public EnumerationIterator(Enumeration<T> enumeration) {
		this.delegate = enumeration;
	}
	
	public boolean hasNext() {
		return delegate.hasMoreElements();
	}

	public T next() {
		return delegate.nextElement();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Iterator<T> iterator() {
		return new EnumerationIterator<T>(delegate);
	}

	public static <T> EnumerationIterator<T> of(Enumeration<T> e) {
		return new EnumerationIterator<>(e);
	}

}
