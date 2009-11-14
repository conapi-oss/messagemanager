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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.PairTableModel;

import org.junit.Before;
import org.junit.Test;

public class TestPairTableModel {

	private PairTableModel<String, Integer> model;
	
	@Before
	public void setup() {
		ArrayList<Pair<String, Integer>> data = CollectionFactory.newArrayList();
		model = new PairTableModel<String, Integer>();
		model.setColumnNames(new String[] {"Column 1", "Column 2"});
		model.setColumnTypes(new Class[] {String.class, Integer.class});
		model.setData(data);
	}
	
	@Test
	public void testAddRow() {
		model.addRow(Pair.create("Some string", 1));
		assertEquals(1, model.getRowCount());
	}

	@Test
	public void testRemoveRow() {
		model.addRow(Pair.create("Some string", 1));
		assertEquals(1, model.getRowCount());
		
		model.removeRow(Pair.create("Some string", 1));
		assertEquals(0, model.getRowCount());
	}

	@Test
	public void testSetColumnNames() {
		model.setColumnNames(new String[] {"Column 1", "Column 2"});
		assertEquals("Column 1", model.getColumnName(0));
		assertEquals("Column 2", model.getColumnName(1));
	}

	@Test
	public void testSetColumnTypes() {
		model.setColumnTypes(new Class[] {String.class, Integer.class});
		assertEquals(String.class, model.getColumnClass(0));
		assertEquals(Integer.class, model.getColumnClass(1));
	}

	@Test
	public void testGetColumnCount() {
		model.setColumnNames(new String[] {"1", "2"});
		assertEquals(2, model.getColumnCount());
	}

	@Test
	public void testGetRowItem() {
		Pair<String, Integer> p1;
		Pair<String, Integer> p2;
		Pair<String, Integer> p3;
		Pair<String, Integer> p4;
		
		model.addRow(p1 = Pair.create("String 1", 1));
		model.addRow(p2 = Pair.create("String 2", 2));
		model.addRow(p3 = Pair.create("String 3", 3));
		model.addRow(p4 = Pair.create("String 4", 4));
		
		assertEquals(p1, model.getRowItem(0));
		assertEquals(p2, model.getRowItem(1));
		assertEquals(p3, model.getRowItem(2));
		assertEquals(p4, model.getRowItem(3));
	}

	@Test
	public void testSetRowItem() {
		Pair<String, Integer> p1 = Pair.create("String 1", 1);
		Pair<String, Integer> p2 = Pair.create("String 2", 2);
		Pair<String, Integer> p3 = Pair.create("String 3", 3);
		Pair<String, Integer> p4 = Pair.create("String 4", 4);
		
		model.addRow(p1);
		model.addRow(p2);
		model.addRow(p3);

		assertEquals(3, model.getRowCount());
		assertEquals(p1, model.getRowItem(0));
		assertEquals(p2, model.getRowItem(1));
		assertEquals(p3, model.getRowItem(2));
		
		model.setRowItem(2, p4);
		
		assertEquals(3, model.getRowCount());
		assertEquals(p1, model.getRowItem(0));
		assertEquals(p2, model.getRowItem(1));
		assertEquals(p4, model.getRowItem(2));
	}

	@Test
	public void testGetItemRow() {
		Pair<String, Integer> p1 = Pair.create("String 1", 1);
		Pair<String, Integer> p2 = Pair.create("String 2", 2);
		Pair<String, Integer> p3 = Pair.create("String 3", 3);
		
		model.addRow(p1);
		model.addRow(p2);
		model.addRow(p3);
		
		assertEquals(0, model.getItemRow(p1));
		assertEquals(1, model.getItemRow(p2));
		assertEquals(2, model.getItemRow(p3));
	}

	@Test
	public void testGetValueAt() {
		Pair<String, Integer> p1 = Pair.create("String 1", 1);
		Pair<String, Integer> p2 = Pair.create("String 2", 2);
		Pair<String, Integer> p3 = Pair.create("String 3", 3);
		
		model.addRow(p1);
		model.addRow(p2);
		model.addRow(p3);
		
		assertEquals("String 1", model.getValueAt(0, 0));
		assertEquals("String 2", model.getValueAt(1, 0));
		assertEquals("String 3", model.getValueAt(2, 0));
		assertEquals(1, model.getValueAt(0, 1));
		assertEquals(2, model.getValueAt(1, 1));
		assertEquals(3, model.getValueAt(2, 1));
	}

	@Test
	public void testGetColumnValue() {
		Pair<String, Integer> p1 = Pair.create("String 1", 1);
		Pair<String, Integer> p2 = Pair.create("String 2", 2);
		Pair<String, Integer> p3 = Pair.create("String 3", 3);
		
		assertEquals("String 1", model.getColumnValue(p1, 0));
		assertEquals("String 2", model.getColumnValue(p2, 0));
		assertEquals("String 3", model.getColumnValue(p3, 0));
		assertEquals(1, model.getColumnValue(p1, 1));
		assertEquals(2, model.getColumnValue(p2, 1));
		assertEquals(3, model.getColumnValue(p3, 1));		
	}
}
