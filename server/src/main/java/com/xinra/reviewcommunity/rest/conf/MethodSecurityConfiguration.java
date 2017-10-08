package com.xinra.reviewcommunity.rest.conf;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.annotation.SecuredAnnotationSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@EnableGlobalMethodSecurity(securedEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
  
  @Override
  public MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
    return new SecuredAnnotationSecurityMetadataSource(new RequiresPermission.MetadataExtractor());
  }
  
  @Override
  protected AccessDecisionManager accessDecisionManager() {
    AccessDecisionManager manager = super.accessDecisionManager();
    ((AffirmativeBased) manager).getDecisionVoters().add(new PermissionVoter());
    return manager;
  }

}
