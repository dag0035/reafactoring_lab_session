/*   This file is part of lanSimulation.
 *
 *   lanSimulation is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   lanSimulation is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with lanSimulation; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   Copyright original Java version: 2004 Bart Du Bois, Serge Demeyer
 *   Copyright C++ version: 2006 Matthias Rieger, Bart Van Rompaey
 */
package lanSimulation;

import lanSimulation.internals.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

/**
 * A <em>Network</em> represents the basic data stucture for simulating a Local
 * Area Network (LAN). The LAN network architecture is a token ring, implying
 * that packahes will be passed from one node to another, until they reached
 * their destination, or until they travelled the whole token ring.
 */
public class Network {
	/**
	 * Holds a pointer to myself. Used to verify whether I am properly initialized.
	 */
	private Network initPtr_;
	/**
	 * Holds a pointer to some "first" node in the token ring. Used to ensure that
	 * various printing operations return expected behaviour.
	 */
	public Node firstNode_;
	/**
	 * Maps the names of workstations on the actual workstations. Used to initiate
	 * the requests for the network.
	 */
	private Hashtable<String, Node> workstations_;

	/**
	 * Construct a <em>Network</em> suitable for holding #size Workstations.
	 * <p>
	 * <strong>Postcondition:</strong>(result.isInitialized()) & (!
	 * result.consistentNetwork());
	 * </p>
	 */
	public Network(int size) {
		assert size > 0;
		initPtr_ = this;
		firstNode_ = null;
		workstations_ = new Hashtable<String, Node>(size, 1.0f);
		assert isInitialized();
		assert !consistentNetwork();
	}

	/**
	 * Return a <em>Network</em> that may serve as starting point for various
	 * experiments. Currently, the network looks as follows.
	 * 
	 * <pre>
	 Workstation Filip [Workstation] -> Node -> Workstation Hans [Workstation]
	 -> Printer Andy [Printer] -> ...
	 * </pre>
	 * <p>
	 * <strong>Postcondition:</strong>result.isInitialized() &
	 * result.consistentNetwork();
	 * </p>
	 */
	public static Network DefaultExample() {
		Network network = new Network(2);

		Node wsFilip = new Node(NodeType.WORKSTATION, "Filip");
		Node n1 = new Node(NodeType.NODE, "n1");
		Node wsHans = new Node(NodeType.WORKSTATION, "Hans");
		Node prAndy = new Node(NodeType.PRINTER, "Andy");

		wsFilip.nextNode_ = n1;
		n1.nextNode_ = wsHans;
		wsHans.nextNode_ = prAndy;
		prAndy.nextNode_ = wsFilip;

		network.workstations_.put(wsFilip.name_, wsFilip);
		network.workstations_.put(wsHans.name_, wsHans);
		network.firstNode_ = wsFilip;

		assert network.isInitialized();
		assert network.consistentNetwork();
		return network;
	}

	/**
	 * Answer whether #receiver is properly initialized.
	 */
	public boolean isInitialized() {
		return (initPtr_ == this);
	};

	/**
	 * Answer whether #receiver contains a workstation with the given name.
	 * <p>
	 * <strong>Precondition:</strong>this.isInitialized();
	 * </p>
	 */
	public boolean hasWorkstation(String ws) {
		// return workstations_.containsKey(ws);
		Node n;

		assert isInitialized();
		n = (Node) workstations_.get(ws);
		if (n == null) {
			return false;
		} else {
			return n.getType_() == NodeType.WORKSTATION;
		}
	};

	/**
	 * Answer whether #receiver is a consistent token ring network. A consistent
	 * token ring network - contains at least one workstation and one printer - is
	 * circular - all registered workstations are on the token ring - all
	 * workstations on the token ring are registered.
	 * <p>
	 * <strong>Precondition:</strong>this.isInitialized();
	 * </p>
	 */
	public boolean consistentNetwork() {
		assert isInitialized();
		Enumeration<Node> iter;
		Node currentNode;
		int printersFound = 0, workstationsFound = 0;
		Hashtable<String, Node> encountered = new Hashtable<String, Node>(workstations_.size() * 2, 1.0f);

		if (workstations_.isEmpty()) {
			return false;
		}

		if (firstNode_ == null) {
			return false;
		}

		// verify whether all registered workstations are indeed workstations
		iter = workstations_.elements();
		while (iter.hasMoreElements()) {
			currentNode = (Node) iter.nextElement();
			if (currentNode.getType_() != NodeType.WORKSTATION) {
				return false;
			}

		}

		// enumerate the token ring, verifying whether all workstations are registered
		// also count the number of printers and see whether the ring is circular
		currentNode = firstNode_;
		while (!encountered.containsKey(currentNode.name_)) {
			encountered.put(currentNode.name_, currentNode);
			if (currentNode.getType_() == NodeType.WORKSTATION) {
				workstationsFound++;
			}

			if (currentNode.getType_() == NodeType.PRINTER) {
				printersFound++;
			}

			currentNode = currentNode.getNext();
		}
		;
		if (currentNode != firstNode_) {
			return false;
		}
		;// not circular
		if (printersFound == 0) {
			return false;
		}
		;// does not contain a printer
		if (workstationsFound != workstations_.size()) {
			return false;
		}
		; // not all workstations are registered
			// all verifications succedeed
		return true;
	}

	/**
	 * The #receiver is requested to broadcast a message to all nodes. Therefore
	 * #receiver sends a special broadcast packet across the token ring network,
	 * which should be treated by all nodes.
	 * <p>
	 * <strong>Precondition:</strong> consistentNetwork();
	 * </p>
	 * 
	 * @param report
	 *            Stream that will hold a report about what happened when handling
	 *            the request.
	 * @return Answer #true when the broadcast operation was succesful and #false
	 *         otherwise
	 */
	public boolean requestBroadcast(Writer report) {
		assert consistentNetwork();

		boolean broadcast = true;
		
		try {
			report.write("Broadcast Request\n");
		} catch (IOException exc) {
			// just ignore
		}

		Node currentNode = firstNode_;
		Packet packet = new Packet("BROADCAST", firstNode_.name_, firstNode_.name_);
		
		currentNode = send(report, broadcast, currentNode, packet);
		
		try {
			report.write(">>> Broadcast travelled whole token ring.\n\n");
		} catch (IOException exc) {
			// just ignore
		}

		return true;
	}

	private Node send(Writer report, boolean broadcast, Node currentNode, Packet packet) {
		do {
			try {
				if ( broadcast == true ) {
				currentNode.loggingNode(report, "accepts broadcase packet.");
				}
			currentNode.loggingNode(report, "passes packet on.");
			} catch (IOException exc) {
				// just ignore
			}

			currentNode = currentNode.getNext();
		} while ((!packet.atDestination(currentNode)) & (!packet.atOrigin(currentNode)));
		return currentNode;
	}

	/**
	 * The #receiver is requested by #workstation to print #document on #printer.
	 * Therefore #receiver sends a packet across the token ring network, until
	 * either (1) #printer is reached or (2) the packet travelled complete token
	 * ring.
	 * <p>
	 * <strong>Precondition:</strong> consistentNetwork() &
	 * hasWorkstation(workstation);
	 * </p>
	 * 
	 * @param workstation
	 *            Name of the workstation requesting the service.
	 * @param document
	 *            Contents that should be printed on the printer.
	 * @param printer
	 *            Name of the printer that should receive the document.
	 * @param report
	 *            Stream that will hold a report about what happened when handling
	 *            the request.
	 * @return Answer #true when the print operation was succesful and #false
	 *         otherwise
	 */
	public boolean requestWorkstationPrintsDocument(String workstation, String document, String printer,
			Writer report) {
		assert consistentNetwork() & hasWorkstation(workstation);
		
		boolean broadcast = false;

		try {
			report.write("'");
			report.write(workstation);
			report.write("' requests printing of '");
			report.write(document);
			report.write("' on '");
			report.write(printer);
			report.write("' ...\n");
		} catch (IOException exc) {
			// just ignore
		}

		boolean result = false;
		Node currentNode;
		Packet packet = new Packet(document, workstation, printer);

		currentNode = (Node) workstations_.get(workstation);

		currentNode = send(report, broadcast, currentNode, packet);
		
		if (packet.atDestination(currentNode)) {
			result = printDocument(packet, currentNode, report);
		} else {
			try {
				report.write(">>> Destinition not found, print job cancelled.\n\n");
				report.flush();
			} catch (IOException exc) {
				// just ignore
			}
			result = false;
		}

		return result;
	}

	/**
	 * Return a printable representation of #receiver.
	 * <p>
	 * <strong>Precondition:</strong> isInitialized();
	 * </p>
	 */
	public String toString() {
		assert isInitialized();
		StringBuffer buf = new StringBuffer(30 * workstations_.size());
		printOn(firstNode_, buf);
		return buf.toString();
	}

	/**
	 * Write a printable representation of #receiver on the given #buf.
	 * <p>
	 * <strong>Precondition:</strong> isInitialized();
	 * </p>
	 * 
	 * @param node
	 *            TODO
	 * @param buf
	 *            TODO
	 */
	public void printOn(Node node, StringBuffer buf) {
		assert isInitialized();
		Node currentNode = node;
		do {
			currentNode.appendCase(buf);
			buf.append(" -> ");
			currentNode = currentNode.getNext();
		} while (currentNode != node);
		buf.append(" ... ");
	}

	/**
	 * Write a HTML representation of #receiver on the given #buf.
	 * <p>
	 * <strong>Precondition:</strong> isInitialized();
	 * </p>
	 * 
	 * @param node
	 *            TODO
	 * @param buf
	 *            TODO
	 */
	public void printHTMLOn(Node node, StringBuffer buf) {
		assert isInitialized();

		buf.append("<HTML>\n<HEAD>\n<TITLE>LAN Simulation</TITLE>\n</HEAD>\n<BODY>\n<H1>LAN SIMULATION</H1>");
		Node currentNode = node;
		buf.append("\n\n<UL>");
		do {
			buf.append("\n\t<LI> ");
			currentNode.appendCase(buf);
			buf.append(" </LI>");
			currentNode = currentNode.getNext();
		} while (currentNode != node);
		buf.append("\n\t<LI>...</LI>\n</UL>\n\n</BODY>\n</HTML>\n");
	}

	/**
	 * Write an XML representation of #receiver on the given #buf.
	 * <p>
	 * <strong>Precondition:</strong> isInitialized();
	 * </p>
	 * 
	 * @param node
	 *            TODO
	 * @param buf
	 *            TODO
	 */
	public void printXMLOn(Node node, StringBuffer buf) {
		assert isInitialized();

		Node currentNode = node;
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<network>");
		do {
			buf.append("\n\t");
			switch (currentNode.getType_()) {
			case NodeType.NODE:
				buf.append("<node>");
				buf.append(currentNode.name_);
				buf.append("</node>");
				break;
			case NodeType.WORKSTATION:
				buf.append("<workstation>");
				buf.append(currentNode.name_);
				buf.append("</workstation>");
				break;
			case NodeType.PRINTER:
				buf.append("<printer>");
				buf.append(currentNode.name_);
				buf.append("</printer>");
				break;
			default:
				buf.append("<unknown></unknown>");
				;
				break;
			}
			;
			currentNode = currentNode.getNext();
		} while (currentNode != node);
		buf.append("\n</network>");
	}

	public boolean printDocument (Packet packet, Node node, Writer report) {
		String author = "Unknown";
		String title = "Untitled";
		int startPos = 0, endPos = 0;
	
		if (node.getType_() == NodeType.PRINTER) {
			try {
				if (packet.message_.startsWith("!PS")) {
					startPos = packet.message_.indexOf("author:");
					if (startPos >= 0) {
						endPos = packet.message_.indexOf(".", startPos + 7);
						if (endPos < 0) {endPos = packet.message_.length();};
						author = packet.message_.substring(startPos + 7, endPos);};
						startPos = packet.message_.indexOf("title:");
						if (startPos >= 0) {
							endPos = packet.message_.indexOf(".", startPos + 6);
							if (endPos < 0) {endPos = packet.message_.length();};
							title = packet.message_.substring(startPos + 6, endPos);};
							firstNode_.logging(report, author, title);
							report.write(">>> Postscript job delivered.\n\n");
							report.flush();
				} else {
					title = "ASCII DOCUMENT";
					if (packet.message_.length() >= 16) {
						author = packet.message_.substring(8, 16);};
						firstNode_.logging(report, author, title);
						report.write(">>> ASCII Print job delivered.\n\n");
						report.flush();
				};
			} catch (IOException exc) {
				// just ignore
			};
			return true;
		} else {
			try {
				report.write(">>> Destinition is not a printer, print job cancelled.\n\n");
				report.flush();
			} catch (IOException exc) {
				// just ignore
			};
			return false;
		}
	}

}
