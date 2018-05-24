package lanSimulation.internals;

public class Workstation extends Node{

	public Workstation(byte type, String name) {
		super(type, name);
	}
	
	public Workstation(byte type, String name, Node nextNode) {
		super(type, name, nextNode);
	}
	
	@Override
	public void appendElement(StringBuffer buf, Node currentNode) {
		buf.append("Workstation ");
		buf.append(currentNode.name_);
		buf.append(" [Workstation]");
	}
	
	@Override
	public void appendElementXML(StringBuffer buf, Node currentNode) {
		buf.append("<workstation>");
		buf.append(currentNode.name_);
		buf.append("</workstation>");
	}
	

}
