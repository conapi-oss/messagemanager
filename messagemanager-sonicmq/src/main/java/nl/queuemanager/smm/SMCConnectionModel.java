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
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.sonicsw.ma.gui.PreferenceManager;
import com.sonicsw.ma.gui.domain.AgentManagerConnection;
import com.sonicsw.ma.gui.domain.DomainConnectionModel;
import com.sonicsw.mf.common.IDirectoryFileSystemService;
import com.sonicsw.mf.jmx.client.IRemoteMBeanServer;
import com.sonicsw.mf.mgmtapi.runtime.IAgentManagerProxy;
import com.sonicsw.mx.config.ConfigServerUtility;
import com.sonicsw.mx.config.IConfigServer;

public class SMCConnectionModel implements ConnectionModel {

	DomainConnectionModel delegate;
	
	public SMCConnectionModel(DomainConnectionModel delegate) {
		this.delegate = delegate;
	}

	public Object getAttribute(ObjectName objectName, String attribute) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException{
		return getMBeanServer().getAttribute(objectName, attribute);
	}
	
	public Object invoke(ObjectName objectName, String methodName, Object[] arguments, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
		return getMBeanServer().invoke(objectName, methodName, arguments, signature);
	}
	
	public void connect() throws Exception {
		delegate.connect();
	}

	public void disconnect() throws Exception {
		delegate.disconnect();
	}

	@Override
	public boolean equals(Object arg0) {
		return delegate.equals(arg0);
	}

	public IAgentManagerProxy getAgentManager() {
		return delegate.getAgentManager();
	}

	public AgentManagerConnection getAgentManagerConnection() {
		return delegate.getAgentManagerConnection();
	}

	public String getAgentManagerName() {
		return delegate.getAgentManagerName();
	}

	public IConfigServer getConfigServer() {
		return delegate.getConfigServer();
	}

	public ConfigServerUtility getConfigServerUtility() {
		return delegate.getConfigServerUtility();
	}

	public String getConnectionName() {
		return delegate.getConnectionName();
	}

	public int getConnectTimeout() {
		return delegate.getConnectTimeout();
	}

	public IDirectoryFileSystemService getDirectoryService() {
		return delegate.getDirectoryService();
	}

	public String getDirectoryServiceName() {
		return delegate.getDirectoryServiceName();
	}

	public String getDomainDescription() {
		return delegate.getDomainDescription();
	}

	public String getDomainName() {
		return delegate.getDomainName();
	}

	public String getManagementNode() {
		return delegate.getManagementNode();
	}

	public IRemoteMBeanServer getMBeanServer() {
		return delegate.getMBeanServer();
	}

//	Does not exist in 6.1	
//	public int getNotificationSubscriptionRenewalInterval() {
//		return delegate.getNotificationSubscriptionRenewalInterval();
//	}

	public int getNotificationSubscriptionTimeout() {
		return delegate.getNotificationSubscriptionTimeout();
	}

	public String getPassword() {
		return delegate.getPassword();
	}

//	Does not exist in 6.1	
//	public int getSocketConnectTimeout() {
//		return delegate.getSocketConnectTimeout();
//	}

	public int getTimeout() {
		return delegate.getTimeout();
	}

	public int getUniqueId() {
		return delegate.getUniqueId();
	}

	public String getUrl() {
		return delegate.getUrl();
	}

	public String getUserName() {
		return delegate.getUserName();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	public boolean isConnected() {
		try {
			return delegate.isConnected();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isDefault() {
		return delegate.isDefault();
	}

	public boolean isLoadBalancing() {
		return delegate.isLoadBalancing();
	}

	public boolean isUseDRA() {
		return delegate.isUseDRA();
	}

	public void onDisconnect() {
		delegate.onDisconnect();
	}

	public void onFailure(Exception arg0) {
		delegate.onFailure(arg0);
	}

	public void onFailure(String arg0, String arg1, Exception arg2, long arg3,
			long arg4) {
		delegate.onFailure(arg0, arg1, arg2, arg3, arg4);
	}

	public void onNotificationListenerRenewalFailure(Exception arg0) {
		delegate.onNotificationListenerRenewalFailure(arg0);
	}

	public void onReconnect(String arg0) {
		delegate.onReconnect(arg0);
	}

	public short onRequestFailure(String arg0, String arg1, Exception arg2,
			short[] arg3) {
		return delegate.onRequestFailure(arg0, arg1, arg2, arg3);
	}

	public void onSuccess(String arg0, String arg1, Object arg2, long arg3,
			long arg4) {
		delegate.onSuccess(arg0, arg1, arg2, arg3, arg4);
	}

	public void registerStateNotificationListener() {
		delegate.registerStateNotificationListener();
	}

	public void saveToPrefs(PreferenceManager arg0) {
		delegate.saveToPrefs(arg0);
	}

	public void setConnectionName(String arg0) {
		delegate.setConnectionName(arg0);
	}

	public void setConnectTimeout(int arg0) {
		delegate.setConnectTimeout(arg0);
	}

	public void setDefault(boolean arg0) {
		delegate.setDefault(arg0);
	}

	public void setDomainName(String arg0) {
		delegate.setDomainName(arg0);
	}

	public void setLoadBalancing(boolean arg0) {
		delegate.setLoadBalancing(arg0);
	}

	public void setManagementNode(String arg0) {
		delegate.setManagementNode(arg0);
	}

//	Does not exist in 6.1	
//	public void setNotificationSubscriptionRenewalInterval(int arg0) {
//		delegate.setNotificationSubscriptionRenewalInterval(arg0);
//	}

	public void setNotificationSubscriptionTimeout(int arg0) {
		delegate.setNotificationSubscriptionTimeout(arg0);
	}

	public void setPassword(String arg0) {
		delegate.setPassword(arg0);
	}

//	Does not exist in 6.1	
//	public void setSocketConnectTimeout(int arg0) {
//		delegate.setSocketConnectTimeout(arg0);
//	}

	public void setTimeout(int arg0) {
		delegate.setTimeout(arg0);
	}

	public void setUrl(String arg0) {
		delegate.setUrl(arg0);
	}

	public void setUseDRA(boolean arg0) {
		delegate.setUseDRA(arg0);
	}

	public void setUserName(String arg0) {
		delegate.setUserName(arg0);
	}

	@Override
	public String toString() {
		return 
			delegate.getConnectionName() + 
			" (" + delegate.getUserName() + 
			"@" + delegate.getUrl() + ")";
	}

	public void unregisterStateNotificationListener() {
		delegate.unregisterStateNotificationListener();
	}

	public DomainConnectionModel getDelegate() {
		return delegate;
	}

	public int compareTo(ConnectionModel other) {
		return getConnectionName().compareTo(other.getConnectionName());
	}	
}
