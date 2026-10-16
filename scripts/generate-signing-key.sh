#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEY_DIR="$ROOT/signing"
KEYSTORE="$KEY_DIR/opencode-release.jks"
PROPERTIES="$ROOT/signing.properties"
ALIAS="${OPENCODE_KEY_ALIAS:-opencode-release}"
VALIDITY_DAYS="${OPENCODE_KEY_VALIDITY_DAYS:-10000}"

if ! command -v keytool >/dev/null 2>&1; then
  echo "ERROR: keytool was not found. Install a JDK (Java 17 recommended) and retry." >&2
  exit 1
fi

if [[ -e "$KEYSTORE" || -e "$PROPERTIES" ]]; then
  echo "Signing files already exist; refusing to overwrite them." >&2
  echo "Existing keystore: $KEYSTORE" >&2
  echo "Properties file:   $PROPERTIES" >&2
  echo "Back them up first, or move them away if you intentionally want a new signing identity." >&2
  exit 1
fi

mkdir -p "$KEY_DIR"
chmod 700 "$KEY_DIR"

# Generate high-entropy alphanumeric passwords so they can safely be stored
# in Java properties without escaping special characters.
make_password() {
  od -An -N36 -tx1 /dev/urandom | tr -d ' \n' | cut -c1-48
}
STORE_PASSWORD="$(make_password)"
KEY_PASSWORD="$(make_password)"

keytool -genkeypair -v \\
  -keystore "$KEYSTORE" \\
  -storetype JKS \\
  -storepass "$STORE_PASSWORD" \\
  -keypass "$KEY_PASSWORD" \\
  -alias "$ALIAS" \\
  -keyalg RSA -keysize 3072 \\
  -validity "$VALIDITY_DAYS" \\
  -dname "CN=OpenCode Android, OU=Mobile, O=OpenCode, L=Unknown, ST=Unknown, C=US"

cat > "$PROPERTIES" <<EOF
storeFile=signing/opencode-release.jks
storePassword=$STORE_PASSWORD
keyAlias=$ALIAS
keyPassword=$KEY_PASSWORD
EOF
chmod 600 "$PROPERTIES"
chmod 600 "$KEYSTORE"

echo
echo "Signing key generated."
echo "Keystore: $KEYSTORE"
echo "Gradle signing config: $PROPERTIES"
echo
echo "IMPORTANT:"
echo " - Back up both files securely. Losing this key can prevent updates to installed releases."
echo " - Never commit signing/ or signing.properties to Git."
echo " - Keep this same key for every future release of the app."
echo
echo "Build signed debug + release APKs with:"
echo "  ./gradlew :app:assembleDebug :app:assembleRelease"
