package nl.queuemanager.solace;

interface DataPanel<T> {
	public void displayItem(T item);
	public void updateItem(T item);
}
