/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.trade.protocol.trade.buyer.tasks;

import io.bitsquare.common.taskrunner.TaskRunner;
import io.bitsquare.trade.Trade;
import io.bitsquare.trade.protocol.trade.StateUtil;
import io.bitsquare.trade.protocol.trade.TradeTask;
import io.bitsquare.trade.protocol.trade.messages.RequestPublishDepositTxMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.*;
import static io.bitsquare.util.Validator.*;

public class BuyerProcessRequestPublishDepositTxMessage extends TradeTask {
    private static final Logger log = LoggerFactory.getLogger(BuyerProcessRequestPublishDepositTxMessage.class);

    public BuyerProcessRequestPublishDepositTxMessage(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void doRun() {
        try {
            RequestPublishDepositTxMessage message = (RequestPublishDepositTxMessage) processModel.getTradeMessage();
            checkTradeId(processModel.getId(), message);
            checkNotNull(message);

            processModel.tradingPeer.setFiatAccount(checkNotNull(message.sellerFiatAccount));
            processModel.tradingPeer.setAccountId(nonEmptyStringOf(message.sellerAccountId));
            processModel.tradingPeer.setP2pSigPubKey(checkNotNull(message.sellerP2pSigPublicKey));
            processModel.tradingPeer.setTradeWalletPubKey(checkNotNull(message.sellerTradeWalletPubKey));
            processModel.tradingPeer.setP2pEncryptPubKey(checkNotNull(message.sellerP2pEncryptPublicKey));
            processModel.tradingPeer.setContractAsJson(nonEmptyStringOf(message.sellerContractAsJson));
            processModel.tradingPeer.setContractSignature(nonEmptyStringOf(message.sellerContractSignature));
            processModel.tradingPeer.setPayoutAddressString(nonEmptyStringOf(message.sellerPayoutAddressString));
            processModel.tradingPeer.setPreparedDepositTx(checkNotNull(message.sellersPreparedDepositTx));
            processModel.tradingPeer.setConnectedOutputsForAllInputs(checkNotNull(message.sellerConnectedOutputsForAllInputs));
            checkArgument(message.sellerConnectedOutputsForAllInputs.size() > 0);

            complete();
        } catch (Throwable t) {
            t.printStackTrace();
            trade.setThrowable(t);
            StateUtil.setOfferOpenState(trade);
            failed(t);
        }
    }
}