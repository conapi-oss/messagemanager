/*
 * Copyright [2008-2009] [Kiev Gama - kiev.gama@gmail.com]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package nl.queuemanager.scripting;

/**
 * A class that holds a variable (and its metadata) that can be available
 * through the scripting panel.
 * 
 * @author Kiev Gama (kiev.gama@gmail.com)
 * 
 */
public class ScriptContextVariable {
	private Object var;
	private String name;
	private String description;

	/**
	 * Constructs a new context object
	 * 
	 * @param var
	 *            The actual variable the will be called
	 * @param name
	 *            The name that will refere to the variable in the scripting
	 *            panel
	 * @param description
	 *            An informative text describing the variable
	 */
	public ScriptContextVariable(Object var, String name, String description) {
		if (var == null) {
			throw new RuntimeException("The variable must not be null");
		}
		if (name == null || name.trim().equals("")) {
			throw new RuntimeException("The variable name is mandatory");
		}
		this.var = var;
		this.name = name;
		this.description = description;
	}

	public Object getVar() {
		return var;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}