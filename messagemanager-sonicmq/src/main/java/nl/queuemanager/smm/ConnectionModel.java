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
package nl.queuemanager.smm;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.sonicsw.mf.common.IDirectoryFileSystemService;
import com.sonicsw.mf.mgmtapi.runtime.IAgentManagerProxy;

public interface ConnectionModel extends Comparable<ConnectionModel> {

	public void connect() throws Exception;

	public void disconnect() throws Exception;

	public IAgentManagerProxy getAgentManager() throws MalformedObjectNameException, NullPointerException;

	public Object getAttribute(ObjectName objectName, String attribute) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException;
	
	public Object invoke(ObjectName objectName, String string, Object[] objects, String[] strings) 
		throws InstanceNotFoundException, MBeanException, ReflectionException;

	public IDirectoryFileSystemService getDirectoryService() throws MalformedObjectNameException, NullPointerException;

	public String getConnectionName();
	
	public String getUrl();
	
	public String getUserName();

	public String getPassword();

	public Object getDomainName();

	boolean isConnected();

}
