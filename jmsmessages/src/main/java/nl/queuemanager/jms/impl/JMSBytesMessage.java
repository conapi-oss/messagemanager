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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import java.io.*;

class JMSBytesMessage extends JMSMessage implements BytesMessage, Serializable {

	protected byte[] body;
	protected transient ByteArrayInputStream bin;
	protected transient DataInputStream din; 
	
	protected transient ByteArrayOutputStream bout;
	protected transient DataOutputStream dout;

	/**
	 * Create a new BytesMessage in write-only mode.
	 * @throws JMSException 
	 */
	JMSBytesMessage() {
		clearBody();
	}
	
	@Override
	public void clearBody() {
		body = null;
		bin = null;
		din = null;
		bout = new ByteArrayOutputStream();
		dout = new DataOutputStream(bout);
	}
	
	public long getBodyLength() throws JMSException {
		checkReadable();
		return body.length;
	}

	public boolean readBoolean() throws JMSException {
		checkReadable();
		try {
			return din.readBoolean();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public byte readByte() throws JMSException {
		checkReadable();
		try {
			return din.readByte();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public int readBytes(byte[] dst) throws JMSException {
		checkReadable();
		return readBytes(dst, dst.length);
	}

	public int readBytes(byte[] dst, int length) throws JMSException {
		checkReadable();
		if(length < 0 || length > dst.length)
			throw new IndexOutOfBoundsException();

		try {
			for(int i = 0; i<length; i++)
				dst[i] = (byte)din.read();
			return length;
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public char readChar() throws JMSException {
		checkReadable();
		try {
			return din.readChar();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public double readDouble() throws JMSException {
		checkReadable();
		try {
			return din.readDouble();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public float readFloat() throws JMSException {
		checkReadable();
		try {
			return din.readFloat();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public int readInt() throws JMSException {
		checkReadable();
		try {
			return din.readInt();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public long readLong() throws JMSException {
		checkReadable();
		try {
			return din.readLong();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public short readShort() throws JMSException {
		checkReadable();
		try {
			return din.readShort();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public String readUTF() throws JMSException {
		checkReadable();
		try {
			return din.readUTF();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public int readUnsignedByte() throws JMSException {
		checkReadable();
		try {
			return din.readUnsignedByte();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public int readUnsignedShort() throws JMSException {
		checkReadable();
		try {
			return din.readUnsignedShort();
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void reset() throws JMSException {
		
		try {
			if(bin != null) {
				bin.reset();
				din = new DataInputStream(bin);
			} else {
				dout.flush();
				bout.flush();
				body = bout.toByteArray();
				bin = new ByteArrayInputStream(body);
				din = new DataInputStream(bin);
				bout = null;
				dout = null;
			}
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeBoolean(boolean arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeBoolean(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeByte(byte arg0) throws JMSException {
		checkWritable();
		try {
			dout.write(arg0);			
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeBytes(byte[] arg0) throws JMSException {
		checkWritable();
		bout.write(arg0, 0, arg0.length);
	}

	public void writeBytes(byte[] value, int offset, int length) throws JMSException {
		checkWritable();
		bout.write(value, offset, length);
	}

	public void writeChar(char arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeChar(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeDouble(double arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeDouble(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeFloat(float arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeFloat(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeInt(int arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeInt(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeLong(long arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeLong(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeObject(Object obj) throws JMSException {
		checkWritable();
		try {
			if(obj instanceof Boolean)
				dout.writeBoolean((Boolean)obj);
			else if(obj instanceof Byte)
				dout.writeByte((Byte)obj);
			else if(obj instanceof byte[])
				dout.write((byte[])obj);
			else if(obj instanceof Character)
				dout.writeChar((Character)obj);
			else if(obj instanceof Double)
				dout.writeDouble((Double)obj);
			else if(obj instanceof Float)
				dout.writeFloat((Float)obj);
			else if(obj instanceof Integer)
				dout.writeInt((Integer)obj);
			else if(obj instanceof Long)
				dout.writeLong((Long)obj);
			else if(obj instanceof Short)
				dout.writeShort((Short)obj);
			else if(obj instanceof String)
				dout.writeUTF((String)obj);
			else throw new JMSException("Object not of allowed type!");
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeShort(short arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeShort(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public void writeUTF(String arg0) throws JMSException {
		checkWritable();
		try {
			dout.writeUTF(arg0);
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}	
	
	protected void checkReadable() throws MessageNotReadableException {
		if(din == null)
			throw new MessageNotReadableException("Message is in write-only mode");
	}
	
	protected void checkWritable() throws MessageNotWriteableException {
		if(dout == null)
			throw new MessageNotWriteableException("Message is in read-only mode");
	}
}
