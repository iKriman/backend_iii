package cl.bancoxyz.bff.model;

public record DataQualityReport(
        String source,
        int totalRows,
        int acceptedRows,
        int rejectedRows,
        int duplicatedRows
) {
}
