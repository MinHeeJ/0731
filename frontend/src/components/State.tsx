export function StateBanner({
  type,
  message,
}: {
  type: "loading" | "error" | "success" | "empty" | "permission";
  message: string;
}) {
  return (
    <div role="status" className={`banner ${type}`}>
      {message}
    </div>
  );
}

export function PermissionPanel({ operationId }: { operationId: string }) {
  return (
    <StateBanner
      type="permission"
      message={`권한이 없습니다. operationId=${operationId}`}
    />
  );
}

export function FieldError({
  name,
  fields,
}: {
  name: string;
  fields?: Record<string, string>;
}) {
  return fields?.[name] ? (
    <small className="field-error">{fields[name]}</small>
  ) : null;
}
