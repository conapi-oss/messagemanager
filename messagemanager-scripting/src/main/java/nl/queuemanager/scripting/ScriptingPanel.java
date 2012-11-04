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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;


/**
 * A JPanel that can be embedded in any swing application. It provides a
 * scripting panel that can be used with any JSR 223 scripting language. The
 * Java 6 standard scripting engine is JavaScript/Rhino. For details on other
 * languages: http://scripting.dev.java.net
 * 
 * @author Kiev Gama (kiev.gama@gmail.com)
 * 
 */
public class ScriptingPanel extends JPanel {
	private Bindings bindings;
	private JTextArea jTaScript;
	private JTextArea jTaResult;
	private JButton jbtOk;
	private JButton jbtCancel;
	private JComboBox jCbLanguages;
	private JSplitPane splitPane;
	private List<ScriptContextVariable> variables;
	private Output defaultOutput = new Output() {
		public void println(String str) {
			print(str);
			print("\n");
		}

		public void print(String str) {
			jTaResult.append(str);
			jTaResult.setCaretPosition(jTaResult.getText().length());
		}
	};
	private ScriptContextVariable outputVariable = new ScriptContextVariable(
			defaultOutput, "output",
			"Available methods: print(String) and println(String)");
	private ActionListener scriptExecutorAction = new ActionListener() {
		private ScriptExecutor currentExecutor;

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == jbtOk) {
				currentExecutor = new ScriptExecutor();
				currentExecutor.execute();
			} else {
				try {
					currentExecutor.abort();
				} catch (Exception ex) {
					// TODO: handle exception
					ex.printStackTrace();
				}
			}
		}
	};

	public ScriptingPanel() {
		this(Collections.EMPTY_LIST);
	}
	
	public ScriptingPanel(ScriptContextVariable ... variables) {
		this(Arrays.asList(variables));
	}

	public ScriptingPanel(List<ScriptContextVariable> variables) {
		initBindings(variables);
		this.initGUI();
		this.printVariables();
	}

	private void initBindings(List<ScriptContextVariable> paramVars) {
		this.variables = new ArrayList<ScriptContextVariable>();
		this.bindings = new SimpleBindings();
		this.variables.addAll(paramVars);
		this.variables.add(outputVariable);
		for (ScriptContextVariable variable : variables) {
			if (variable != null) {
				this.bindings.put(variable.getName(), variable.getVar());
			}
		}
	}

	public void setOutput(Output output) {
		this.bindings.remove("output");
		this.bindings.put("output", output);
	}

	private void printVariables() {
		this.jTaResult.append("*SCRIPTING VARIABLES*\n");
		for (ScriptContextVariable variable : variables) {
			if (variable == null) continue;
			this.jTaResult.append(variable.getName());
			this.jTaResult.append(" - ");
			String descr = variable.getDescription();
			if (descr == null || descr.trim().equals("")) {
				this.jTaResult.append("class ");
				this.jTaResult.append(variable.getVar().getClass().getName());
			} else {
				this.jTaResult.append(descr);
			}
			this.jTaResult.append("\n");
		}
	}

	private void initGUI() {
		JPanel lowerPanel = new JPanel();
		JPanel upperPanel = new JPanel();
		JButton jbtClear = new JButton("Clear");
		JButton jbtVariables = new JButton("Variables");
		this.jCbLanguages = new JComboBox();
		this.jbtOk = new JButton("Run");
		this.jbtCancel = new JButton("Cancel");
		this.jTaScript = new JTextArea();
		this.jTaResult = new JTextArea();
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.setLayout(new BorderLayout());
		JScrollPane scriptScroll = new JScrollPane(jTaScript);
		this.splitPane.add(scriptScroll);
		scriptScroll.setPreferredSize(new Dimension(300, 150));
		scriptScroll
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane resultScroll = new JScrollPane(jTaResult);
		this.splitPane.add(resultScroll);
		resultScroll.setBorder(new TitledBorder("Output"));
		this.jTaResult.setEditable(false);
		this.jCbLanguages.setRenderer(new ScriptLanguageCellRenderer());
		this.jTaResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
		this.jTaScript.setFont(this.jTaResult.getFont());
		upperPanel.add(new JLabel("Scripting language:"));
		upperPanel.add(jCbLanguages);
		lowerPanel.add(jbtClear);
		lowerPanel.add(jbtVariables);
		lowerPanel.add(jbtOk);
		lowerPanel.add(jbtCancel);
		this.jbtCancel.setEnabled(false);
		this.jbtOk.setMnemonic('R');
		jbtClear.setMnemonic('C');
		jbtVariables.setMnemonic('V');
		this.jbtOk.setMnemonic('R');
		this.jbtCancel.setMnemonic('n');
		this.jbtCancel.setToolTipText("Tries to cancel executing script");
		jbtClear.setToolTipText("Clears output");
		jbtVariables.setToolTipText("Prints available scripting context variables");
		this.jbtOk.setToolTipText("Runs the script");
		
		this.jbtOk.addActionListener(scriptExecutorAction);
		this.jbtCancel.addActionListener(scriptExecutorAction);
		jbtClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jTaResult.setText("");
			}
		});
		jbtVariables.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printVariables();
			}
		});
		this.loadComboBoxModel();
		this.add(upperPanel, BorderLayout.NORTH);
		this.add(splitPane, BorderLayout.CENTER);
		this.add(lowerPanel, BorderLayout.SOUTH);
	}

	private void loadComboBoxModel() {
		ScriptEngineManager manager = new ScriptEngineManager();
		Vector<ScriptEngineFactory> list = new Vector<ScriptEngineFactory>();
		for (ScriptEngineFactory factory : manager.getEngineFactories()) {
			list.add(factory);
		}
		this.jCbLanguages.setModel(new DefaultComboBoxModel(list));
	}

	class ScriptExecutor extends SwingWorker<Object, Object> {
		public ScriptExecutor() {
			jbtOk.setEnabled(false);
			jbtCancel.setEnabled(true);
		}

		@Override
		protected Object doInBackground() throws Exception {
			ScriptEngine engine = resolveEngine();
			try {
				if (engine != null) {
					engine.eval(jTaScript.getText());
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(ScriptingPanel.this,
						"Error executing script:\n " + ex.getMessage());
			}
			return new Object();
		}

		private ScriptEngine resolveEngine() {
			ScriptEngine engine = null;
			try {
				//the engine will execute the script using the bundle's classloader for class resolving
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				
				ScriptEngineFactory factory = ((ScriptEngineFactory) jCbLanguages
						.getSelectedItem());
				engine = factory.getScriptEngine();
				engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
			} catch (Throwable ex) {
				JOptionPane.showMessageDialog(ScriptingPanel.this,
						"Error retrieving script engine:\n " + ex.getMessage());
			}
			return engine;
		}

		// FIXME This has proven to be effective only when we use Thread.sleep
		// inside script the calls
		void abort() {
			cancel(true);
			restoreButtonsState();
		}

		@Override
		protected void done() {
			restoreButtonsState();
		}

		private void restoreButtonsState() {
			jbtOk.setEnabled(true);
			jbtCancel.setEnabled(false);
		}
	}
}