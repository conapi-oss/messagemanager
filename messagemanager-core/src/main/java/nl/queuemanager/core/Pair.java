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

import java.util.Comparator;

public class Pair<First, Second> implements java.io.Serializable {
	private final First one;
	private final Second two;

	public Pair(First one, Second two) {
		this.one = one;
		this.two = two;
	}

	/**
	 * evaluates to "(" + first() + "," + second() + ")"
	 */
	@Override
	public String toString() {
		return "(" + one + "," + two + ")";
	}

	public First first() {
		return one;
	}

	public Second second() {
		return two;
	}

	/**
	 * A pair equals another pair when their respective parts equal one another.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		
		if(o == this)
			return true;
		
		if (o instanceof Pair) {
			Pair<First, Second> p = (Pair<First, Second>) o;
			return p.one.equals(one) && p.two.equals(two);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return one.hashCode() * 47 + two.hashCode();
	}
	
	/**
	 * Easy access constructor
	 */
	public static <First, Second> Pair<First, Second> create(First first, Second second) {
		return new Pair<First, Second>(first, second);
	}
	
	/**
	 * Wrap a comparator such to create a new comparator that operates on the first() component of a Pair
	 */
	public static <First, Second> Comparator<Pair<First, Second>> compareFirst(final Comparator<First> comp) {
		return new Comparator<Pair<First,Second>>() {
			public int compare(Pair<First, Second> o1, Pair<First, Second> o2) {
				return comp.compare(o1.first(), o2.first());
			}
		};
	}
	
	/**
	 * Wrap a comparator such to create a new comparator that operates on the second() component of a Pair
	 */
	public static <First, Second> Comparator<Pair<First, Second>> compareSecond(final Comparator<Second> comp) {
		return new Comparator<Pair<First,Second>>() {
			public int compare(Pair<First, Second> o1, Pair<First, Second> o2) {
				return comp.compare(o1.second(), o2.second());
			}
		};
	}
}