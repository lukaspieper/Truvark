<!--
SPDX-FileCopyrightText: 2026 Lukas Pieper

SPDX-License-Identifier: GPL-3.0-or-later
-->

# Threat model

This document describes what Truvark protects, which attacks are considered, and which risks remain. It is a living
document: implementation changes can invalidate an assumption or change the status of a risk.

## Security Objectives

The primary security objective is confidentiality and integrity of vault contents while the vault is at rest. A vault is
considered at rest when no password, derived password key, or decrypted cryptographic key material remains in memory.

Given only the vault directory and any copies of it, an attacker without the user's password should not be able to:

- recover plaintext
- forge or substitute ciphertext that authenticates successfully in an unintended context
- recover encrypted metadata beyond intentionally exposed metadata

The security guarantees of a locked vault remain valid even if the device storing the vault is compromised, provided the
vault has not been unlocked on that device. Once the vault is unlocked on a compromised device, the attacker is
considered to have access to the decrypted vault contents and cryptographic material.

## System Overview

The vault is intentionally stored in Android's shared storage, which is treated as accessible to an attacker. The
security model therefore assumes the attacker can freely copy, replace, or inspect vault files.

The vault contains:

- encrypted file contents
- encrypted indices of file and folder names and metadata

The user's password is processed with Argon2id to derive a key-encryption key. This key decrypts encrypted Tink keysets,
which contain the StreamingAEAD and PRF keys used by the vault.

## Assets

| Asset                              | Security Property                            |
|------------------------------------|----------------------------------------------|
| Vault contents                     | Confidentiality and integrity                |
| Vault indices and metadata         | Confidentiality and integrity                |
| Vault configuration                | Integrity; Confidentiality for keysets       |
| App settings (DataStore)           | Confidentiality for biometric config         |
| Thumbnail cache                    | Confidentiality, integrity and unlinkability |
| Password                           | Never disclosed                              |
| Derived keys and decrypted keysets | Present only while unlocked                  |

## Security Boundary

Truvark protects the confidentiality and integrity of encrypted vault contents and metadata, and the confidentiality of
cryptographic key material, within the security objectives defined above.

The following are outside the protection boundary:

- exported plaintext
- user-created copies
- data elsewhere in shared storage
- observable metadata (see below)

Availability is not a security objective. An attacker with access to the vault directory can delete or corrupt vault
files or delete the entire vault.

### Observable Metadata While Locked

The following metadata remains observable even while the vault is locked and is not considered confidential by this
model:

- vault existence
- vault identifier
- vault display name
- Argon2 parameters
- ciphertext sizes
- UUID object names
- object counts
- file timestamps

## Assumptions

This model assumes:

- the password is strong and confidential (not disclosed to an attacker)
- cryptographic algorithms used by the application remain secure and are correctly implemented
- the Android keystore and biometric authentication mechanisms provided by the platform are secure
- a compromised/rooted operating system can access cryptographic key material when the vault is unlocked
- a compromised/rooted operating system cannot obtain cryptographic key material from vaults that have never been
  unlocked on that device

### Attacker Capabilities

The threat model assumes an attacker can:

- copy the entire vault directory
- modify, replace, or delete vault files
- restore older versions of vault files
- inspect ciphertext indefinitely
- perform offline password-guessing attacks
- compare multiple snapshots of the vault over time
- potentially access the app-private storage

The attacker is not assumed to:

- know the user's password
- break standard cryptographic primitives

## Threats

The following threats are considered within this security model.

| Threat                                    | Description                                                                                                                                                                  | Mitigation / Status                                                                                                                                                                                                                                                                                                                          |
|-------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Offline password cracking                 | An attacker obtains a copy of the vault and attempts to recover the user's password by repeatedly deriving keys and testing decryption attempts.                             | Mitigated by Argon2id password-based key derivation with memory-hard parameters. Security depends on password strength and chosen Argon2 parameters.                                                                                                                                                                                         |
| Vault contents disclosure                 | An attacker with access to the vault directory attempts to recover plaintext files, folder names, or metadata without unlocking the vault.                                   | Mitigated by authenticated encryption of vault objects and encrypted indices.                                                                                                                                                                                                                                                                |
| Ciphertext modification                   | An attacker modifies encrypted files or metadata in an attempt to alter vault contents or inject malicious data.                                                             | Mitigated by authenticated encryption. Modified ciphertext should fail authentication.                                                                                                                                                                                                                                                       |
| Ciphertext swapping / object substitution | An attacker exchanges valid encrypted objects, for example replacing one file's ciphertext with another file's ciphertext.                                                   | Mitigated by using associated data during authenticated encryption. The ciphertext is cryptographically bound to its expected context.                                                                                                                                                                                                       |
| Key recovery from locked vaults           | An attacker attempts to recover encryption keys from the stored vault files.                                                                                                 | Mitigated by encrypting keysets with a key derived from the user's password.                                                                                                                                                                                                                                                                 |
| Thumbnail cache disclosure                | An attacker obtains the thumbnail cache and attempts to recover image previews, correlate thumbnails with vault objects, or infer which files have corresponding thumbnails. | Mitigated by encrypting thumbnails using the same authenticated-encryption key material as regular vault files. A PRF is used for thumbnail identifiers to prevent direct linkability between thumbnails and their associated vault objects. Residual risks include metadata leakage such as thumbnail ciphertext sizes and access patterns. |
| Screen capture                            | An attacker attempts to capture unlocked vault contents using screenshots, screen recording, or the recent-apps screen.                                                      | Mitigated by `FLAG_SECURE` since v2.1. Effectiveness depends on Android platform enforcement and does not protect against physical cameras or compromised system components. The protection is enabled by default but can be disabled by the user.                                                                                           |
| Overlay attacks                           | A malicious application attempts to display UI over the vault interface to deceive the user or capture input.                                                                | Mitigated by `HIDE_OVERLAY_WINDOWS` since v2.1. Does not protect against privileged/root attackers.                                                                                                                                                                                                                                          |
| Biometric unlock bypass                   | An attacker attempts to bypass biometric authentication or obtain access to biometric-protected vault unlock material.                                                       | Mitigated by protecting the stored unlock credential with an Android Keystore key requiring Class 3 (strong) biometric authentication.                                                                                                                                                                                                       |
| Sensitive log disclosure                  | Sensitive information such as vault paths, identifiers, or operation details is leaked through system logs.                                                                  | Mitigated by logging being disabled by default. When enabled, logs may contain operational details but are not transmitted off-device.                                                                                                                                                                                                       |
| Compromised device while unlocked         | Malware or a privileged attacker accesses the application after the vault has been unlocked.                                                                                 | Not mitigated.                                                                                                                                                                                                                                                                                                                               |
| Snapshot comparison                       | An attacker obtains multiple copies of the vault at different times and compares changes.                                                                                    | Not mitigated.                                                                                                                                                                                                                                                                                                                               |
| Rollback attacks                          | An attacker replaces the current vault state with an older valid copy in order to hide changes or revert vault contents.                                                     | Not mitigated.                                                                                                                                                                                                                                                                                                                               |
| Metadata analysis                         | An attacker analyzes observable information such as ciphertext sizes, object counts, timestamps, or identifiers to infer information about the vault.                        | Not mitigated. Observable metadata is explicitly outside the confidentiality guarantees.                                                                                                                                                                                                                                                     |
| Denial of service                         | An attacker deletes, corrupts, or blocks access to vault files.                                                                                                              | Not mitigated. Availability is explicitly outside the security objectives.                                                                                                                                                                                                                                                                   |
| Password disclosure                       | An attacker obtains the user's password through reuse, phishing, observation, or another compromise.                                                                         | Outside cryptographic protection. The vault security depends on password secrecy.                                                                                                                                                                                                                                                            |
