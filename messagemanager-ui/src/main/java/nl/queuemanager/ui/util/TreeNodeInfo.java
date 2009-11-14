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
package nl.queuemanager.ui.util;

public class TreeNodeInfo {
	private final String title;
	private       Object data;
	private final Object ref;
	
	public TreeNodeInfo(final String title, final Object data) {
		this.title = title;
		this.data = data;
		this.ref = null;
	}
	
	public TreeNodeInfo(final String title, final Object data, final Object ref) {
		this.title = title;
		this.data = data;
		this.ref = ref;
	}

	public Object getRef() {
		return ref;
	}
	
	public void setData(Object data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}
