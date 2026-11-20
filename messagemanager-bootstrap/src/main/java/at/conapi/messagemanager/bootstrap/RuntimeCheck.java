package at.conapi.messagemanager.bootstrap;
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

/**
 * The activation check of a downloaded Java runtime: started by {@link RuntimeStep} with the new runtime's own
 * {@code java}, with the proxy and TLS settings of the running application, it reads the update site's setup.xml.
 * Exit 0 when it could; otherwise exit 1 and one line on standard output that says why.
 * <p>
 * It runs on the class path, alone: it uses nothing but java.base.
 */
public final class RuntimeCheck {

    private RuntimeCheck() {
    }

    public static void main(String[] args) {
        try {
            java.net.URLConnection connection = java.net.URI.create(args[0]).toURL().openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(30 * 1000);
            connection.setReadTimeout(30 * 1000);
            String setup;
            try (java.io.InputStream in = connection.getInputStream()) {
                setup = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            if (!setup.contains("<configuration")) {
                System.out.println("not an update configuration: " + args[0]);
                System.exit(1);
            }
            System.out.println("read " + args[0] + " on Java " + Runtime.version());
            System.exit(0);
        } catch (Throwable t) {
            System.out.println(t);
            System.exit(1);
        }
    }
}
