package lanSimulation.internals;

public class Workstation extends Node{

	public Workstation(byte type, String name) {
		super(type, name);
	}
	
	public Workstation(byte type, String name, Node nextNode) {
		super(type, name, nextNode);
	}

}
