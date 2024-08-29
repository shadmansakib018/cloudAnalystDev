package cloudsim.ext.datacenter;

import java.util.Iterator;
import java.util.Map;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import gridsim.GridSim;

/**
 * This class implements {@link VmLoadBalancer} as a Throttled load balancer. Each VM is
 * allocated only one task at a time and can be allocated another task only when the current
 * task has completed. The ThrottledVmLoadBalancer does not implement any task queueing 
 * functionality, but returns a valid VM id in the <code>getNextAvailableVm</code>
 * only if available. The calling {@link DatacenterController} should implement the task 
 * queueing locally.
 * 
 * The ThrottledVmLoadBalancer implements CloudSimEventListener to get notified of the VM's
 * being allocated and freed up by the {@link DatacenterController}
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class ThrottledVmLoadBalancer extends VmLoadBalancer implements CloudSimEventListener {
	
	private Map<Integer, VirtualMachineState> vmStatesList;
	
	/** 
	 * Constructor
	 * 
	 * @param dcb The {@link DatacenterController} using the load balancer.
	 */
	public ThrottledVmLoadBalancer(DatacenterController dcb){
		this.vmStatesList = dcb.getVmStatesList();
		dcb.addCloudSimEventListener(this);
	}

	/**
	 * @return VM id of the first available VM from the vmStatesList in the calling
	 * 			{@link DatacenterController}
	 */
	@Override
	public int getNextAvailableVm(){
		int vmId = -1;
		
		if (vmStatesList.size() > 0){
//			while(vmId == -1) {
				
			int temp;
			for (Iterator<Integer> itr = vmStatesList.keySet().iterator(); itr.hasNext();){
				temp = itr.next();
				VirtualMachineState state = vmStatesList.get(temp); 
//				System.out.println(temp + " state: " + state);
//				System.out.println(temp + " state is " + state + " total vms " + vmStatesList.size());
				if (state.equals(VirtualMachineState.AVAILABLE)){
					vmId = temp;
//					System.out.println(vmId);
					break;
				}
			}
			System.out.println("asigned vm: " + vmId);
//			}
		}
		
		allocatedVm(vmId);
		
		return vmId;
		
	}
	
	public void cloudSimEventFired(CloudSimEvent e) {
//		System.out.println(GridSim.clock());
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
//			System.out.println(vmId + " is busy");
			vmStatesList.put(vmId, VirtualMachineState.BUSY);
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
//			System.out.println(vmId + " is available");
			vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
		}
	}
	

}
