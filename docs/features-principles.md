<!--
SPDX-FileCopyrightText: 2022 Lukas Pieper

SPDX-License-Identifier: GPL-3.0-or-later
-->

# Features

## Multiple vaults

You can create multiple vaults on your device. Any empty folder can become a vault. All your data remains on the shared
device storage, which means you can access the encrypted files from a file manager, for example, for backups.

> [!TIP]
> Using the device's shared storage is a significant difference compared to alternatives. Some apps don't even encrypt
> your files, they just move them to the app's internal storage. This is often referred to as "data hiding" rather than
> encrypting.

## Deep folder structures

Truvark is not an encrypted gallery that just lets you group your images into albums. It is a file encryption app with
full support for subfolders. You are not limited in how you organize your files.

## View encrypted files

Common file types can be viewed in the application. Currently supported are images, video and audio. Decryption is done
*on the fly*, which means that the required data remains in memory (RAM) instead of being written to storage. This is
especially important for long videos that would otherwise not fit in memory. The image viewer supports high resolution
images and shows more details when zooming in instead of getting pixelated (called *subsampling*).

> [!TIP]
> Some popular alternative apps decrypt the entire file to disk before displaying it. This sacrifices performance and
> may put the file at risk.

# Core Principles

## Privacy by default

In short, this app has no Internet permissions. There are no analytics, ads, telemetry, or account requirements. There
is an option in the settings to enable on-device logging, which is turned off by default.

## Security by design

In cryptography, it is enough to get a single parameter wrong to make software insecure. To reduce this risk, popular
open source libraries are used.

One of them is [Tink](https://github.com/tink-crypto/tink-java), a crypto library built by Google engineers and
"deployed in hundreds of products and systems". It was designed with the goal of reducing insecure software due to
"configuration" errors. They feature this prominently:

> *Tink provides secure APIs that are easy to use correctly and hard(er) to misuse.*

Truvark uses the `AES256_GCM_HKDF_1MB` *StreamingAEAD* primitive from Tink for file encryption. *AEAD* stands for
"Authenticated Encryption with Associated Data" and is a type of encryption that provides both confidentiality and
integrity. It ensures that the data is not only encrypted but also protected against tampering. Truvark makes use of the
associated data since version 2.0. *Streaming* refers to an optimized way of encrypting large files. More details can be
found in the [Tink documentation](https://developers.google.com/tink/streaming-aead).

In addition, Argon2(id) is used for key derivation. It won the
[Password Hashing Competition](https://en.wikipedia.org/wiki/Password_Hashing_Competition)
in 2015 and is one of the best (if not the best) algorithm for this task.

Instead of a database, index files are used to store metadata. They are encrypted the same way the user files are
encrypted.

> [!TIP]
> Many other vault apps use an unencrypted database!
