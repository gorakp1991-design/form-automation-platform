package com.formautomation.data.importer

import com.formautomation.domain.model.Dataset
import com.formautomation.domain.model.DataRow
import java.io.File

class XlsxImporter {
    /**
     * XLSX parsing using a lightweight library like Apache POI or similar
     * For MVP, we recommend focusing on CSV first.
     * XLSX support can be added in Phase 2.5 if needed.
     */
    fun import(file: File): ImportResult {
        return ImportResult.Error("XLSX support coming in Phase 2.5")
    }
}