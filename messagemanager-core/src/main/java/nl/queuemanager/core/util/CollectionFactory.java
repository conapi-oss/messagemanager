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
package nl.queuemanager.core.util;

import java.util.*;

/**
 * Static class to allow for easy creation of JDK collection classes. Inspired by Google Collections
 * Lists, Sets and Maps but not quite as extensive.
 * 
 * @author Gerco Dries
 *
 */
public class CollectionFactory {
	private CollectionFactory(){}

	/**
	 * Return a new empty Arraylist
	 * 
	 * @param <E>
	 * @return
	 */
	public static <E> ArrayList<E> newArrayList() {
	    return new ArrayList<E>();
	}

	/**
	 * Create an Arraylist with the following elements
	 * 
	 * @param <E>
	 * @param elements
	 * @return
	 */
	public static <E> ArrayList<E> newArrayList(E... elements) {
		ArrayList<E> list = new ArrayList<E>((int)(elements.length * 1.1));
		Collections.addAll(list, elements);
		return list;
	}
	
	/**
	 * Create a new ArrayList with the contents of another List
	 *  
	 * @param <E>
	 * @param list
	 * @return
	 */
	public static <E> ArrayList<E> newArrayList(List<? extends E> list) {
		return new ArrayList<E>(list);
	}

	/**
	 * Create a new HashMap
	 * 
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	/**
	 * Create a new HashMap with the contents of an existing Map
	 * 
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
		return new HashMap<K, V>(map);
	}

	/**
	 * Create a new HashMap with the specified keys and values
	 * 
	 * @param keys
	 * @param values
	 * @return
	 */
	public static <K, V> HashMap<K, V> newHashMap(K[] keys, V[] values) {
		HashMap<K, V> map = newHashMap();
		for(int i=0; i< keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		return map;
	}
	
}
