# Production Launch

This repository contains the release defaults and runbooks needed to ship `AppLimiter` to Google Play.

## Release Values

| Item | Value |
| --- | --- |
| Product name | `AppLimiter` |
| Android application ID | `com.stas.applimiter` |
| Store version | `1.0.0` |
| Build number | `1` |
| Android signing config | `keystore.properties` |
| Release keystore template | `keystore.properties.example` |
| Support email | `stanislavkholmanskii@gmail.com` |
| Privacy policy source | `docs/release/privacy-policy.md` |
| Privacy policy URL | https://stasholmansky.github.io/applimiter-privacy/ |
| Store listing draft | `docs/release/store-listing.md` |
| Brand master icon | `docs/branding/app-icon.png` |

## Before Submission

1. Use the public privacy policy URL above in Google Play Console.
2. Confirm the support email inbox (`stanislavkholmanskii@gmail.com`) is monitored.
3. Prepare feature graphic `1024x500` and at least 2 phone screenshots.
4. Complete Data safety, content rating, target audience, and ads declaration.
5. Complete Play Console declarations for:
   - Accessibility API use case
   - `QUERY_ALL_PACKAGES` / all-files style package visibility justification
   - Usage access purpose

## Sensitive policy notes

Google reviews apps with Accessibility and broad package visibility carefully.

Be ready to explain that:

- Accessibility is used only to leave a limited app after the daily limit is exhausted
- Package visibility is required to let the user pick apps to limit
- Usage stats are required to measure daily usage against those limits
- No remote collection of usage content for ads/analytics

## Accounts

1. `Google Play Console`
2. Support inbox for `stanislavkholmanskii@gmail.com`

## Android Release Setup

1. Ensure the shared upload keystore is available.
2. Copy `keystore.properties.example` to `keystore.properties` if needed.
3. Fill in the real store file path, passwords, and alias.
4. Keep the keystore file and `keystore.properties` out of git.
5. Build the upload bundle:

```bash
./gradlew :app:bundleRelease
```

Expected output:

- `app/build/outputs/bundle/release/app-release.aab`

## Data Safety Notes

Declare at least:

- Local app storage for limits, theme preference, and monitoring state
- App activity / usage-related data processed on device for limiting
- Installed app list processed on device for selecting limits
- No account, no ads SDK, no analytics SDK, no location permission
- No sale of data
