# Prototype idle sessions and local shoulder-surfing design

## Implemented backend

- JWTs expire absolutely after 15 minutes.
- Each login gets a unique server-tracked session ID (JWT jti).
- An authenticated session expires after **five minutes without an explicit user-activity event**.
- Ordinary API calls, polling, background synchronization, camera detections and network traffic **do not** refresh the timer.
- POST /api/v1/auth/activity with the current Bearer token refreshes the server deadline only while the session is still valid. The client MUST call it only in response to actual user interaction, not periodically or from camera motion. Throttle/coalesce bursts of real events.
- POST /api/v1/auth/logout revokes the session. Expired sessions cannot be revived.
- The registry is in-memory, single-node and prototype-only: restart logs everyone out; multi-instance deployment requires shared storage and lifecycle management.
- There is no frontend in this repository. Therefore a visible five-minute lock, client-side idle tracking, token clearing and camera behavior **are not yet implemented**. The server denies expired sessions regardless of UI state.
- A short absolute token expiry can interrupt an active user after 15 minutes; a future renewal mechanism must preserve the independent five-minute idle limit.

## Local-only shoulder-surfing design (not implemented)

Privacy-first default: camera **off until explicitly opted in**. All frame inference runs locally, with no uploads, recordings, face recognition, identity matching or persistent embeddings. Camera permission is visible and revocable; provide a manual privacy lock as a fallback.

Use a state machine with three stages:
1. **Candidate**: detect an additional person within a defined near-screen viewing zone; hallway traffic and distant people are excluded where practical. A single detected face is not sufficient.
2. **Likely observer**: require sustained proximity, face/head orientation toward the display, and repeated detection across a short temporal window (for example 1.5–2.5 seconds). Treat orientation as a noisy proxy, not proof of gaze or intent.
3. **Privacy response**: initially obscure only high-sensitivity panels or show a subtle privacy indicator; escalate to full blur when confidence and dwell time increase. Unblur after a short stable clear period (for example 2–3 seconds) to avoid flicker.

Add configurable sensitivity and per-device calibration. Do not infer identities, race, age, emotions, or intentions. Fail safely when camera is unavailable, with user-controlled screen lock and short idle expiration. Avoid camera-driven automatic logout: a false positive should never destroy unsaved clinical work.

Test with hall passersby, people conversing in the background, seated colleagues, reflections, poor lighting, camera occlusion, multiple monitors, and actual close observers. Measure false blur activations per hour as well as missed observations. For clinical deployments, evaluate accessibility, consent, policy, and safety before enabling.

**Do not use real patient data with this prototype.**
