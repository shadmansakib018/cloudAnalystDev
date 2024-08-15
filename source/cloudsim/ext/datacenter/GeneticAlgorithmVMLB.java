package cloudsim.ext.datacenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cloudsim.VMCharacteristics;
import cloudsim.VirtualMachine;
import cloudsim.VirtualMachineList;
import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;

/**
 * Using GA we are going to map the internet couldlets to a specific VM within a Data Center according to the best fitness function
 * given by a population of Vms
 * @author Shadman Sakib
 *
 */
public class GeneticAlgorithmVMLB extends VmLoadBalancer implements CloudSimEventListener {
	/** Holds the count current active allcoations on each VM */
	private Map<Integer, Integer> currentAllocationCounts;
	private Map<Integer, VirtualMachineState> vmStatesList;
	private Integer lastVmID = -1;
	
	public GeneticAlgorithmVMLB(DatacenterController dcb){
		dcb.addCloudSimEventListener(this);
		this.vmStatesList = dcb.getVmStatesList();
		this.currentAllocationCounts = Collections.synchronizedMap(new HashMap<Integer, Integer>());
		
	}

	/**
	 * @return The VM id of a VM so that the number of active tasks on each VM is kept
	 * 			evenly distributed among the VMs.
	 */
	@Override
	public int getNextAvailableVm(){
		
		int vmId = -1;
		System.out.println("current allocated VM's: "+currentAllocationCounts.size());
		
//		for (int thisVmId : currentAllocationCounts.keySet()){
//			System.out.println(thisVmId + "======> " + currentAllocationCounts.get(thisVmId));
//		}
		
		//Find the vm with least number of allocations
		
		//If all available vms are not allocated, allocated the new ones
		if (currentAllocationCounts.size() < vmStatesList.size()){			
			for (int availableVmId : vmStatesList.keySet()){
				if (!currentAllocationCounts.containsKey(availableVmId) && lastVmID != availableVmId){
					vmId = availableVmId;
					lastVmID = availableVmId;
					break;
				}				
			}
		} else {
			int currCount;
			int minCount = Integer.MAX_VALUE;
			
			for (int thisVmId : currentAllocationCounts.keySet()){
				if(lastVmID != thisVmId) {
				currCount = currentAllocationCounts.get(thisVmId);
				if (currCount < minCount){
					minCount = currCount;
					vmId = thisVmId;
					lastVmID = thisVmId;
				}
				}
			}
		}
//		if(vmId == -1) {
//			vmId = (lastVmID+1)%vmStatesList.size();
//			lastVmID = vmId;
//			System.out.println("in round robin: ");
//		}
//		System.out.println("Allocated VmID: " + vmId);
		allocatedVm(vmId);
		
		Integer currCount = currentAllocationCounts.remove(vmId);
		if (currCount == null){
			currCount = 1;
		} else {
			currCount++;
		}
		
		currentAllocationCounts.put(vmId, currCount);
		
		return vmId;
		
	}
	
	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			
			Integer currCount = currentAllocationCounts.remove(vmId);
			if (currCount == null){
				currCount = 1;
			} else {
				currCount++;
			}
			
			currentAllocationCounts.put(vmId, currCount);
			
		} 
		else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
//			System.out.println("this vmid is now free====> "+vmId);
			Integer currCount = currentAllocationCounts.remove(vmId);
			if (currCount != null){
				currCount--;
				currentAllocationCounts.put(vmId, currCount);
			}
		}
	}

}
