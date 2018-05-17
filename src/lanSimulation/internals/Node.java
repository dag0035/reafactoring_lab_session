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
 *   Foundation, Inc. 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   Copyright original Java version: 2004 Bart Du Bois, Serge Demeyer
 *   Copyright C++ version: 2006 Matthias Rieger, Bart Van Rompaey
 */
package lanSimulation.internals;

import java.io.IOException;
import java.io.Writer;

/**
A <em>Node</em> represents a single Node in a Local Area Network (LAN).
Several types of Nodes exist.
 */
public class Node {
	/**
    Holds the type of the Node.
	 */
	private byte type_;
	/**
    Holds the name of the Node.
	 */
	public String name_;
	/**
    Holds the next Node in the token ring architecture.
    @see lanSimulation.internals.Node
	 */
	public Node nextNode_;

	/**
Construct a <em>Node</em> with given #type and #name.
<p><strong>Precondition:</strong> (type >= NODE) & (type <= PRINTER);</p>
	 */
	public Node(byte type, String name) {
		assert (type >= NodeType.NODE) & (type <= NodeType.PRINTER);
		setType_(type);
		name_ = name;
		nextNode_ = null;
	}

	/**
Construct a <em>Node</em> with given #type and #name, and which is linked to #nextNode.
<p><strong>Precondition:</strong> (type >= NODE) & (type <= PRINTER);</p>
	 */
	public Node(byte type, String name, Node nextNode) {
		assert (type >= NodeType.NODE) & (type <= NodeType.PRINTER);
		setType_(type);
		name_ = name;
		nextNode_ = nextNode;
	}

	public void loggingNode(Writer report, String msg) throws IOException {
		report.write("\tNode '");
		report.write(name_);
		report.write("' " + msg + "\n");
		report.flush();
	}

	public void logging(Writer report, String author, String title) throws IOException {
		report.write("\tAccounting -- author = '");
		report.write(author);
		report.write("' -- title = '");
		report.write(title);
		report.write("'\n");
	}

	public void appendCase(StringBuffer buf) {
		switch (getType_()) {
		case NodeType.NODE:
			buf.append("Node ");
			buf.append(name_);
			buf.append(" [Node]");
			break;
		case NodeType.WORKSTATION:
			buf.append("Workstation ");
			buf.append(name_);
			buf.append(" [Workstation]");
			break;
		case NodeType.PRINTER:
			buf.append("Printer ");
			buf.append(name_);
			buf.append(" [Printer]");
			break;
		default:
			buf.append("(Unexpected)");;
			break;
		};
	}

	public Node getNext() {
		return this.nextNode_;
	}

	public byte getType_() {
		return type_;
	}

	public void setType_(byte type) {
		
		this.type_ = NodeType.createNodeType(type);
	}

}