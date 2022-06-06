/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.jms.impl;

import javax.jms.JMSException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import javax.jms.StreamMessage;
import java.io.*;

/**
 * Stream message implementation that uses a byte array for storage.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class JMSStreamMessage extends JMSMessage implements StreamMessage, Serializable {

	private static final long serialVersionUID = 6236058322245760102L;
	
	protected byte[] body;
	protected transient ByteArrayInputStream bin;
	protected transient ObjectInputStream oin; 
	
	protected transient ByteArrayOutputStream bout;
	protected transient ObjectOutputStream oout;

	private transient ByteArrayInputStream currentByteArray;
	
	/**
	 * Create a new StreamMessage in write-only mode.
	 * @throws JMSException 
	 */
	JMSStreamMessage() {
		clearBody();
	}
	
	@Override
	public void clearBody() {
		try {
			body = null;
			bin = null;
			oin = null;
			bout = new ByteArrayOutputStream();
			oout = new ObjectOutputStream(bout);
		} catch (IOException e) {
		}
	}
	
	public boolean readBoolean() throws JMSException {
		return (Boolean)readObject();
	}

	public byte readByte() throws JMSException {
		return (Byte)readObject();
	}

	public int readBytes(byte[] dst) throws JMSException {
		checkReadable();
		try {
			if(currentByteArray == null) {
				byte[] data = (byte[])readObject();
				currentByteArray = new ByteArrayInputStream(data);
			}
			
			int ret = currentByteArray.read(dst);
			if(ret == -1) {
				currentByteArray = null;
			}
			return ret;
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}
	
	public char readChar() throws JMSException {
		return (Character)readObject();
	}

	public double readDouble() throws JMSException {
		return (Double)readObject();
	}

	public float readFloat() throws JMSException {
		return (Float)readObject();
	}

	public int readInt() throws JMSException {
		return (Integer)readObject();
	}

	public long readLong() throws JMSException {
		return (Long)readObject();
	}

	public Object readObject() throws JMSException {
		checkReadable();
		try {
			return oin.readObject();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		} catch (ClassNotFoundException e) {
			throw new JMSException(e.toString());
		}
	}
	
	public short readShort() throws JMSException {
		return (Short)readObject();
	}
	
	public String readString() throws JMSException {
		return (String)readObject();
	}

	public void reset() throws JMSException {
		
		try {
			if(bin != null) {
				bin.reset();
				oin = new ObjectInputStream(bin);
			} else {
				oout.flush();
				bout.flush();
				body = bout.toByteArray();
				bin = new ByteArrayInputStream(body);
				oin = new ObjectInputStream(bin);
				bout = null;
				oout = null;
			}
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeBoolean(boolean arg0) throws JMSException {
		writeObject(arg0);
	}

	public void writeByte(byte arg0) throws JMSException {
		writeObject(arg0);
	}

	public void writeBytes(byte[] arg0) throws JMSException {
		writeObject(arg0);
	}
	
	public void writeBytes(byte[] data, int offset, int length) throws JMSException {
		checkWritable();
		byte[] buf = new byte[length];
		System.arraycopy(data, offset, buf, 0, length);
		writeObject(buf);
	}

	public void writeChar(char arg0) throws JMSException {
		writeObject(arg0);
	}

	public void writeDouble(double arg0) throws JMSException {
		writeObject(arg0);
	}

	public void writeFloat(float arg0) throws JMSException {
		writeObject(arg0);
	}

	public void writeInt(int arg0) throws JMSException {
		writeObject(arg0);
	}

	public void writeLong(long arg0) throws JMSException {
		writeObject(arg0);
	}

	public void writeObject(Object obj) throws JMSException {
		checkWritable();
		try {
			if(obj instanceof Boolean)
				oout.writeObject(obj);
			else if(obj instanceof Byte)
				oout.writeObject(obj);
			else if(obj instanceof byte[])
				oout.writeObject(obj);
			else if(obj instanceof Character)
				oout.writeObject(obj);
			else if(obj instanceof Double)
				oout.writeObject(obj);
			else if(obj instanceof Float)
				oout.writeObject(obj);
			else if(obj instanceof Integer)
				oout.writeObject(obj);
			else if(obj instanceof Long)
				oout.writeObject(obj);
			else if(obj instanceof Short)
				oout.writeObject(obj);
			else if(obj instanceof String)
				oout.writeObject(obj);
			else throw new JMSException("Object not of allowed type!");
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeShort(short arg0) throws JMSException {
		writeObject(arg0);
	}
	
	public void writeString(String arg0) throws JMSException {
		writeObject(arg0);
	}	
	
	protected void checkReadable() throws MessageNotReadableException {
		if(oin == null)
			throw new MessageNotReadableException("Message is in write-only mode");
	}
	
	protected void checkWritable() throws MessageNotWriteableException {
		if(oout == null)
			throw new MessageNotWriteableException("Message is in read-only mode");
	}
}
