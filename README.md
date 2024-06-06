<div align="center">
  <h1>daily.dev Android app</h1>
</div>
<br>

A [bubblewrap](https://github.com/GoogleChromeLabs/bubblewrap/tree/main/packages/cli) generated Android project.
The app uses Trusted Web Activity (TWA) to publish our [Progressive Web App (PWA)](https://app.daily.dev/) to the Play Store.

Here's a guide if you want to learn more:
https://developer.chrome.com/docs/android/trusted-web-activity/quick-start/

## Getting Started

### Clone this repo

```
git clone git@github.com:dailydotdev/android.git
```

### Install Bubblewrap

```
npm i -g @bubblewrap/cli
```

### Build the application

```
bubblewrap build
```

This requires a keystore for signing the APK, you can create your own for local testing.
Bubblewrap can create it for you.

Bubblewrap will generate a `app-release-signed.apk` file that you can copy to your device and install it to try it out.


### Update manifset

In case the PWA manifest was updated, you can use Bubblewrap to fetch the new manifest and update the project accordingly.

```
bubblewrap merge
bubblewrap update
```

### Facebook SDK

Make sure to follow this guide whenever you generate the project again.
https://developers.facebook.com/docs/android/getting-started
