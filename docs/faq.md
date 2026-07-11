<!--
SPDX-FileCopyrightText: 2026 Lukas Pieper

SPDX-License-Identifier: GPL-3.0-or-later
-->

# FAQ

This FAQ is continuously extended based on user feedback and recurring questions. If you encounter something that is not
covered here, feedback and suggestions are welcome.

## How do I lock an open vault?

Currently, Truvark does not provide a dedicated lock action for an open vault. Once you have finished your work and no
background operations are running (check the Android notifications), you may lock the vault by exiting the app, for
example by removing it from the recent apps list.

Support for manually locking a vault is planned and expected to be added in the near future.

## Where are my decrypted files?

Decrypted files are stored in the `decrypted` folder inside the vault directory. This folder keeps the same directory
structure as the vault.

Due to file permission restrictions, it is not possible to place the decrypted files back into the original location
where they were encrypted from.

## How do I back up an entire vault?

To create a complete backup of a vault, copy the `vault` file, the `index` file, and the entire `files` directory
(including all of its contents) to your backup location. It is recommended to create the backup while the vault is not
in use to avoid any conflicts.

## Are thumbnails encrypted?

Yes. Truvark is designed so that thumbnails do not compromise the security of your vault.

Thumbnails are stored in the app's private storage instead of shared storage because this provides significantly better performance on Android. Although they are stored privately, thumbnails are still encrypted using the same encryption scheme as every other file in a vault. This means they cannot be decrypted unless the corresponding vault has been unlocked.

The thumbnail cache is shared across all vaults. As a result, even if an attacker gained access to the app's private storage, they could not determine which vault a particular thumbnail belongs to.

To further reduce metadata leakage, thumbnail filenames are not derived directly from the encrypted file identifiers. Instead, Truvark uses a pseudorandom function (PRF) to generate thumbnail identifiers. Unlike a regular hash, a PRF requires a secret key, preventing an attacker from linking a thumbnail to its corresponding encrypted file.

Thumbnails are generated only when a media file is viewed and are not created during encryption. The cache has a size limit, so older thumbnails are automatically removed over time. You can also delete all cached thumbnails at any time by clearing the app's cache through Android.
