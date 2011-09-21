package org.instedd.mobilegw.messaging;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Extends message with direction (AT or AO) information
 * @author spalladino
 */
public class DirectedMessage extends Message {

	@XmlTransient
	public Direction direction;
	
	public static enum Direction {
		AT,
		AO
	}
	
	public DirectedMessage(Message message, Direction direction) {
		this.direction = direction;
		
		this.from = message.from;
		this.id = message.id;
		this.text = message.text;
		this.to = message.to;
		this.when = message.when;
		this.retries = message.retries;
		this.retryTime = message.retryTime;		
	}

	public DirectedMessage() {
	}

	public boolean isAO() {
		return direction == Direction.AO;
	}
	
	public boolean isAT() {
		return direction == Direction.AT;
	}
	
}
