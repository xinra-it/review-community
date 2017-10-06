package com.xinra.reviewcommunity.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A role is a set of {@link Permission}s. Permissions can be inherited from one or more roles.
 * Each user can have multiple roles.
 */
@Getter
@RequiredArgsConstructor
public enum Role {
  
  // New roles can be added here. Make sure to not create circular inheritance!
  
  /**
   * Default role of newly registered users.
   */
  USER(
    inheritsFrom(),
    addsPermissions(
      Permission.ADD_PRODUCT,
      Permission.ADD_REVIEW
    )
  ),
  MODERATOR(
    inheritsFrom(USER),
    addsPermissions(
      Permission.DELETE_PRODUCT
    )
  ),
  ADMIN(
    inheritsFrom(MODERATOR),
    addsPermissions(
      
    )
  );
  
  /**
   * The first role of this list that a user has determines his/her "level".
   * It is displayed in the client.
   */
  public static final ImmutableList<Role> LEVELS = ImmutableList.of(ADMIN, MODERATOR, USER);

  private static ImmutableSet<Role> inheritsFrom(Role... parents) {
    return ImmutableSet.copyOf(parents);
  }
  
  private static ImmutableSet<Permission> addsPermissions(Permission... permissions) {
    return ImmutableSet.copyOf(permissions);
  }
  
  private final ImmutableSet<Role> parents;
  private final ImmutableSet<Permission> permissions;
  private ImmutableSet<Role> transitiveRoles;
  private ImmutableSet<Permission> transitivePermissions;
  
  /**
   * Returns an enum set of this role as well of all roles that are inherited.
   */
  public ImmutableSet<Role> getTransitiveRoles() {
    if (transitiveRoles == null) {
      Set<Role> roles = new HashSet<>();
      roles.add(this);
      // this is infinite recursion if there is a circular inheritance!
      parents.forEach(p -> roles.addAll(p.getTransitiveRoles()));
      transitiveRoles = Sets.immutableEnumSet(roles);
    }
    return transitiveRoles;
  }
  
  /**
   * Returns an enum set of all permissions that are granted to this role.
   * This includes inherited permissions.
   */
  public ImmutableSet<Permission> getTransitivePermissions() {
    if (transitivePermissions == null) {
      Set<Permission> roles = new HashSet<>();
      roles.addAll(permissions);
      parents.forEach(p -> roles.addAll(p.getTransitivePermissions()));
      transitivePermissions = Sets.immutableEnumSet(roles);
    }
    return transitivePermissions;
  }
  
  /**
   * Prefixes constant names with "ROLE_".
   */
  @Override
  public String toString() {
    return "ROLE_" + name();
  }
  
}
