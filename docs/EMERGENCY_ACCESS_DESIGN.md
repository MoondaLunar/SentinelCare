# Emergency access and clinical privacy: proposed design

**Status: design plus isolated policy helper, NOT deployed emergency authentication.**
Do not use for real patient information or represent as HIPAA-certified.

## Agreed behavior

- Sentinel Care **application profile** locks after 300 seconds without genuine user interaction, including scrolling; it does not lock Windows. Background sync or camera motion never counts as activity. Backend must enforce session expiration independently of the UI.
- Sync dashboard shows acknowledged successes, durable pending queue, failures, and conflicts. Never mark an operation synced until the backend acknowledges it. Encrypt local queue; retain audit events across offline periods.
- Camera frames are processed **only on device**. Do not transmit frames, recordings, face embeddings or pixel data to cloud services. Log only necessary security-event metadata.
- Break-glass request is always audited and notifies admin/IT. Issue a short-lived, one-time, request-bound code to an **individually verified** staff identity. Do not use a universal shared password.
- Before administrator approval, a verified, scoped temporary session may read an emergency summary. Only accounts with a centrally assigned ER, ICU, NICU or PICU designation **and** EMERGENCY_NOTE_CREATE permission may append urgent treatment notes. No edits to existing records, exports, bulk reads, or administrator operations.
- Admin approval may grant expanded access only within the user's normal authorized scope. Denial revokes the emergency session. Temporary sessions expire and may be revoked.
- Require identity verification and admin/IT notification. During a formally declared emergency, an authorized, time-bounded override may waive the *physical verification visit*, never identity checks, authorization, alerts or audit logging. Capture incident identifier, authorizer, reason, and time window.
- Preserve mother/newborn chart separation, guardian/custody rules and patient-specific access checks.
- Every request, code issuance, attempt, read, note, approval, denial, expiry and emergency override must generate an attributed, durable audit event. Offline events queue securely and reconcile on reconnect. An inability to preserve an audit record must fail closed or invoke a separately reviewed clinical downtime procedure.

## Planned architecture (not yet implemented)

1. Account-directory-backed department and permission assignments, not client-provided roles or headers.
2. Code issuance/validation, rate limits, replay prevention, and authenticated device binding.
3. Restricted emergency token scopes, patient IDs, expiry, revocation and backend enforcement at every endpoint.
4. Append-only emergency note endpoint with patient-level authorization and attribution.
5. Durable audit outbox; SHA-256 hash links, keyed signatures or HMAC, independent checkpoints and verification. Hashing alone does not prevent rewriting the chain.
6. Admin verification/approval/denial dashboard with alerts, audit inspection and declared-emergency override workflow.
7. Application-profile idle lock with frontend activity handling and server-side session enforcement.
8. Persistent encrypted offline sync queue and explicit per-operation acknowledgements.
9. Local-only camera privacy pipeline with tests proving no frame uploads.

## Scope of this PR

The pure `EmergencyAccessPolicy` helper models the **preapproval** read/append-only note distinction and includes unit tests. It is intentionally **not wired into controllers** until a secure emergency session, verified identity, patient scoping, account-directory designation and durable audit subsystem exist. No emergency login capability is enabled by this change.

## Required integration tests before enabling emergency access

- Standard staff cannot write emergency notes even via direct HTTP request.
- ER/ICU/NICU/PICU accounts without the explicit permission cannot write.
- Client-forged roles, departments, or headers never grant permissions.
- Expired, revoked, reused or wrong-device codes fail; remote guessing is rate-limited.
- No read or write outside the scoped patient.
- Denial and expiry revoke access immediately.
- Declared-emergency override never suppresses audit events.
- Offline audit and sync events survive restart and preserve ordering.
- Application idle lock protects backend endpoints as well as visible UI.
- No camera image/frame data appears in network requests or logs.
