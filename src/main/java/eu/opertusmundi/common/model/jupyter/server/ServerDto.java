package eu.opertusmundi.common.model.jupyter.server;

import java.net.URL;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerDto 
{
	private String name;
	
	private boolean ready;
	
	@JsonIgnore
	private ActionType pendingAction;
	
	@JsonSetter("pending")
	public void setPendingActionFromString(String name)
	{
		this.pendingAction = ActionType.fromName(name);
	}
	
	/**
	 * A URL path (relative to JupyterHub root URL) for the user's server
	 */
	@JsonProperty("url")
	private String urlPath;
	
	@JsonProperty("started")
	private ZonedDateTime startedAt;
	
	@JsonProperty("last_activity")
	private ZonedDateTime lastActivityAt;
}
