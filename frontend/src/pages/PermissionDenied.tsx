export function PermissionDenied() {
  return (
    <div className="card p-8 text-center" role="alert">
      <p className="text-sm font-semibold text-brand-gold">permission-denied</p>
      <h1 className="mt-2 text-2xl font-bold">권한 없음</h1>
      <p className="mt-3 text-slate-600">
        R09 시스템관리자 권한이 없는 사용자는 이 메뉴를 볼 수 없으며 직접 API
        요청도 403으로 차단됩니다.
      </p>
    </div>
  );
}
