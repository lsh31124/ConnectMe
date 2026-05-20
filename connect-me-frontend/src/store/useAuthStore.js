import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import * as authApi from '../api/authApi'

const useAuthStore = create(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      loginAction: async (credentials) => {
        const data = await authApi.login(credentials)
        const { accessToken, refreshToken } = data.data
        set({ accessToken, refreshToken, isAuthenticated: true })
      },

      registerAction: async (credentials) => {
        const data = await authApi.register(credentials)
        const { accessToken, refreshToken } = data.data
        set({ accessToken, refreshToken, isAuthenticated: true })
      },

      logoutAction: async () => {
        const { refreshToken } = get()
        if (refreshToken) await authApi.logout(refreshToken)
        set({ accessToken: null, refreshToken: null, isAuthenticated: false })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)

export default useAuthStore