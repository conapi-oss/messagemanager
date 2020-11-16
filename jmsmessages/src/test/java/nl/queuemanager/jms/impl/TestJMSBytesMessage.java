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

import nl.queuemanager.jms.impl.MessageFactory;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestJMSBytesMessage {

	@Test
	public void testBytesMessage() throws JMSException {
		BytesMessage msg = MessageFactory.createBytesMessage();
		msg.writeBoolean(true);
		msg.writeBoolean(false);
		msg.writeChar('c');
		msg.writeDouble(1.0);
		msg.writeFloat(2.0f);
		msg.writeInt(3);
		msg.writeLong(4L);
		msg.writeObject(Integer.valueOf(31337));
		msg.writeShort((short)123);
		msg.writeUTF("UTF string");

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
		assertEquals("UTF string", msg.readUTF());
		
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
		assertEquals("UTF string", msg.readUTF());
	}

	@Test
	public void testWriteReadObject() throws JMSException {
		BytesMessage msg = MessageFactory.createBytesMessage();
		
		msg.writeObject(Integer.valueOf(123));
		msg.reset();
		assertEquals(4, msg.getBodyLength());
		assertEquals(123, msg.readInt());
	}
	
	@Test
	public void testWriteReadBytes() throws JMSException {
		BytesMessage msg = MessageFactory.createBytesMessage();

		byte[] in = new byte[256];
		for(int i=0; i<256; i++)
			in[i] = (byte)i;
		
		msg.writeBytes(in);
		msg.reset();
		
		assertEquals(256, msg.getBodyLength());

		byte[] out = new byte[256];
		msg.readBytes(out);
		assertArrayEquals(in, out);
	}
}
