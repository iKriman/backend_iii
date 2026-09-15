package cl.bancoxyz.bff.service;

import cl.bancoxyz.bff.dto.AccountDetailDto;
import cl.bancoxyz.bff.dto.AccountSummaryDto;
import cl.bancoxyz.bff.dto.AtmWithdrawalResponse;
import cl.bancoxyz.bff.dto.BalanceDto;
import cl.bancoxyz.bff.dto.DataQualityDto;
import cl.bancoxyz.bff.dto.InterestDto;
import cl.bancoxyz.bff.dto.MobileSummaryDto;
import cl.bancoxyz.bff.dto.MovementDto;
import cl.bancoxyz.bff.dto.WebDashboardDto;
import cl.bancoxyz.bff.model.AccountProfile;
import cl.bancoxyz.bff.model.AnnualMovement;
import cl.bancoxyz.bff.model.AtmWithdrawal;
import cl.bancoxyz.bff.repository.BankDataRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BankService {

    private final BankDataRepository repository;
    private final List<AtmWithdrawal> atmWithdrawals = new CopyOnWriteArrayList<>();

    public BankService(BankDataRepository repository) {
        this.repository = repository;
    }

    public WebDashboardDto webDashboard(String customerId) {
        List<AccountDetailDto> accounts = repository.findAccounts().stream()
                .map(account -> accountDetail(account.accountId()))
                .toList();

        return new WebDashboardDto(
                "WEB",
                customerId,
                accounts.stream().map(AccountDetailDto::balance).reduce(BigDecimal.ZERO, BigDecimal::add),
                accounts.size(),
                accounts,
                dataQuality()
        );
    }

    public MobileSummaryDto mobileSummary(String customerId) {
        List<AccountSummaryDto> accounts = repository.findAccounts().stream()
                .map(account -> new AccountSummaryDto(account.accountId(), account.productType(), balanceFor(account.accountId())))
                .toList();

        return new MobileSummaryDto(
                "MOBILE",
                customerId,
                accounts.stream().map(AccountSummaryDto::balance).reduce(BigDecimal.ZERO, BigDecimal::add),
                accounts.size(),
                accounts
        );
    }

    public AccountDetailDto accountDetail(Integer accountId) {
        AccountProfile account = accountOrFail(accountId);
        return new AccountDetailDto(
                account.accountId(),
                account.customerName(),
                account.productType(),
                balanceFor(accountId),
                interestFor(accountId).monthlyInterest(),
                movements(accountId).stream().limit(10).toList()
        );
    }

    public List<MovementDto> movements(Integer accountId) {
        accountOrFail(accountId);
        List<MovementDto> legacyMovements = repository.findMovementsByAccount(accountId).stream()
                .map(this::toMovementDto)
                .toList();

        List<MovementDto> atmMovements = atmWithdrawals.stream()
                .filter(withdrawal -> withdrawal.accountId().equals(accountId))
                .map(withdrawal -> new MovementDto(
                        withdrawal.createdAt().toLocalDate(),
                        "retiro_atm",
                        withdrawal.amount(),
                        "Retiro realizado por cajero automatico"
                ))
                .toList();

        return java.util.stream.Stream.concat(legacyMovements.stream(), atmMovements.stream())
                .sorted(Comparator.comparing(MovementDto::date).reversed())
                .toList();
    }

    public List<MovementDto> lastMovements(Integer accountId, int limit) {
        return movements(accountId).stream().limit(limit).toList();
    }

    public BalanceDto balance(Integer accountId) {
        accountOrFail(accountId);
        return new BalanceDto(accountId, balanceFor(accountId));
    }

    public InterestDto interestFor(Integer accountId) {
        AccountProfile account = accountOrFail(accountId);
        BigDecimal annualRate = annualRateFor(account.productType());
        BigDecimal monthlyInterest = account.baseBalance()
                .multiply(annualRate)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        return new InterestDto(
                account.accountId(),
                account.productType(),
                account.baseBalance(),
                annualRate,
                monthlyInterest
        );
    }

    public AtmWithdrawalResponse withdraw(Integer accountId, BigDecimal amount) {
        accountOrFail(accountId);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto del retiro debe ser mayor que cero");
        }
        BigDecimal currentBalance = balanceFor(accountId);
        if (amount.compareTo(currentBalance) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Saldo insuficiente");
        }

        AtmWithdrawal withdrawal = new AtmWithdrawal(
                UUID.randomUUID().toString(),
                accountId,
                amount.setScale(2, RoundingMode.HALF_UP),
                LocalDateTime.now()
        );
        atmWithdrawals.add(withdrawal);

        return new AtmWithdrawalResponse(
                withdrawal.operationId(),
                accountId,
                withdrawal.amount(),
                balanceFor(accountId),
                withdrawal.createdAt()
        );
    }

    public List<DataQualityDto> dataQuality() {
        return repository.qualityReports().stream()
                .map(report -> new DataQualityDto(
                        report.source(),
                        report.totalRows(),
                        report.acceptedRows(),
                        report.rejectedRows(),
                        report.duplicatedRows()
                ))
                .toList();
    }

    private BigDecimal balanceFor(Integer accountId) {
        AccountProfile account = accountOrFail(accountId);
        BigDecimal annualEffect = repository.findMovementsByAccount(accountId).stream()
                .map(this::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal atmEffect = atmWithdrawals.stream()
                .filter(withdrawal -> withdrawal.accountId().equals(accountId))
                .map(AtmWithdrawal::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal calculatedBalance = account.baseBalance()
                .add(annualEffect)
                .subtract(atmEffect)
                .setScale(2, RoundingMode.HALF_UP);
        if (calculatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return calculatedBalance;
    }

    private BigDecimal signedAmount(AnnualMovement movement) {
        if ("deposito".equals(movement.transactionType())) {
            return movement.amount();
        }
        return movement.amount().negate();
    }

    private MovementDto toMovementDto(AnnualMovement movement) {
        return new MovementDto(
                movement.date(),
                movement.transactionType(),
                movement.amount(),
                movement.description()
        );
    }

    private BigDecimal annualRateFor(String productType) {
        return switch (productType) {
            case "ahorro" -> new BigDecimal("0.018");
            case "prestamo" -> new BigDecimal("0.024");
            case "hipoteca" -> new BigDecimal("0.030");
            default -> BigDecimal.ZERO;
        };
    }

    private AccountProfile accountOrFail(Integer accountId) {
        return repository.findAccount(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }
}
