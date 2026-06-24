---
name: Release checklist
about: Prepare a new release.
title: Release v{x.y.z}
labels: 'documentation'
assignees: ''
---

# Checklist

The following checklist should be completed before creating a new release. The checklist is not exhaustive, but should 
cover the most important features.

### Core features
- [ ] Create a new vault
- [ ] Open an existing vault
- [ ] Create a new (sub)folder
- [ ] Rename a folder
- [ ] Rename the vault
- [ ] Setup and use biometric unlocking
- [ ] Enable logging

### Background operations
- [ ] Encrypt
  - [ ] Files
  - [ ] Folder
  - [ ] Delete source files after encryption
- [ ] Decrypt
  - [ ] Files
  - [ ] Folders
- [ ] Delete
    - [ ] Files
    - [ ] Folders
- [ ] Move/Relocate
    - [ ] Files (root folder is not a valid target)
    - [ ] Folders

## UI

### General
- [ ] Switch light/dark mode (only via system settings)
- [ ] Switch between list and grid view
- [ ] Select items through dragging
- [ ] Media duration is displayed
- [ ] Settings UI is adaptive
- [ ] Show open source licenses

### Notifications
- [ ] Show notifications for all background operations
- [ ] Decryption notification stays until dismissed
    - [ ] Opens decryption directory on click (or button click)

### File presenter
- [ ] Show images
  - [ ] Raster images (png, jpg, webp)
    - [ ] Subsampling
  - [ ] Vector images (svg)
  - [ ] Animated images (gif)
  - [ ] Scale images to match screen size/keep original size (setting/toggle)
    - [ ] Zoom into small images (keep original size)
- [ ] Play media
  - [ ] Play videos (mp4)
  - [ ] Play audio (mp3)
  - [ ] Double tap to skip 10 seconds (video and audio)
- [ ] Show information for files that are not supported
- [ ] Show information for missing files

# Release steps
- [ ] Update `versionCode` and `versionName`
- [ ] Add changelog to Fastlane (`/fastlane/metadata/android/{en-US}/changelogs/{versionCode}.txt`) 
- [ ] Create a new release (with tag)
- [ ] Check F-Droid availability
