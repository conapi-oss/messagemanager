package nl.queuemanager.solace;

class SempException extends Exception {

	private static final long serialVersionUID = 3495720910560709771L;
	private final int responseCode;
	private final String responseMessage;
	private final byte[] response;
	
	public SempException(int responseCode, String responseMessage, byte[] response) {
		super();
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.response = response;
	}

	public SempException(int responseCode, String responseMessage, byte[] response, String message) {
		super(message);
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.response = response;
	}
	
	public SempException(int responseCode, String responseMessage, byte[] response, String message, Throwable cause) {
		super(message, cause);
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.response = response;
	}
	
	public SempException(byte[] response, String message) {
		super(message);
		this.responseCode = 0;
		this.responseMessage = "";
		this.response = response;
	}
	
	public SempException(byte[] response, String message, Throwable cause) {
		super(message, cause);
		this.responseCode = 0;
		this.responseMessage = "";
		this.response = response;
	}
	
	public SempException(String message, Throwable cause) {
		super(message, cause);
		this.responseCode = 0;
		this.responseMessage = "";
		this.response = null;
	}
	
	public SempException(String message) {
		super(message);
		this.responseCode = 0;
		this.responseMessage = "";
		this.response = null;
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	public String getResponseMessage() {
		return responseMessage;
	}

	public byte[] getResponse() {
		return response;
	}
	
}