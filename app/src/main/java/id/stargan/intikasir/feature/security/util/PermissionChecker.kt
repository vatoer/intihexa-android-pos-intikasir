package id.stargan.intikasir.feature.security.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import id.stargan.intikasir.feature.security.data.RolePermissionRepository
import id.stargan.intikasir.data.local.entity.RolePermissionEntity

/**
 * Lightweight helper utilities to check and observe permissions.
 * - observePermissionFlow(repo, roleId, selector): Flow<Boolean>
 * - hasPermissionSuspend(repo, roleId, selector): suspend Boolean
 * - usePermission(observeFlow): Compose helper to collect Flow<Boolean>
 */

fun observePermissionFlow(
    repository: RolePermissionRepository,
    roleId: String,
    selector: (RolePermissionEntity) -> Boolean
): Flow<Boolean> {
    return repository.getPermissionsForRole(roleId).map { it?.let(selector) ?: false }
}

suspend fun hasPermissionSuspend(
    repository: RolePermissionRepository,
    roleId: String,
    selector: (RolePermissionEntity) -> Boolean
): Boolean {
    val p = repository.getPermissionsForRoleSuspend(roleId)
    return p?.let(selector) ?: false
}

@Composable
fun usePermission(observeFlow: Flow<Boolean>): Boolean {
    val state by observeFlow.collectAsState(initial = false)
    return state
}
