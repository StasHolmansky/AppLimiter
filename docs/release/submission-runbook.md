# Submission Runbook

## Recommended Sequence

1. Finish store assets (feature graphic + screenshots)
2. Create a release candidate AAB
3. Test the signed release build on a physical device, including permissions
4. Upload to `Internal testing` on Google Play
5. Complete Accessibility / package visibility declarations carefully
6. Fix issues from internal review or Play policy questions
7. Submit for production review
8. Roll out gradually and monitor feedback

## Android

### Build

```bash
./gradlew :app:bundleRelease
```

Expected output:

- `app/build/outputs/bundle/release/app-release.aab`

### Upload

1. Open `Google Play Console`
2. Create the app entry for `AppLimiter`
3. Complete store listing, Data safety, content rating, and app access sections
4. Set privacy policy URL to `https://stasholmansky.github.io/applimiter-privacy/`
5. Fill Accessibility API and package visibility declarations
6. Upload the `.aab` to `Internal testing`
7. Verify install, permission flows, monitoring, and blocking on a test device

## Rollout Guardrails

- Start with a limited Android rollout percentage
- Watch support inbox and policy emails closely after submission
- Prepare a `1.0.1` hotfix if Play requests wording or permission clarifications
