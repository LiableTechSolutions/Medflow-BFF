/**
 * Users module: staff and portal accounts, their role, portal group and lifecycle. Owns
 * the {@code users} table; resolves role and group identifiers through the access-control
 * module and writes administrative changes to the audit trail.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Users")
package com.medflow.modules.users;
