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
package nl.queuemanager.ui;

import nl.queuemanager.jms.JMSDestination;

import java.io.Serializable;
import java.util.Comparator;

@SuppressWarnings("serial")
public class JMSDestinationComparator implements Comparator<JMSDestination>, Serializable {
	public int compare(final JMSDestination d1, final JMSDestination d2) {
		return d1.getName().compareToIgnoreCase(d2.getName());
	}
}
