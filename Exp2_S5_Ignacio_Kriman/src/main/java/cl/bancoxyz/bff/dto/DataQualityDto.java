package cl.bancoxyz.bff.dto;

public record DataQualityDto(
        String source,
        int totalRows,
        int acceptedRows,
        int rejectedRows,
        int duplicatedRows
) {
}
