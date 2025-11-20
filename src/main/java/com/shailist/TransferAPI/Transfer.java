package com.shailist.TransferAPI;

import java.util.Objects;

/**
 * Immutable model representing a money transfer between two accounts.
 */
public final class Transfer {
    private final String id;
    private final double amount;
    private final String fromAccount;
    private final String toAccount;

    /**
     * Create a new Transfer instance.
     *
     * @param id          unique identifier for the transfer
     * @param amount      amount to transfer
     * @param fromAccount source account id
     * @param toAccount   destination account id
     */
    public Transfer(String id, double amount, String fromAccount, String toAccount) {
        this.id = Objects.requireNonNull(id, "id");
        this.amount = amount;
        this.fromAccount = Objects.requireNonNull(fromAccount, "fromAccount");
        this.toAccount = Objects.requireNonNull(toAccount, "toAccount");
    }

    /**
     * @return the transfer id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the transfer amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @return the source account id
     */
    public String getFromAccount() {
        return fromAccount;
    }

    /**
     * @return the destination account id
     */
    public String getToAccount() {
        return toAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Double.compare(transfer.amount, amount) == 0 && id.equals(transfer.id) && fromAccount.equals(transfer.fromAccount) && toAccount.equals(transfer.toAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, fromAccount, toAccount);
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                '}';
    }
}

