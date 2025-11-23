package id.stargan.intikasir.data.local.dao

import androidx.room.*
import id.stargan.intikasir.data.local.entity.RolePermissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RolePermissionDao {

    @Query("SELECT * FROM role_permissions WHERE roleId = :roleId LIMIT 1")
    fun getByRole(roleId: String): Flow<RolePermissionEntity?>

    @Query("SELECT * FROM role_permissions WHERE roleId = :roleId LIMIT 1")
    suspend fun getByRoleSuspend(roleId: String): RolePermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(permission: RolePermissionEntity)

    @Delete
    suspend fun delete(permission: RolePermissionEntity)
}
