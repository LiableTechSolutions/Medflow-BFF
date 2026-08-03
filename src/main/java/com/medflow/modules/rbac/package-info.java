/**
 * Access control module: roles, fine-grained permissions and the portal groups that
 * decide which modules an account sees. Reference data lives here; the users module
 * stores only the role/group identifiers it was given.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Access Control")
package com.medflow.modules.rbac;
