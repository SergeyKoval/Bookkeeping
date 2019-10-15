package by.bk.security.role;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Sergey Koval
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(by.bk.entity.user.UserPermission).ADMIN.name())")
//@PreAuthorize("hasPermission(T(by.bk.entity.user.UserPermission).ADMIN, T(by.bk.entity.user.UserPermission).USER)")
public @interface RoleAdmin {
}
