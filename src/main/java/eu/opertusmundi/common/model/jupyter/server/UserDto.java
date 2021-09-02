package eu.opertusmundi.common.model.jupyter.server;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto 
{	
	public static final String DEFAULT_SERVER_NAME = "";
	
	private String name;
	
	@JsonProperty("admin")
	private boolean isAdmin;
	
	@JsonProperty("groups")
	private List<String> groups;
	
	@JsonIgnore
	private ActionType pendingAction;
	
	@JsonSetter("pending")
	public void setPendingActionFromString(String name)
	{
		this.pendingAction = ActionType.fromName(name);
	}
	
	@JsonProperty("last_activity")
	private ZonedDateTime lastActivityAt;
	
	private Map<String, ServerDto> servers;

	public Optional<ServerDto> getServerForDefaultName()
	{
		if (servers == null)
			return Optional.empty();
		else
			return Optional.ofNullable(servers.get(DEFAULT_SERVER_NAME));
	}
}
