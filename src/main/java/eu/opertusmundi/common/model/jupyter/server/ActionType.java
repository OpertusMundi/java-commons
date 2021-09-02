package eu.opertusmundi.common.model.jupyter.server;

public enum ActionType {
	SPAWN("spawn"), 
	STOP("stop");
	
	private String name;
	
	private ActionType(String name) {
		this.name = name;
	}
	
	public static ActionType fromName(String s)
	{
		for (ActionType t: ActionType.values())
			if (t.name.equals(s))
				return t;
		return null;
	}
	
	public String getName() {
		return name;
	}
};