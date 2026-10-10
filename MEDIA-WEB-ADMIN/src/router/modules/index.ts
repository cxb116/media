import { AppRouteRecord } from '@/types/router'
import { userCenterRoute } from './system'
import { mediaAppRoutes, mediaSlotRoutes, mediaDataRoutes } from './media'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [
  mediaAppRoutes,
  mediaSlotRoutes,
  mediaDataRoutes,
  userCenterRoute
]
