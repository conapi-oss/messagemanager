package nl.queuemanager.activemq.incompat;

import nl.queuemanager.activemq.ui.JavaProcessDescriptor;

import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

@SuppressWarnings("restriction")
class VirtualMachineDescriptorWrapper implements JavaProcessDescriptor {

	private final VirtualMachineDescriptor delegate;
	
	@Override
	public String displayName() {
		return delegate.displayName();
	}

	@Override
	public String id() {
		return delegate.id();
	}

	public AttachProvider provider() {
		return delegate.provider();
	}

	VirtualMachineDescriptorWrapper(VirtualMachineDescriptor vm) {
		this.delegate = vm;
	}
	
	public String toString() {
		return String.format("%s (%s)", displayName(), id());
	}
	
}
