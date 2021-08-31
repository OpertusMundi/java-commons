package eu.opertusmundi.common.model.jupyter;

import java.util.List;

import org.springframework.util.unit.DataSize;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class JupyterHubProfile
{   
    /**
     * Represents the amount of (compute) resources available to this profile
     */
    @lombok.ToString
    public static class Resources
    {
        @lombok.Getter
        private long memoryLimit;
        
        public void setMemoryLimit(String s)
        {
            s = s.trim();
            if (s.endsWith("K") || s.endsWith("M") || s.endsWith("G")) {
                s = s.concat("B"); // because DataSize.parse expects units as KB, MB, GB, ...
            }
            this.memoryLimit = DataSize.parse(s).toBytes();
        }
        
        @lombok.Getter
        @lombok.Setter
        private float cpuLimit;
    }
    
    /**
     * A human-friendly name for this profile
     */
    private String name;
    
    private String description;
    
    private Resources resources;
    
    /**
     * The names of (JupyterHub) user groups that are allowed to use this profile
     */
    private List<String> groups;
}
