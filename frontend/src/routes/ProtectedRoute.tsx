import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";
import { PermissionPanel, StateBanner } from "../components/State";
export function ProtectedRoute({
  children,
  role = "R09",
}: {
  children: JSX.Element;
  role?: string;
}) {
  const { user, loading, hasRole } = useAuth();
  if (loading) return <StateBanner type="loading" message="세션 확인 중" />;
  if (!user) return <Navigate to="/login" replace />;
  if (!hasRole(role)) return <PermissionPanel operationId="protectedRoute" />;
  return children;
}
