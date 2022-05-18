package eu.opertusmundi.common.model.keycloak.server;

/**
 * A set of required actions that may be enforced per user account.
 * 
 * See also: https://www.keycloak.org/docs-api/15.0/javadocs/index.html?org/keycloak/models/UserModel.RequiredAction.html 
 */
public enum EnumRequiredAction
{
    UPDATE_PASSWORD,
    
    UPDATE_PROFILE,
    
    VERIFY_EMAIL;
}
