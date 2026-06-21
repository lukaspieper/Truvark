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
