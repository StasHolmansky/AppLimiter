# QA Checklist

## Smoke

- [ ] App launches on a clean install
- [ ] Installed apps list loads
- [ ] Search filters apps by name/package
- [ ] Opening an app opens the limit editor
- [ ] Saving hours/minutes creates a limit
- [ ] Resetting a limit removes it
- [ ] Theme chips: System / Light / Dark apply and persist
- [ ] Feedback screen opens mail client or shows fallback message

## Permissions

- [ ] Usage Access card appears when permission is missing and opens system settings
- [ ] Accessibility card appears when service is off and opens system settings
- [ ] Monitoring toggle stays disabled until both permissions are granted
- [ ] Monitoring starts and shows active state
- [ ] Monitoring stops cleanly

## Limiting behavior

- [ ] Today's usage updates after using another app
- [ ] With monitoring on and limit exhausted, opening the limited app returns to home
- [ ] Apps without limits are unaffected

## Release build

- [ ] Install signed release APK/AAB
- [ ] Launcher icon and name look correct (`AppLimiter`)
- [ ] Persistent/foreground monitoring behavior is acceptable
- [ ] Support email is correct on Feedback screen
- [ ] Play Protect / sideload warnings understood for local testing (expected for Accessibility apps outside Play)
