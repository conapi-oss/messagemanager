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
package nl.queuemanager.core;

import nl.queuemanager.core.util.CollectionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class TestMapNamespaceContext {

	private final MapNamespaceContext context = new MapNamespaceContext();
	
	@Before
	public void setup() {
		context.add("a", "urn:a");
		context.add("a2", "urn:a");
		context.add("b", "urn:b");
		context.add("b2", "urn:b");
		context.add("c", "urn:c");
		context.add("c2", "urn:c");
	}
	
	@Test
	public void testGetNamespaceURI() {
		assertEquals("urn:a", context.getNamespaceURI("a"));
		assertEquals("urn:a", context.getNamespaceURI("a2"));
	}

	@Test
	public void testGetPrefix() {
		String prefixA = context.getPrefix("urn:a");
		
		if(!"a".equals(prefixA) && !"a2".equals(prefixA))
			fail("Incorrect prefix: " + prefixA + " for uri urn:a");
		
		String prefixB = context.getPrefix("urn:b");
		
		if(!"b".equals(prefixB) && !"b2".equals(prefixB))
			fail("Incorrect prefix: " + prefixB + " for uri urn:b");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPrefixes() {
		Iterator<String> prefixIt = context.getPrefixes("urn:b");
		List<String> prefixes = CollectionFactory.newArrayList();
		while(prefixIt.hasNext()) {
			prefixes.add(prefixIt.next());
		}

		assertEquals(2, prefixes.size());
		assertTrue(prefixes.contains("b"));
		assertTrue(prefixes.contains("b2"));
	}

}
