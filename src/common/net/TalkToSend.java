package common.net;

import common.data.Agent;
import common.data.Talk;

public class TalkToSend {
	int idx;
	int day;
	int turn;
	int agent;
	String text;

	public TalkToSend(Talk talk) {
		this.idx = talk.getIdx();
		this.day = talk.getDay();
		this.turn = talk.getTurn();
		this.agent = talk.getAgent().getAgentIdx();
		this.text = talk.getText();
	}

	public int getIdx() {
		return idx;
	}

	public int getDay() {
		return day;
	}

	public int getAgent() {
		return agent;
	}

	public String getText() {
		return text;
	}

	public int getTurn() {
		return turn;
	}

	public Talk toTalk() {
		return new Talk(idx, day, turn, Agent.getAgent(agent), text);
	}
}
