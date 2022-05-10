package eu.opertusmundi.common.model.account;

import eu.opertusmundi.common.model.EnumAccountType;

/**
 * An enumeration of account attributes that may be set on the IDP side.  
 */
public enum EnumAccountAttribute {

    /**
     * Corresponds to an attribute holding a value of a {@link EnumAccountType}
     */
    ACCOUNT_TYPE("opertusmundi.eu/accountType")
    ;
    
    private String key;
    
    private EnumAccountAttribute(String key) {
        this.key = key;
    }
    
    public String key()
    {
        return this.key;
    }
}
