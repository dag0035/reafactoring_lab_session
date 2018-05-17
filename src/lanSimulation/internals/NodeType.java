package lanSimulation.internals;

public class NodeType {

	//enumeration constants specifying all legal node types
	/**
	A node with type NODE has only basic functionality.
	 */
	public static final byte NODE = 0;
	/**
	A node with type WORKSTATION may initiate requests on the LAN.
	 */
	public static final byte WORKSTATION = 1;
	/**
	A node with type PRINTER may accept packages to be printed.
	 */
	public static final byte PRINTER = 2;

	
	public static byte createNodeType(byte type) {
		switch (type) {
		case 0:
			return NodeType.NODE;
		case 1:
			return NodeType.WORKSTATION;
		case 2:
			return NodeType.PRINTER;
		default:
			break;
		}
		return type;
	}
	
}
