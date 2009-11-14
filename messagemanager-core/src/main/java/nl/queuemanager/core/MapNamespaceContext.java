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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

import nl.queuemanager.core.util.CollectionFactory;

public class MapNamespaceContext implements NamespaceContext {

	private Map<String, String> namespaces = CollectionFactory.newHashMap();
	
	public void add(String prefix, String uri) {
		namespaces.put(prefix, uri);
	}
	
	public String getNamespaceURI(String prefix) {
		return namespaces.get(prefix);
	}

	public String getPrefix(String uri) {
		for(Iterator<Map.Entry<String, String>> it = namespaces.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			if(uri.equals(entry.getValue()))
				return entry.getKey();
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public Iterator getPrefixes(String uri) {
		List<String> result = CollectionFactory.newArrayList();
		
		for(Iterator<Entry<String, String>> it = namespaces.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();
			if(uri.equals(entry.getValue()))
				result.add(entry.getKey());
		}
		
		return result.iterator();
	}
}
