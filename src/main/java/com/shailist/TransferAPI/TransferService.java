
package com.shailist.TransferAPI;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for creating and retrieving {@link Transfer} objects.
 */
public interface TransferService {
    /**
     * Persist a transfer.
     *
     * @param transfer transfer to create
     * @return the created transfer (implementation may enrich/return the same instance)
     */
    Transfer createTransfer(Transfer transfer);

    /**
     * Retrieve a transfer by id.
     *
     * @param id transfer id
     * @return optional containing the transfer if found
     */
    Optional<Transfer> getTransferById(String id);

    /**
     * List all transfers known to the service.
     *
     * @return list of transfers
     */
    List<Transfer> listTransfers();
}
