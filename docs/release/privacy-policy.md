# Privacy Policy

Last updated: `2026-07-10`

`AppLimiter` helps you set daily usage limits for installed apps and optionally block an app after its limit is reached.

**Public URL (use this in Google Play / App Store):**  
https://stasholmansky.github.io/applimiter-privacy/

## Who we are

For questions about this policy, contact:

- Email: `stanislavkholmanskii@gmail.com`

## What the app stores

The app stores the following information locally on your device:

- App package names and display names that you choose to limit
- Daily time limits that you configure
- Theme preference (system / light / dark)
- Monitoring on/off state needed for the limiter to work

The app does not create an account and does not upload your usage history to our servers.

## Sensitive permissions and why they are needed

`AppLimiter` requests sensitive Android permissions only for core product functionality:

1. **Usage access (`PACKAGE_USAGE_STATS`)**  
   Used to measure how long selected apps were used today so the app can compare usage against your limits.

2. **Query installed apps (`QUERY_ALL_PACKAGES`)**  
   Used to show the list of launchable apps on your device so you can choose which ones to limit.

3. **Accessibility service**  
   Used only to detect when an app with an exhausted daily limit is opened and return you to the home screen. The service is not used to read passwords, message contents, or payment details, and it is not used for advertising or analytics.

4. **Notifications and foreground service**  
   Used to keep monitoring active while limits are enforced and to show a persistent monitoring notification when needed.

All of these permissions are optional until you enable the corresponding system settings. Without them, monitoring and blocking cannot work.

## Network requests

In the current version, `AppLimiter` does not send your usage data or installed-app list to remote servers for analytics or advertising.

Opening the Feedback screen may launch your email app so you can contact support.

## Accounts and authentication

The app does not require an account and does not include sign-in.

## Analytics, ads, and tracking

The app does not include advertising SDKs or analytics/tracking SDKs in this repository version.

## Location

The app does not request location permission.

## Data sharing

Your limits and local usage-related settings are not shared with other users. Data stays on your device unless you uninstall the app or clear app storage.

## Data retention

Your data remains on your device until you delete it, uninstall the app, or clear app storage.

## Your choices

You can:

- Disable usage access, accessibility, or notifications in Android settings
- Turn monitoring off inside the app
- Remove individual app limits
- Uninstall the app or clear app storage to remove local data
