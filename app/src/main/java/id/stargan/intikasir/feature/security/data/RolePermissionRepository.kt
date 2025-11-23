package id.stargan.intikasir.feature.security.data

import id.stargan.intikasir.data.local.dao.RolePermissionDao
import id.stargan.intikasir.data.local.entity.RolePermissionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface RolePermissionRepository {
    fun getPermissionsForRole(roleId: String): Flow<RolePermissionEntity?>
    suspend fun getPermissionsForRoleSuspend(roleId: String): RolePermissionEntity?
    suspend fun upsert(permission: RolePermissionEntity)
}

@Singleton
class RolePermissionRepositoryImpl @Inject constructor(
    private val dao: RolePermissionDao
) : RolePermissionRepository {
    override fun getPermissionsForRole(roleId: String): Flow<RolePermissionEntity?> = dao.getByRole(roleId)
    override suspend fun getPermissionsForRoleSuspend(roleId: String): RolePermissionEntity? = dao.getByRoleSuspend(roleId)
    override suspend fun upsert(permission: RolePermissionEntity) = dao.upsert(permission)
}

