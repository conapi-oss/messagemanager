package nl.queuemanager.activemq.incompat;

import java.util.ArrayList;
import java.util.List;

import nl.queuemanager.activemq.ui.JavaProcessDescriptor;
import nl.queuemanager.activemq.ui.JavaProcessFinder;

import com.sun.tools.attach.VirtualMachineDescriptor;

public class VirtualMachineProcessFinder implements JavaProcessFinder {

	@Override
	@SuppressWarnings("restriction")
	public List<JavaProcessDescriptor> find() {
		List<JavaProcessDescriptor> ret = new ArrayList<JavaProcessDescriptor>();
		
		List<VirtualMachineDescriptor> vms = com.sun.tools.attach.VirtualMachine.list();
        for(VirtualMachineDescriptor vm : vms) {
        	ret.add(new VirtualMachineDescriptorWrapper(vm));
        }
        
        return ret;
	}

	@Override
	public boolean isSupported() {
		return true;
	}
	
}
