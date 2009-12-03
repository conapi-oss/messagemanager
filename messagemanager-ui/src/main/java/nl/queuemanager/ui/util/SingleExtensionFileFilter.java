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

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * FileFilter extension 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class SingleExtensionFileFilter extends FileFilter {
	
	private final String extension;
	private final String description;
	
	public SingleExtensionFileFilter(String extension, String description) {
		this.extension = extension;
		this.description = description;
	}
	
	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().toLowerCase().endsWith(extension);
	}

	@Override
	public String getDescription() {
		return description + " (*" + extension + ")";
	}
}
