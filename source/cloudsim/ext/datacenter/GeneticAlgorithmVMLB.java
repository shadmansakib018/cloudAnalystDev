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
	private Map<Integer, VirtualMachineState> vmStatesList;
	private int currVm = -1;
	private Map<Integer, Integer> currentAllocationCounts;


	public GeneticAlgorithmVMLB(DatacenterController dcb, Map<Integer, VirtualMachineState> vmStatesList){
		super();
		System.out.println("*********hello*********");
		
		this.vmStatesList = vmStatesList;
		dcb.addCloudSimEventListener(this);
		this.currentAllocationCounts = Collections.synchronizedMap(new HashMap<Integer, Integer>());
	}

	/* (non-Javadoc)
	 * @see cloudsim.ext.VMLoadBalancer#getVM()
	 */
	public int getNextAvailableVm(){
		currVm++;
		
		if (currVm >= vmStatesList.size()){
			currVm = 0;
		}
		
		if (vmStatesList.size() > 0){
			
			int temp;
			for (Iterator<Integer> itr = vmStatesList.keySet().iterator(); itr.hasNext();){
				temp = itr.next();
//				System.out.println("temp: "+ temp + "====== currVm: " + currVm );
				if(temp < currVm) {
					continue;
				}
				VirtualMachineState state = vmStatesList.get(temp); 
//				System.out.println(temp + "===>"+state);
				if (state.equals(VirtualMachineState.AVAILABLE)){
					currVm = temp;
					break;
				}
			}
		}
//		System.out.println("Allocated VM: "+currVm);
		
		
		allocatedVm(currVm);
		
		return currVm;
		
	}
	
	public void cloudSimEventFired(CloudSimEvent e) {
//		System.out.println(GridSim.clock());
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
//			System.out.println(vmId + " is busy");
			System.out.println("getting busy VMID "+  vmId);

			vmStatesList.put(vmId, VirtualMachineState.BUSY);
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
//			System.out.println(vmId + " is available");
			vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
			System.out.println("getting available VMID "+  vmId);
//			vmStatesList.put(0, VirtualMachineState.BUSY);
		}
	}

}
