package nl.queuemanager.solace;

import java.nio.charset.Charset;

class SempRequests {
	
	private static final Charset charset = Charset.forName("UTF-8");

	public static byte[] showHostname() {
		return "<rpc semp-version='soltr/7_1_1'><show><hostname/></show></rpc>".getBytes(charset);
	}
	
	public static byte[] showIpVrf(String name) {
		return String.format(
				"<rpc semp-version='soltr/7_1_1'><show><ip><vrf><name>%s</name></vrf></ip></show></rpc>", 
				name).getBytes(charset);
		/*
		<rpc-reply semp-version="soltr/7_1_1">
		  <rpc>
		    <show>
		      <ip>
		        <vrf>
		          <vrf-element>
		            <vrf-name>msg-backbone</vrf-name>
		            <num-interfaces>1</num-interfaces>
		            <interfaces>
		              <intf-element>
		                <interface>1/1/lag1:3</interface>
		                <v-router>static</v-router>
		                <ip-addr>192.168.59.103/24</ip-addr>
		                <redundancy-status>not-applicable</redundancy-status>
		                <admin-status>enabled</admin-status>
		                <physical-status>disabled</physical-status>
		                <connections>0</connections>
		                <fragments>0</fragments>
		              </intf-element>
		            </interfaces>
		            <num-static-routes>0</num-static-routes>
		            <routes>
		            </routes>
		            <num-interface-static-routes>0</num-interface-static-routes>
		          </vrf-element>
		        </vrf>
		      </ip>
		    </show>
		  </rpc>
		<execute-result code="ok"/>
		</rpc-reply>
		*/
	}
	
	public static byte[] showIpVrfMsgBackbone() {
		return showIpVrf("msg-backbone");
	}
	
	public static byte[] showIpVrfManagement() {
		return showIpVrf("management");
	}

	public static byte[] showService() {
		return "<rpc semp-version='soltr/7_1_1'><show><service/></show></rpc>".getBytes(charset);
		/*
		<rpc-reply semp-version="soltr/7_1_1">
		  <rpc>
		    <show>
		      <service>
		        <services>
		          <service>
		            <name>SEMP</name>
		            <vrf>management</vrf>
		            <listen-port>8080</listen-port>
		            <enabled>true</enabled>
		            <listen-port-operational-status>Up</listen-port-operational-status>
		            <ssl>
		              <listen-port>443</listen-port>
		              <enabled>true</enabled>
		              <listen-port-operational-status>No Cert</listen-port-operational-status>
		            </ssl>
		          </service>
		          <service>
		            <name>SMF</name>
		            <vrf>msg-backbone</vrf>
		            <listen-port>55555</listen-port>
		            <enabled>true</enabled>
		            <listen-port-operational-status>Up</listen-port-operational-status>
		            <compression-listen-port>55003</compression-listen-port>
		            <compression-listen-port-operational-status>Down</compression-listen-port-operational-status>
		            <routing-control>
		              <listen-port>55556</listen-port>
		              <listen-port-operational-status>Down</listen-port-operational-status>
		            </routing-control>
		            <ssl>
		              <listen-port>55443</listen-port>
		              <listen-port-operational-status>No Cert</listen-port-operational-status>
		            </ssl>
		          </service>
		          <service>
		            <name>WEB</name>
		            <vrf>msg-backbone</vrf>
		            <listen-port>80</listen-port>
		            <enabled>true</enabled>
		            <listen-port-operational-status>Up</listen-port-operational-status>
		            <web-url-suffix></web-url-suffix>
		          </service>
		          <service>
		            <name>MQTT</name>
		            <vrf>msg-backbone</vrf>
		            <vpn-name>default</vpn-name>
		            <listen-port>1883</listen-port>
		            <enabled>true</enabled>
		            <listen-port-operational-status>Up</listen-port-operational-status>
		            <ssl>
		              <enabled>true</enabled>
		              <listen-port>8883</listen-port>
		              <listen-port-operational-status>No Cert</listen-port-operational-status>
		            </ssl>
		            <websocket>
		              <enabled>true</enabled>
		              <listen-port>8000</listen-port>
		              <listen-port-operational-status>Up</listen-port-operational-status>
		            </websocket>
		            <websocket-secure>
		              <enabled>true</enabled>
		              <listen-port>8443</listen-port>
		              <listen-port-operational-status>No Cert</listen-port-operational-status>
		            </websocket-secure>
		          </service>
		          <service>
		            <name>REST</name>
		            <vrf>msg-backbone</vrf>
		            <vpn-name>default</vpn-name>
		            <listen-port>9000</listen-port>
		            <enabled>true</enabled>
		            <listen-port-operational-status>Up</listen-port-operational-status>
		            <ssl>
		              <enabled>true</enabled>
		              <listen-port>9443</listen-port>
		              <listen-port-operational-status>No Cert</listen-port-operational-status>
		            </ssl>
		          </service>
		          <msg-backbone>Enabled</msg-backbone>
		          <rest-incoming-admin-state>Enabled</rest-incoming-admin-state>
		          <rest-outgoing-admin-state>Enabled</rest-outgoing-admin-state>
		          <mqtt-incoming-admin-state>Enabled</mqtt-incoming-admin-state>
		          <max-connections>1000</max-connections>
		          <max-connections-service-smf>1000</max-connections-service-smf>
		          <max-connections-service-web>1000</max-connections-service-web>
		          <max-connections-service-mqtt>1000</max-connections-service-mqtt>
		          <max-connections-service-rest-incoming>1000</max-connections-service-rest-incoming>
		          <max-connections-service-rest-outgoing>1000</max-connections-service-rest-outgoing>
		          <max-connections-service-ssl>1000</max-connections-service-ssl>
		          <event-configuration>
		            <event-thresholds>
		              <name>connections-service</name>
		              <set-value>800</set-value>
		              <clear-value>600</clear-value>
		              <set-percentage>80</set-percentage>
		              <clear-percentage>60</clear-percentage>
		            </event-thresholds>
		            <event-thresholds>
		              <name>connections-service-smf</name>
		              <set-value>800</set-value>
		              <clear-value>600</clear-value>
		              <set-percentage>80</set-percentage>
		              <clear-percentage>60</clear-percentage>
		            </event-thresholds>
		            <event-thresholds>
		              <name>connections-service-rest-outgoing</name>
		              <set-value>800</set-value>
		              <clear-value>600</clear-value>
		              <set-percentage>80</set-percentage>
		              <clear-percentage>60</clear-percentage>
		            </event-thresholds>
		            <event-thresholds>
		              <name>ssl-connections</name>
		              <set-value>800</set-value>
		              <clear-value>600</clear-value>
		              <set-percentage>80</set-percentage>
		              <clear-percentage>60</clear-percentage>
		            </event-thresholds>
		          </event-configuration>
		        </services>
		      </service>
		    </show>
		  </rpc>
		<execute-result code="ok"/>
		</rpc-reply>
		 */
	}
	
	public static byte[] showMessageVPN(String vpn) {
		if(vpn == null) { vpn = "*"; }
		return String.format(
			"<rpc semp-version='soltr/7_1_1'><show><message-vpn><vpn-name>%s</vpn-name></message-vpn></show></rpc>",
			vpn).getBytes(charset);
		
		/*
		<rpc-reply semp-version="soltr/8_0VMR">
		  <rpc>
		    <show>
		      <message-vpn>
		        <vpn>
		          <name>default</name>
		          <is-management-message-vpn>false</is-management-message-vpn>
		          <enabled>true</enabled>
		          <operational>true</operational>
		          <locally-configured>true</locally-configured>
		          <local-status>Up</local-status>
		          <unique-subscriptions>5</unique-subscriptions>
		          <total-local-unique-subscriptions>5</total-local-unique-subscriptions>
		          <total-remote-unique-subscriptions>0</total-remote-unique-subscriptions>
		          <total-unique-subscriptions>5</total-unique-subscriptions>
		          <connections>1</connections>
		        </vpn>
		      </message-vpn>
		    </show>
		  </rpc>
		<execute-result code="ok"/>
		</rpc-reply>
		*/
	}
	
	public static byte[] showMessageSpoolByVpn(String vpn) {
		if(vpn == null) { vpn = "*"; }
		return String.format(
			"<rpc semp-version='soltr/7_1_1'><show><message-spool><vpn-name>%s</vpn-name></message-spool></show></rpc>",
			vpn).getBytes(charset);
		/*
		<rpc-reply semp-version="soltr/8_0VMR">
		  <rpc>
		    <show>
		      <message-spool>
		        <message-vpn>
		          <vpn>
		            <name>default</name>
		            <current-messages-spooled>0</current-messages-spooled>
		            <current-spool-usage-mb>0</current-spool-usage-mb>
		            <maximum-spool-usage-mb>1500</maximum-spool-usage-mb>
		          </vpn>
		        </message-vpn>
		      </message-spool>
		    </show>
		  </rpc>
		<execute-result code="ok"/>
		</rpc-reply>
		 */
	}
	
	public static byte[] showQueues(String vpn) {
		return String.format(
			"<rpc semp-version='soltr/7_1_1'><show><queue><name>*</name><vpn-name>%s</vpn-name></queue></show></rpc>",
			vpn).getBytes(charset);
	}
	
	public static byte[] deleteMessages(String vpn, String queue) {
		return String.format(
				"<rpc semp-version='soltr/7_1_1'><admin><message-spool><vpn-name>%s</vpn-name><delete-messages><queue-name>%s</queue-name></delete-messages></message-spool></admin></rpc>",
				vpn, queue).getBytes(charset);
	}
	
	public static byte[] showSession() {
		return "<rpc semp-version='soltr/7_1_1'><show><session/></show></rpc>".getBytes(charset);
	}
}


