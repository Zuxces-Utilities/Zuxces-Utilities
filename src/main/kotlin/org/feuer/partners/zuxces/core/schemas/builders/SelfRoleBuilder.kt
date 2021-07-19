package org.feuer.partners.zuxces.core.schemas.builders

import net.dv8tion.jda.api.entities.Role
import org.feuer.partners.zuxces.core.schemas.SelfRoleList
import java.lang.NullPointerException

class SelfRoleBuilder {
    var name: String? = null
    var roles =  mutableListOf<String>()
    fun addRole(role: Role) {
        roles.add(role.id)
    }
    fun addRole(role: String) {
        roles.add(role)
    }
    fun build(): SelfRoleList {
        if (name.isNullOrBlank()) throw NullPointerException("Name must not be null to build self-role-list schema")
        return SelfRoleList(name!!, roles)
    }
}