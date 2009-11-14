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
package nl.queuemanager.core.jms.impl;

import static org.junit.Assert.assertEquals;

import javax.jms.JMSException;
import javax.jms.StreamMessage;

import nl.queuemanager.core.jms.impl.MessageFactory;

import org.junit.Before;
import org.junit.Test;

public class TestJMSStreamMessage {

	StreamMessage msg;
	
	@Before
	public void setup() {
		msg = MessageFactory.createStreamMessage();	
	}
	
	@Test
	public void testStreamMessage() throws JMSException {
		msg.writeBoolean(true);
		msg.writeBoolean(false);
		msg.writeChar('c');
		msg.writeDouble(1.0);
		msg.writeFloat(2.0f);
		msg.writeInt(3);
		msg.writeLong(4L);
		msg.writeObject(new Integer(31337));
		msg.writeShort((short)123);
		msg.writeString("UTF string");

		msg.reset();
		assertEquals(true, msg.readBoolean());
		assertEquals(false, msg.readBoolean());
		assertEquals('c', msg.readChar());
		assertEquals(1.0, msg.readDouble(), 0.01);
		assertEquals(2.0f, msg.readFloat(), 0.01);
		assertEquals(3, msg.readInt());
		assertEquals(4L, msg.readLong());
		assertEquals(31337, msg.readInt());
		assertEquals((short)123, msg.readShort());
		assertEquals("UTF string", msg.readString());
		
		msg.reset();
		assertEquals(true, msg.readBoolean());
		assertEquals(false, msg.readBoolean());
		assertEquals('c', msg.readChar());
		assertEquals(1.0, msg.readDouble(), 0.01);
		assertEquals(2.0f, msg.readFloat(), 0.01);
		assertEquals(3, msg.readInt());
		assertEquals(4L, msg.readLong());
		assertEquals(31337, msg.readInt());
		assertEquals((short)123, msg.readShort());
		assertEquals("UTF string", msg.readString());
	}

	@Test
	public void testWriteReadObjects() throws JMSException {
		msg.writeBoolean(true);
		msg.writeBoolean(false);
		msg.writeChar('c');
		msg.writeDouble(1.0);
		msg.writeFloat(2.0f);
		msg.writeInt(3);
		msg.writeLong(4L);
		msg.writeObject(new Integer(31337));
		msg.writeShort((short)123);
		msg.writeString("UTF string");

		msg.reset();
		assertEquals(true, msg.readObject());
		assertEquals(false, msg.readObject());
		assertEquals('c', msg.readObject());
		assertEquals(1.0, (Double)msg.readObject(), 0.01);
		assertEquals(2.0f, (Float)msg.readObject(), 0.01);
		assertEquals(3, msg.readObject());
		assertEquals(4L, msg.readObject());
		assertEquals(31337, msg.readObject());
		assertEquals((short)123, msg.readObject());
		assertEquals("UTF string", msg.readObject());
	}
	
	@Test
	public void testWriteReadInt() throws JMSException {
		msg.writeObject(new Integer(123));
		msg.reset();
		assertEquals(123, msg.readInt());
	}
	
	@Test
	public void testWriteReadByteArray() throws JMSException {
		byte[] in = new byte[256];
		for(int i=0; i<256; i++)
			in[i] = (byte)i;
		
		msg.writeBytes(in);
		msg.reset();
		
		int j = 0;
		byte[] out = new byte[16];
		while(msg.readBytes(out) != -1) {
			for(int k=0; k<out.length; k++, j++) {
				assertEquals(in[j], out[k]);
			}
		}
		
		assertEquals(in.length, j);
	}
}
