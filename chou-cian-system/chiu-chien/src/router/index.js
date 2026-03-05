import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AdminLayout from '@/layouts/AdminLayout.vue'
import UserLayout from '@/layouts/UserLayout.vue'
import AdminDashboard from '@/views/admin/DashBoard.vue'
import CreateActivity from '@/views/admin/CreateActivity.vue'
import ActivityDetail from '@/views/admin/ActivityDetail.vue'
import UserJoin from '@/views/user/Join.vue'
import UserSeatSelect from '@/views/user/SeatSelect.vue'
import Login from '@/views/auth/Login.vue'
import NotFound from '@/views/not-found/inedx.vue'

const routes = [
    {
        path: '/',
        redirect: '/admin'
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: {
            requiresAuth: false,
            redirectIfAuthenticated: true  // 新增：如果已登入則重定向
        }
    },
    {
        path: '/admin',
        component: AdminLayout,
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                name: 'AdminDashboard',
                component: AdminDashboard
            },
            {
                path: 'create',
                name: 'CreateActivity',
                component: CreateActivity
            },
            {
                path: 'activity/:id',
                name: 'ActivityDetail',
                component: ActivityDetail,
                props: true
            },
            {
                path: '/admin/activity/:id/result',
                name: 'ActivityResult',
                component: () => import('@/views/admin/ActivityResult.vue'),
                props: true
            }
        ]
    },
    {
        path: '/activity/:activityId',
        component: UserLayout,
        children: [
            {
                path: '',
                name: 'UserJoin',
                component: UserJoin,
                props: true
            },
            {
                path: 'select',
                name: 'UserSeatSelect',
                component: UserSeatSelect,
                props: true
            },
        ]
    },

    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: NotFound,
        meta: {
            requiresAuth: false
        }
    }

]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守衛
router.beforeEach((to, from, next) => {
    // 獲取用戶 store
    const userStore = useUserStore()
    const isAuthenticated = userStore.checkAuth()

    console.log('路由守衛檢查:', {
        path: to.path,
        isAuthenticated,
        requiresAuth: to.matched.some(record => record.meta.requiresAuth),
        redirectIfAuthenticated: to.matched.some(record => record.meta.redirectIfAuthenticated)
    })

    // 檢查是否需要在已登入時重定向（例如登入頁面）
    if (to.matched.some(record => record.meta.redirectIfAuthenticated)) {
        if (isAuthenticated) {
            console.log('用戶已登入，重定向到管理後台')
            next('/admin')
            return
        }
    }

    // 檢查該路由是否需要認證
    if (to.matched.some(record => record.meta.requiresAuth)) {
        // 如果需要認證且用戶未登入，跳轉到登入頁面
        if (!isAuthenticated) {
            console.log('用戶未登入，重定向到登入頁面')
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
            return
        }
    }

    next()
})

export default router
