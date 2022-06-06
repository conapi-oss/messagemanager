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

import java.util.EventObject;

/**
 * Events relating to message receivers.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class MessageEvent extends EventObject {
	
	public static enum EVENT {
		MESSAGE_DISCARDED,
		MESSAGE_RECEIVED
	}
		
	private final EVENT id;
	private final Object info;
		
	public MessageEvent(EVENT id, Object info, Object source) {
		super(source);
		this.id = id;
		this.info = info;
	}

	public Object getInfo() {
		return info;
	}

	public EVENT getId() {
		return id;
	}

	@Override
	public String toString() {
		return getSource() + ": " + getId() + " (" + getInfo() + ")";
	}
}
