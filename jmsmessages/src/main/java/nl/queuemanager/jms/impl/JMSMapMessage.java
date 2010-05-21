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

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.MapMessage;

/**
 * Map message implementation that uses a HashMap as the storage method.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class JMSMapMessage extends JMSMessage implements MapMessage, Serializable {

	private static final long serialVersionUID = 6025598183944056084L;
	
	protected final Map<String, Object> body = new HashMap<String, Object>();

	public boolean getBoolean(String name) {
		return (Boolean)body.get(name);
	}

	
	public byte getByte(String name) {
		return (Byte)body.get(name);
	}

	
	public byte[] getBytes(String name) {
		return (byte[])body.get(name);
	}

	
	public char getChar(String name) {
		return (Character)body.get(name);
	}

	
	public double getDouble(String name) {
		return (Double)body.get(name);
	}

	
	public float getFloat(String name) {
		return (Float)body.get(name);
	}

	
	public int getInt(String name) {
		return (Integer)body.get(name);
	}

	
	public long getLong(String name) {
		return (Long)body.get(name);
	}

	
	public Enumeration<String> getMapNames() {
		return Collections.enumeration(body.keySet());
	}

	
	public Object getObject(String name) {
		return body.get(name);
	}

	
	public short getShort(String name) {
		return (Short)body.get(name);
	}

	
	public String getString(String name) {
		return String.valueOf(body.get(name));
	}

	
	public boolean itemExists(String name) {
		return body.containsKey(name);
	}

	
	public void setBoolean(String name, boolean value) {
		body.put(name, value);
	}

	
	public void setByte(String name, byte value) {
		body.put(name, value);
	}

	
	public void setBytes(String name, byte[] value) {
		body.put(name, value);
	}

	
	public void setBytes(String name, byte[] value, int offset, int length) {
		byte[] data = new byte[length];
		System.arraycopy(value, offset, data, 0, length);
		body.put(name, data);
	}

	
	public void setChar(String name, char value) {
		body.put(name, value);
	}

	
	public void setDouble(String name, double value) {
		body.put(name, value);
	}

	
	public void setFloat(String name, float value) {
		body.put(name, value);
	}

	
	public void setInt(String name, int value) {
		body.put(name, value);
	}

	
	public void setLong(String name, long value) {
		body.put(name, value);
	}

	
	public void setObject(String name, Object value) {
		body.put(name, value);
	}

	
	public void setShort(String name, short value) {
		body.put(name, value);
	}

	
	public void setString(String name, String value) {
		body.put(name, value);
	}

}
