import React, { createContext, useContext, useEffect, useState } from "react";
import { CurrentUser, me } from "./authApi";

type Auth = {
  user: CurrentUser | null;
  loading: boolean;
  setUser: (u: CurrentUser | null) => void;
  hasRole: (r: string) => boolean;
};
const AuthContext = createContext<Auth | null>(null);
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    me()
      .then((r) => {
        if (r.success && r.data) setUser(r.data);
      })
      .finally(() => setLoading(false));
  }, []);
  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        setUser,
        hasRole: (r) => !!user?.roles?.includes(r),
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => {
  const v = useContext(AuthContext);
  if (!v) throw new Error("AuthProvider missing");
  return v;
};
