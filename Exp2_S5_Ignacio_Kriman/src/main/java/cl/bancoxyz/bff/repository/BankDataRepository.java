package cl.bancoxyz.bff.repository;

import cl.bancoxyz.bff.model.AccountProfile;
import cl.bancoxyz.bff.model.AnnualMovement;
import cl.bancoxyz.bff.model.DailyTransaction;
import cl.bancoxyz.bff.model.DataQualityReport;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class BankDataRepository {

    private final Map<Integer, AccountProfile> accounts = new HashMap<>();
    private final List<AnnualMovement> annualMovements = new ArrayList<>();
    private final List<DailyTransaction> dailyTransactions = new ArrayList<>();
    private final List<DataQualityReport> qualityReports = new ArrayList<>();

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    @PostConstruct
    void loadData() throws IOException {
        loadAccounts();
        loadAnnualMovements();
        loadDailyTransactions();
    }

    public List<AccountProfile> findAccounts() {
        return accounts.values().stream()
                .sorted(Comparator.comparing(AccountProfile::accountId))
                .toList();
    }

    public Optional<AccountProfile> findAccount(Integer accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public List<AnnualMovement> findMovementsByAccount(Integer accountId) {
        return annualMovements.stream()
                .filter(movement -> movement.accountId().equals(accountId))
                .sorted(Comparator.comparing(AnnualMovement::date).reversed())
                .toList();
    }

    public List<DailyTransaction> findDailyTransactions() {
        return dailyTransactions.stream()
                .sorted(Comparator.comparing(DailyTransaction::date).reversed())
                .toList();
    }

    public List<DataQualityReport> qualityReports() {
        return List.copyOf(qualityReports);
    }

    private void loadAccounts() throws IOException {
        int total = 0;
        int accepted = 0;
        int duplicated = 0;
        Set<String> seenRows = new HashSet<>();

        try (BufferedReader reader = reader("data/intereses.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                if (!seenRows.add(line)) {
                    duplicated++;
                    continue;
                }
                String[] parts = split(line, 5);
                Optional<Integer> accountId = parseInteger(parts[0]);
                Optional<BigDecimal> balance = parseAmount(parts[2]);
                Optional<Integer> age = parseInteger(parts[3]);
                String name = cleanText(parts[1], "Cliente Banco XYZ");
                String productType = normalizeProductType(parts[4]);

                if (accountId.isEmpty() || balance.isEmpty() || age.isEmpty() || productType == null) {
                    continue;
                }
                if (age.get() < 18 || age.get() > 99) {
                    continue;
                }
                if (balance.get().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                accounts.putIfAbsent(accountId.get(), new AccountProfile(
                        accountId.get(),
                        name,
                        balance.get(),
                        age.get(),
                        productType
                ));
                accepted++;
            }
        }
        qualityReports.add(new DataQualityReport("intereses.csv", total, accepted, total - accepted, duplicated));
    }

    private void loadAnnualMovements() throws IOException {
        int total = 0;
        int accepted = 0;
        int duplicated = 0;
        Set<String> seenRows = new HashSet<>();

        try (BufferedReader reader = reader("data/cuentas_anuales.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                if (!seenRows.add(line)) {
                    duplicated++;
                    continue;
                }
                String[] parts = split(line, 5);
                Optional<Integer> accountId = parseInteger(parts[0]);
                Optional<LocalDate> date = parseDate(parts[1]);
                String transactionType = normalizeTransactionType(parts[2]);
                Optional<BigDecimal> amount = parseAmount(parts[3]);
                String description = cleanText(parts[4], "Movimiento sin descripcion");

                if (accountId.isEmpty() || date.isEmpty() || transactionType == null || amount.isEmpty()) {
                    continue;
                }
                if (amount.get().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                annualMovements.add(new AnnualMovement(
                        accountId.get(),
                        date.get(),
                        transactionType,
                        amount.get(),
                        description
                ));
                accepted++;
            }
        }
        qualityReports.add(new DataQualityReport("cuentas_anuales.csv", total, accepted, total - accepted, duplicated));
    }

    private void loadDailyTransactions() throws IOException {
        int total = 0;
        int accepted = 0;
        int duplicated = 0;
        Set<String> seenRows = new HashSet<>();

        try (BufferedReader reader = reader("data/transacciones.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                if (!seenRows.add(line)) {
                    duplicated++;
                    continue;
                }
                String[] parts = split(line, 4);
                Optional<Integer> id = parseInteger(parts[0]);
                Optional<LocalDate> date = parseDate(parts[1]);
                Optional<BigDecimal> amount = parseAmount(parts[2]);
                String type = normalizeCreditDebit(parts[3]);

                if (id.isEmpty() || date.isEmpty() || amount.isEmpty() || type == null) {
                    continue;
                }
                if (amount.get().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                dailyTransactions.add(new DailyTransaction(id.get(), date.get(), amount.get(), type));
                accepted++;
            }
        }
        qualityReports.add(new DataQualityReport("transacciones.csv", total, accepted, total - accepted, duplicated));
    }

    private BufferedReader reader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8));
    }

    private String[] split(String line, int expected) {
        String[] raw = line.split(",", -1);
        String[] values = new String[expected];
        for (int index = 0; index < expected; index++) {
            values[index] = index < raw.length ? raw[index].trim() : "";
        }
        return values;
    }

    private Optional<Integer> parseInteger(String value) {
        try {
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> parseAmount(String value) {
        try {
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new BigDecimal(value.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return Optional.of(LocalDate.parse(value.trim(), formatter));
            } catch (DateTimeParseException ignored) {
            }
        }
        return Optional.empty();
    }

    private String cleanText(String value, String defaultValue) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("unknown")) {
            return defaultValue;
        }
        return value.trim();
    }

    private String normalizeProductType(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "ahorro", "prestamo", "hipoteca" -> normalized;
            default -> null;
        };
    }

    private String normalizeCreditDebit(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "credito", "debito" -> normalized;
            default -> null;
        };
    }

    private String normalizeTransactionType(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "deposito", "retiro", "compra", "pago" -> normalized;
            default -> null;
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("é", "e")
                .replace("á", "a")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }
}
