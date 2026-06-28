/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.licensing

import android.content.Context
import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.lukaspieper.truvark.R

public class OpenSourceLicenseViewModel(private val context: Context) : ViewModel() {

    public val licensedItems: List<LicensedItem> by mutableStateOf(fetchLicenseItems(context.resources))

    public var selectedLicense: License by mutableStateOf(License.UnknownLicense)
        private set

    public var selectedLicenseText: String by mutableStateOf("")
        private set

    public fun selectLicense(license: License) {
        selectedLicense = license
        selectedLicenseText = license.textResId?.let { readRawStringResource(context.resources, it) } ?: ""
    }

    public fun clearSelectedLicense() {
        selectedLicense = License.UnknownLicense
        selectedLicenseText = ""
    }

    private fun fetchLicenseItems(resources: Resources): List<LicensedItem> {
        val metadata = readRawStringResource(resources, R.raw.third_party_license_metadata)
        val licenseUris = readRawStringResource(resources, R.raw.third_party_licenses)

        // TODO: Libraries used in the KMP Java target are incorrectly included in the generated license metadata file.
        val incorrectlyDetectedLicensedItems = setOf("Argon2 JVM", "Java Native Access")
        val undetectedLicensedItems = listOf<LicensedItem>()

        val metadataRegex = Regex("^(\\d+):(\\d+)\\s(.+)$")
        return metadata.lineSequence()
            .mapNotNull { line -> metadataRegex.matchEntire(line) }
            .mapNotNull { matchResult ->
                val (uriStartIndex, uriLength, itemId) = matchResult.destructured

                if (incorrectlyDetectedLicensedItems.contains(itemId)) {
                    return@mapNotNull null
                }

                val licenseUri = licenseUris.substring(uriStartIndex.toInt(), uriStartIndex.toInt() + uriLength.toInt())
                LicensedItem(itemId, License.getByUri(licenseUri))
            }
            .plus(undetectedLicensedItems)
            .distinctBy { it.id.lowercase() }
            .sortedBy { it.id.lowercase() }
            .toList()
    }

    private fun readRawStringResource(resources: Resources, @RawRes id: Int): String {
        resources.openRawResource(id).use { inputStream ->
            return inputStream.bufferedReader().readText()
        }
    }
}
